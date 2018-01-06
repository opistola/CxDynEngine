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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.checkmarx.engine.CxConfig;
import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.EngineServerResponse;
import com.checkmarx.engine.rest.model.Login;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.checkmarx.engine.rest.model.ErrorResponse;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

@Component
public class CxEngineApiClient extends BaseHttpClient implements CxEngineApi {

	private static final Logger log = LoggerFactory.getLogger(CxEngineApiClient.class);
	
	private static final String BASE_URL = "/cxrestapi";
	private static final String AUTH_API_URL = BASE_URL + "/auth/login";
	private static final String ENGINES_API_URL = BASE_URL + "/sast/engineServers";
	private static final String SCAN_REQUESTS_URL = BASE_URL + "/sast/scansQueue";
	
	private final RestTemplate sastClient;

	public CxEngineApiClient(RestTemplateBuilder restTemplateBuilder, CxConfig config) {
		super(config);
		
		this.sastClient = getSastBuilder(restTemplateBuilder).build();

		log.info("ctor(): {}", this);
	}
	
	private RestTemplateBuilder getSastBuilder(RestTemplateBuilder restTemplateBuilder) {
		return super.getRestBuilder(restTemplateBuilder)
				.additionalInterceptors(new CxCookieAuthInterceptor());
	}
	
	@Override
	public boolean login() {
		return login(new Login(config.getUserName(), config.getPassword()));
	}
	
	@Override
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
	
	@Override
	public List<EngineServer> getEngines() {
		log.trace("getEngines()");
		
		final String url = buildEngineUrl();
		final EngineServer[] engines = execute("getEngines", () -> {
			return sastClient.getForObject(url, EngineServer[].class);
		});
		return Lists.newArrayList(engines);
	}
	
	@Override
	public EngineServer getEngine(final long id) {
		log.trace("getEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		final EngineServer engine = execute("getEngine", () -> {
			return sastClient.getForObject(url, EngineServer.class);
		});
		return engine;
	}
	
	@Override
	public EngineServer registerEngine(final EngineServer engine) {
		log.trace("registerEngine() : {}", engine);
		
		final String url = buildEngineUrl();
		final EngineServerResponse response = execute("registerEngine", () -> {
			return sastClient.postForObject(url, engine, EngineServerResponse.class);
		});
		return getEngine(response.getId());
	}
	
	@Override
	public void unregisterEngine(long id) {
		log.trace("unregisterEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		execute("unregisterEngine", () -> {
			sastClient.delete(url);
			return true;
		});
	}
	
	@Override
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
	
	@Override
	public EngineServer blockEngine(long engineId) {
		log.trace("blockEngine(): engineId={}", engineId);
		
		final EngineServer engine = getEngine(engineId);
		if (engine == null) return null;
		
		if (engine.isBlocked()) return engine;
		
		engine.setBlocked(true);
		return updateEngine(engine);
	}

	@Override
	public EngineServer unblockEngine(long engineId) {
		log.trace("unblockEngine(): engineId={}", engineId);
		
		final EngineServer engine = getEngine(engineId);
		if (engine == null) return null;
		
		if (!engine.isBlocked()) return engine;
		
		engine.setBlocked(false);
		return updateEngine(engine);
	}

	@Override
	public List<ScanRequest> getScansQueue() {
		log.trace("getScansQueue()");
		
		final String url = buildUrl(SCAN_REQUESTS_URL);
		final ScanRequest[] scanRequests = execute("getScansQueue", () -> {
			return sastClient.getForObject(url, ScanRequest[].class);
		});
		return Arrays.asList(scanRequests);
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