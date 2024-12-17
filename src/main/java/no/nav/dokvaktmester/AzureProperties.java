package no.nav.dokvaktmester;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

/**
 * Settes av nais platformen
 */
@ConfigurationProperties("azure")
public record AzureProperties(
		URI openidConfigTokenEndpoint,
		String appClientId,
		String appClientSecret
) {
	@Override
	public String toString() {
		return "AzureProperties{" +
			   "openidConfigTokenEndpoint=" + openidConfigTokenEndpoint +
			   ", appClientId='" + appClientId + '\'' +
			   '}';
	}
}
