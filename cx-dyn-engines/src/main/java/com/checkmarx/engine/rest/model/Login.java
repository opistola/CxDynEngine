package com.checkmarx.engine.rest.model;

import com.google.common.base.MoreObjects;

public class Login {

	private String username;
	private String password;
		
	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("username", username)
				.add("password", "xxxxxxxxx")
				.toString();
	}
}
