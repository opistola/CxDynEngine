/*******************************************************************************
 * Copyright (c) 2017 Checkmarx
 * 
 * This software is licensed for customer's internal use only.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.checkmarx.engine.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.checkmarx.engine.CxConfig;
import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.EngineServerResponse;
import com.checkmarx.engine.rest.model.Login;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.checkmarx.engine.rest.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

@Component
public class CxRestClient {

	private static final Logger log = LoggerFactory.getLogger(CxRestClient.class);
	
	private static final String BASE_URL = "/cxrestapi";
	private static final String AUTH_API_URL = BASE_URL + "/auth/login";
	private static final String ENGINES_API_URL = BASE_URL + "/sast/engineServers";
	private static final String SCAN_REQUESTS_URL = BASE_URL + "/sast/scansQueue";
	
	private final CxConfig config;
	private final int timeoutMillis;

	private final RestTemplate sastClient;
	private final RestTemplate engineClient;

	public CxRestClient(RestTemplateBuilder restTemplateBuilder, CxConfig config) {
		this.config = config;
		this.timeoutMillis = config.getTimeoutSecs() * 1000;
		
		this.sastClient = getSastBuilder(restTemplateBuilder).build();
		this.engineClient = getRestBuilder(restTemplateBuilder).build();

		log.info("ctor(): {}", this);
	}
	
	private RestTemplateBuilder getRestBuilder(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.requestFactory(getClientHttpRequestFactory());
	}
	
	private RestTemplateBuilder getSastBuilder(RestTemplateBuilder restTemplateBuilder) {
		return getRestBuilder(restTemplateBuilder)
				.additionalInterceptors(new CxCookieAuthInterceptor());
	}
	
	/**
	 * Creates a custom HttpClient that:
	 *  - disables SSL host verification
	 *  - disables cookie management
	 *  - sets a custom user agent
	 */
	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		
		final CloseableHttpClient httpClient = HttpClients.custom()
	        .setSSLHostnameVerifier(new NoopHostnameVerifier())
	        .setUserAgent(config.getUserAgent() + " : v" + config.getVersion())
	        .disableCookieManagement()
	        .useSystemProperties()
	        .build();
		
	    final HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
	    	= new HttpComponentsClientHttpRequestFactory(httpClient);
	    clientHttpRequestFactory.setConnectTimeout(timeoutMillis);
	    clientHttpRequestFactory.setReadTimeout(timeoutMillis);
	    return clientHttpRequestFactory;
	}

	private interface Request<R> {
		public R send();
	}
	
	private <T,R> R execute(String operation, Request<R> request) {
		final Stopwatch timer = Stopwatch.createStarted();
		boolean success = false;
		try {
			R result = request.send();
			success = true;
			return result;
		} catch (HttpClientErrorException e) {
			final ErrorResponse error = unmarshallError(e.getResponseBodyAsString());
			log.warn("Cx rest call failed: request={}; status={}; {}", operation, e.getRawStatusCode(), error);
			throw e;
		} finally {
			log.debug("Cx api call; request={}; success={}; elapsed={}ms", 
					operation, success, timer.elapsed(TimeUnit.MILLISECONDS)); 
		}
	}

	private ErrorResponse unmarshallError(String content) {
		log.trace("unmarshallError(): {}", content);
		
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(content, ErrorResponse.class);
		} catch (IOException e) {
			log.warn("Failed to unmarshall login response: content={}; cause={}", content, e.getMessage());
		}
		return new ErrorResponse(-1, content);
	}

	public boolean login() {
		return login(new Login(config.getUserName(), config.getPassword()));
	}
	
	public boolean login(Login login) {
		log.trace("login(): {}", login);

		final String url = buildUrl(AUTH_API_URL);

		final HttpEntity<Login> request = new HttpEntity<Login>(login);
		boolean success = false;
		success = execute("login", () -> {
			try {
				sastClient.postForObject(url, request, ErrorResponse.class);
				return true;
			} catch (HttpClientErrorException e) {
				if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
					return false;
				}
				throw e;
			}
		});
		return success;
	}
	
	public List<EngineServer> getEngines() {
		log.trace("getEngines()");
		
		final String url = buildEngineUrl();
		final EngineServer[] engines = execute("getEngines", () -> {
			return sastClient.getForObject(url, EngineServer[].class);
		});
		return Lists.newArrayList(engines);
	}
	
	public EngineServer getEngine(final long id) {
		log.trace("getEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		final EngineServer engine = execute("getEngine", () -> {
			return sastClient.getForObject(url, EngineServer.class);
		});
		return engine;
	}
	
	public EngineServer registerEngine(final EngineServer engine) {
		log.trace("registerEngine() : {}", engine);
		
		final String url = buildEngineUrl();
		final EngineServerResponse response = execute("registerEngine", () -> {
			return sastClient.postForObject(url, engine, EngineServerResponse.class);
		});
		return getEngine(response.getId());
	}
	
	public void unregisterEngine(long id) {
		log.trace("unregisterEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		execute("unregisterEngine", () -> {
			sastClient.delete(url);
			return true;
		});
	}
	
	public EngineServer updateEngine(EngineServer engine) {
		log.trace("updateEngine(): {}", engine);
		
		final long id = engine.getId();
		final String url = buildEngineUrl(id);
		execute("updateEngine", () -> {
			sastClient.put(url, engine);
			return true;
		});
		return getEngine(id);
	}
	
	public EngineServer blockEngine(long engineId) {
		log.trace("blockEngine(): engineId={}", engineId);
		
		final EngineServer engine = getEngine(engineId);
		if (engine == null) return null;
		
		if (engine.isBlocked()) return engine;
		
		engine.setBlocked(true);
		return updateEngine(engine);
	}

	public EngineServer unblockEngine(long engineId) {
		log.trace("unblockEngine(): engineId={}", engineId);
		
		final EngineServer engine = getEngine(engineId);
		if (engine == null) return null;
		
		if (!engine.isBlocked()) return engine;
		
		engine.setBlocked(false);
		return updateEngine(engine);
	}

	public List<ScanRequest> getScansQueue() {
		log.trace("getScansQueue()");
		
		final String url = buildUrl(SCAN_REQUESTS_URL);
		final ScanRequest[] scanRequests = execute("getScansQueue", () -> {
			return sastClient.getForObject(url, ScanRequest[].class);
		});
		return Arrays.asList(scanRequests);
	}
	
	public boolean pingEngine(String host) {
		log.trace("pingEngine(): host={}", host);
		
		final String url = buildEngineServerUrl(host);
		
		try {
			final String payload = execute("pingEngine", () -> {
				final ResponseEntity<String> response = engineClient.getForEntity(url, String.class);
				return response.getBody();
			});
			log.debug("pingEngine: response={}", payload.substring(0, 120));
			
			return true;
			
		} catch (RestClientException ex) {
			log.debug("pingEngine failed : message={}", ex.getMessage());
			return false;
		}
	}

	public String buildEngineServerUrl(String host) {
		return host + config.getCxEngineUrlPath();
	}
	
	private String buildEngineUrl(long id) {
		return buildEngineUrl() + "/" + id;
	}
	
	private String buildEngineUrl() {
		return config.getRestUrl() + ENGINES_API_URL;
	}
	
	private String buildUrl(String url) {
		return config.getRestUrl() + url;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("config", config)
				.toString();
	}

}
