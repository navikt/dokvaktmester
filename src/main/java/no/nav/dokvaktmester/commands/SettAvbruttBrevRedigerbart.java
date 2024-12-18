package no.nav.dokvaktmester.commands;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.ApplicationProperties;
import no.nav.dokvaktmester.AzureProperties;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ProxySelector;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Slf4j
@Component
public class SettAvbruttBrevRedigerbart {
	private final ApplicationProperties applicationProperties;
	private final AzureProperties azureProperties;
	private final RestClient restClient;
	private final RetryTemplate retryTemplate;

	public SettAvbruttBrevRedigerbart(ApplicationProperties applicationProperties,
									  AzureProperties azureProperties,
									  RestClient.Builder restClientBuilder) {
		this.applicationProperties = applicationProperties;
		this.azureProperties = azureProperties;
		ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
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

	public void execute(long journalpostId) {
		log.info("Forsøker sette avbrutt brev med journalpostId={} tilbake til redigerbar tilstand", journalpostId);
		settAvbruttBrevRedigerbart(journalpostId);
		log.info("Satte avbrutt brev med journalpostId={} tilbake til redigerbar tilstand", journalpostId);
	}

	private void settAvbruttBrevRedigerbart(long journalpostId) {
		restClient.post()
				.uri(fromUri(applicationProperties.getEndpoints().getDokprod().getUrl())
						.pathSegment("settAvbruttJournalpostRedigerbar", "{journalpostId}")
						.build(journalpostId))
				.headers(headers -> headers.setBearerAuth(getAccessToken()))
				.retrieve()
				.onStatus(httpStatusCode -> !httpStatusCode.is2xxSuccessful(), (request, response) -> {
					throw new SettAvbruttBrevRedigerbartFeiletException("Klarte ikke sette avbrutt brev til redigerbar tilstand. respons=" + IOUtils.toString(response.getBody(), UTF_8));
				}).toBodilessEntity();
	}

	private String getAccessToken() {
		return retryTemplate.execute(retryContext -> doGetAccessToken());
	}

	private String doGetAccessToken() {
		var formdata = new LinkedMultiValueMap<String, String>();
		formdata.add("grant_type", "client_credentials");
		formdata.add("client_id", azureProperties.appClientId());
		formdata.add("client_secret", azureProperties.appClientSecret());
		formdata.add("scope", applicationProperties.getEndpoints().getDokprod().getScope());
		return restClient.post()
				.uri(azureProperties.openidConfigTokenEndpoint())
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formdata)
				.retrieve()
				.toEntity(JsonNode.class)
				.getBody()
				.get("access_token").textValue();
	}
}
