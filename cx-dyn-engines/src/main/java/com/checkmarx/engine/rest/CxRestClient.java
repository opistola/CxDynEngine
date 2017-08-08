package com.checkmarx.engine.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.checkmarx.engine.Config;
import com.checkmarx.engine.rest.model.EngineServer;
import com.checkmarx.engine.rest.model.EngineServerResponse;
import com.checkmarx.engine.rest.model.Login;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.checkmarx.engine.rest.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CxRestClient {

	private static final Logger log = LoggerFactory.getLogger(CxRestClient.class);
	
	private static final String BASE_URL = "/cxrestapi";
	private static final String AUTH_API_URL = BASE_URL + "/auth/login";
	private static final String ENGINES_API_URL = BASE_URL + "/sast/engineServers";
	private static final String SCAN_REQUESTS_URL = BASE_URL + "/sast/scanRequests";
	
	private final Config config;
	private final int timeoutMillis;

	private final RestTemplate sastClient;
	private final CxCookieAuthInterceptor authInterceptor;

	public CxRestClient(RestTemplateBuilder restTemplateBuilder, Config config) {
		this.config = config;
		
		this.authInterceptor = new CxCookieAuthInterceptor();
		this.timeoutMillis = config.getTimeoutSecs() * 1000;
		
		this.sastClient = restTemplateBuilder
				.setConnectTimeout(timeoutMillis)
				.additionalInterceptors(this.authInterceptor)
				.build();
	}

	public boolean login(Login login) {
		log.debug("login(): {}", login);

		final String url = buildUrl(AUTH_API_URL);

		final HttpEntity<Login> request = new HttpEntity<Login>(login);
		try {
			sastClient.postForObject(url, request, ErrorResponse.class);
			return true;
		} catch (HttpClientErrorException e) {
			final ErrorResponse loginResponse = tryUnmarshallErrorResponse(e.getResponseBodyAsString());
			log.warn("Login failed: {}", loginResponse);
		} catch (HttpServerErrorException e) {
			log.warn("Login failed", e);
		} catch (RestClientException e) {
			log.warn("Login failed", e);
		}
		
		return false;
	}
	
	private ErrorResponse tryUnmarshallErrorResponse(String content) {
		log.trace("tryUnmarshallErrorResponse(): {}", content);
		
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(content, ErrorResponse.class);
		} catch (IOException e) {
			log.warn("Failed to unmarshall login response: {}", e.getMessage());
		}
		return null;
	}

	public List<EngineServer> getEngines() {
		log.debug("getEngines()");
		
		final String url = buildEngineUrl();
		final EngineServer[] engines = sastClient.getForObject(url, EngineServer[].class);
		return Lists.newArrayList(engines);
	}
	
	public EngineServer getEngine(long id) {
		log.debug("getEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		final EngineServer engine = sastClient.getForObject(url, EngineServer.class);
		return engine;
	}
	
	public EngineServer registerEngine(EngineServer engine) {
		log.debug("registerEngine() : {}", engine);
		
		final String url = buildEngineUrl();
		final EngineServerResponse response = sastClient.postForObject(url, engine, EngineServerResponse.class);
		return getEngine(response.getId());
	}
	
	public void unregisterEngine(long id) {
		log.debug("unregisterEngine(): id={}", id);
		
		final String url = buildEngineUrl(id);
		sastClient.delete(url);
	}
	
	public EngineServer updateEngine(EngineServer engine) {
		log.debug("updateEngine() : {}", engine);
		
		final long id = engine.getId();
		final String url = buildEngineUrl(id);
		sastClient.put(url, engine);
		return getEngine(id);
	}
	
	public List<ScanRequest> getScanRequests() {
		log.debug("getScanRequests()");
		
		final String url = buildUrl(SCAN_REQUESTS_URL);
		final ScanRequest[] scanRequests = sastClient.getForObject(url, ScanRequest[].class);
		return Arrays.asList(scanRequests);
	}
	
	private String buildEngineUrl(long id) {
		return buildEngineUrl() + "/" + id;
	}
	
	private String buildEngineUrl() {
		return config.getCxUrl() + ENGINES_API_URL;
	}
	
	private String buildUrl(String url) {
		return config.getCxUrl() + url;
	}

}
