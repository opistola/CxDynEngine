package com.checkmarx.engine.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManifestUtilsTests {
	
	private static final Logger log = LoggerFactory.getLogger(ManifestUtilsTests.class);

	@Test
	public void testGetVersion() {

		final String stringVersion = ManifestUtils.getVersion(String.class);
		log.debug("version={}", stringVersion);
		assertThat(stringVersion, is(not(isEmptyOrNullString())));
		
		final String version1 = ManifestUtils.getVersion(ManifestUtils.class);
		log.debug("version={}", version1);
		assertThat(version1, is(nullValue()));
		
		final String DEFAULT_VERSION = "1.0";
	
		final String version2 = ManifestUtils.getVersion(ManifestUtils.class, DEFAULT_VERSION);
		log.debug("version={}", version2);
		assertThat(version2, is(DEFAULT_VERSION));

		final String version3 = ManifestUtils.getVersion(this, DEFAULT_VERSION);
		log.debug("version={}", version3);
		assertThat(version3, is(DEFAULT_VERSION));
	}
}
