package com.checkmarx.engine.rest;

import java.io.IOException;
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
import com.checkmarx.engine.rest.model.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CxRestClient {

	private static final Logger log = LoggerFactory.getLogger(CxRestClient.class);
	
	private static final String BASE_URL = "/cxrestapi";
	private static final String AUTH_API_URL = BASE_URL + "/auth/login";
	private static final String ENGINES_API_URL = BASE_URL + "/sast/engineServers";

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
			sastClient.postForObject(url, request, LoginResponse.class);
			return true;
		} catch (HttpClientErrorException e) {
			final LoginResponse loginResponse = tryUnmarshallLoginResponse(e.getResponseBodyAsString());
			log.warn("Login failed: {}", loginResponse);
		} catch (HttpServerErrorException e) {
			log.warn("Login failed", e);
		} catch (RestClientException e) {
			log.warn("Login failed", e);
		}
		
		return false;
	}
	
	private LoginResponse tryUnmarshallLoginResponse(String content) {
		log.trace("tryUnmarshallLoginResponse(): {}", content);
		
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(content, LoginResponse.class);
		} catch (IOException e) {
			log.warn("Failed to unmarshall login response: {}", e.getMessage());
		}
		return null;
	}

	public List<EngineServer> getEngines() {
		log.debug("getEngines()");
		
		final String url = buildUrl(ENGINES_API_URL);
		final EngineServer[] engines = sastClient.getForObject(url, EngineServer[].class);
		return Lists.newArrayList(engines);
	}
	
	public EngineServer getEngine(long id) {
		log.debug("getEngine(): id={}", id);
		
		final String url = buildUrl(ENGINES_API_URL) + "/" + id;
		final EngineServer engine = sastClient.getForObject(url, EngineServer.class);
		return engine;
	}
	
	public EngineServerResponse registerEngine(EngineServer engine) {
		log.debug("registerEngine() : {}", engine);
		
		final String url = buildUrl(ENGINES_API_URL);
		final EngineServerResponse response = sastClient.postForObject(url, engine, EngineServerResponse.class);
		return response;
	}
	
	public void unregisterEngine(long id) {
		log.debug("unregisterEngine(): id={}", id);
		
		final String url = buildUrl(ENGINES_API_URL) + "/" + id;
		sastClient.delete(url);
	}
	
	private String buildUrl(String url) {
		return config.getCxUrl() + url;
	}

}
