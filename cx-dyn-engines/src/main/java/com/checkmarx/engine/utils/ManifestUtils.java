package com.checkmarx.engine.utils;

import javax.validation.constraints.NotNull;

public class ManifestUtils {
	
	public static String getVersion(@NotNull Object object) {
		return getVersion(object.getClass());
	}
	
	public static String getVersion(@NotNull Object object, String defaultVersion) {
		return getVersion(object.getClass(), defaultVersion);
	}
	
	public static String getVersion(@NotNull Class<?> clazz) {
		return getVersion(clazz, null);
	}

	public static String getVersion(@NotNull Class<?> clazz, String defaultVersion) {
		final String version = clazz.getPackage().getImplementationVersion(); 
		return version == null ? defaultVersion : version;
	}

}
