package no.nav.dokvaktmester.security;

import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.AzureProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ProxySelector;

import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Slf4j
@Component
public class TokenService {
	private final AzureProperties azureProperties;
	private final RestClient restClient;
	private final RetryTemplate retryTemplate;

	public TokenService(AzureProperties azureProperties,
						RestClient.Builder restClientBuilder) {
		this.azureProperties = azureProperties;
		HttpClientSettings settings = HttpClientSettings.defaults()
				.withConnectTimeout(ofSeconds(15)).withReadTimeout(ofSeconds(30));
		this.restClient = restClientBuilder
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withHttpClientCustomizer(builder -> builder.proxy(ProxySelector.getDefault()).build())
						.build(settings))
				.build();
		this.retryTemplate = RetryTemplate.builder()
				.maxAttempts(3)
				.fixedBackoff(1000)
				.retryOn(RestClientException.class)
				.build();
	}

	public String hentAccessToken(String scope) {
		return retryTemplate.execute(retryContext -> {
			if (retryContext.getRetryCount() > 0) {
				log.info("Forsøker å hente accessToken. Forsøk={}", retryContext.getRetryCount() + 1);
			}
			return doHentAccessToken(scope);
		});
	}

	private String doHentAccessToken(String scope) {
		var formdata = new LinkedMultiValueMap<String, String>();
		formdata.add("grant_type", "client_credentials");
		formdata.add("client_id", azureProperties.appClientId());
		formdata.add("client_secret", azureProperties.appClientSecret());
		formdata.add("scope", scope);

		return restClient.post()
				.uri(azureProperties.openidConfigTokenEndpoint())
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formdata)
				.retrieve()
				.toEntity(JsonNode.class)
				.getBody()
				.get("access_token").stringValue();
	}
}
