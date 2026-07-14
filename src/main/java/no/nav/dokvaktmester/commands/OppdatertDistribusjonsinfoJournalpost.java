package no.nav.dokvaktmester.commands;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.ApplicationProperties;
import no.nav.dokvaktmester.api.dokarkiv.OppdaterDistribusjonsinfoRequest;
import no.nav.dokvaktmester.api.dokarkiv.UtsendingsKanalCode;
import no.nav.dokvaktmester.security.TokenService;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.ProxySelector;

import static java.time.Duration.ofSeconds;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class OppdatertDistribusjonsinfoJournalpost {
	private final ApplicationProperties applicationProperties;
	private final TokenService tokenService;
	private final RestClient restClient;

	public OppdatertDistribusjonsinfoJournalpost(ApplicationProperties applicationProperties,
	                                             TokenService tokenService,
	                                             RestClient.Builder restClientBuilder) {
		this.applicationProperties = applicationProperties;
		this.tokenService = tokenService;
		HttpClientSettings settings = HttpClientSettings.defaults()
				.withConnectTimeout(ofSeconds(15)).withReadTimeout(ofSeconds(30));
		this.restClient = restClientBuilder
				.baseUrl(applicationProperties.getEndpoints().getDokarkiv().getUrl())
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withHttpClientCustomizer(builder -> builder.proxy(ProxySelector.getDefault()).build())
						.build(settings))
				.build();
	}

	public void execute(long journalpostId, String kanal) {
		String utsendingskanal = isBlank(kanal) ? UtsendingsKanalCode.L.name() : kanal;
		validate(utsendingskanal);

		log.info("Endrer distribusjonsinfo for journalpost med journalpostId={}, setter utsendingskanal={} og status=EKSPEDERT",
				journalpostId, utsendingskanal);
		endreDistribusjonsinfoJournalpost(journalpostId, utsendingskanal);
		log.info("Endret distribusjonsinfo for journalpost med journalpostId={}, satt utsendingskanal={} og status=EKSPEDERT",
				journalpostId, utsendingskanal);
	}

	private void validate(String kanal) {
		try {
			UtsendingsKanalCode.valueOf(kanal);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("kanal må være gyldig");
		}
	}

	private void endreDistribusjonsinfoJournalpost(long journalpostId, String utsendingskanal) {
		final String accessToken = tokenService.hentAccessToken(applicationProperties.getEndpoints().getDokarkiv().getScope());
		OppdaterDistribusjonsinfoRequest endreFerdigstiltJournalpostRequest = mapRequest(utsendingskanal);
		restClient.patch()
				.uri(uriBuilder ->
						uriBuilder.path("/rest/journalpostapi/v1/journalpost/{journalpostId}/oppdaterDistribusjonsinfo")
								.build(journalpostId))
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(APPLICATION_JSON)
				.body(endreFerdigstiltJournalpostRequest)
				.retrieve().toBodilessEntity();
	}

	private OppdaterDistribusjonsinfoRequest mapRequest(String utsendingskanal) {
		return new OppdaterDistribusjonsinfoRequest(true, utsendingskanal, null, null);
	}
}
