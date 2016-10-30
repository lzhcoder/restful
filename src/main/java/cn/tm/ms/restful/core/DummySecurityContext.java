package cn.tm.ms.restful.core;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class DummySecurityContext implements SecurityContext {

	@Override
	public boolean isUserInRole(final String role) {
		return false;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public String getAuthenticationScheme() {
		return null;
	}
}
