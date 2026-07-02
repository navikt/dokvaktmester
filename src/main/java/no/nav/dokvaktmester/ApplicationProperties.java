package no.nav.dokvaktmester;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties("app")
public class ApplicationProperties {

	private Endpoints endpoints = new Endpoints();

	@Data
	public static class Endpoints {
		private EntraEndpoint dokprod;
		private EntraEndpoint dokdistadmin;
		private EntraEndpoint dokarkiv;
	}

	@Data
	public static class EntraEndpoint {
		private URI url;
		private String scope;
	}
}
