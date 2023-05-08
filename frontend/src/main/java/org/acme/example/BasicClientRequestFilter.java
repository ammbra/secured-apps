package org.acme.example;

import jakarta.annotation.Priority;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.util.Base64;

@Priority(Priorities.AUTHENTICATION)
@Provider
public class BasicClientRequestFilter implements ClientRequestFilter {

	@ConfigProperty(name = "service.token")
	String token;

	@Override
	public void filter(ClientRequestContext requestContext) {
		requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, getAccessToken());
	}

	private String getAccessToken() {
		return "Basic " + Base64.getEncoder().encodeToString((token).getBytes());
	}
}


