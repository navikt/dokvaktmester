package no.nav.dokvaktmester.commands;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.ApplicationProperties;
import no.nav.dokvaktmester.api.dokarkiv.FerdigstillJournalpostRequest;
import no.nav.dokvaktmester.api.dokarkiv.OppdaterDistribusjonsinfoRequest;
import no.nav.dokvaktmester.api.dokarkiv.UtsendingsKanalCode;
import no.nav.dokvaktmester.security.TokenService;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.ProxySelector;
import java.time.LocalDateTime;

import static java.time.Duration.ofSeconds;
import static org.apache.logging.log4j.util.Strings.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class SettNyKanalDistribusjonsinfoJournalpostOgFerdigstill {
	public static final String MASKINELL_ENHET = "9999";
	private final ApplicationProperties applicationProperties;
	private final TokenService tokenService;
	private final RestClient restClient;

	public SettNyKanalDistribusjonsinfoJournalpostOgFerdigstill(ApplicationProperties applicationProperties,
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

		var statusEtterFerdigstilling = utsendingskanal.equals("L") ? "FL" : "FS";
		log.info("Endrer distribusjonsinfo for journalpost med journalpostId={}, setter utsendingskanal={}, dato_ekspedert=<nå> og status={}",
				journalpostId, utsendingskanal, statusEtterFerdigstilling);
		endreKanalDistribusjonsinfoJournalpostOgFerdigstill(journalpostId, utsendingskanal);
		log.info("Endret distribusjonsinfo for journalpost med journalpostId={}, satt utsendingskanal={}, dato_ekspedert=<nå> og status={}",
				journalpostId, utsendingskanal, statusEtterFerdigstilling);
	}

	private void validate(String kanal) {
		try {
			UtsendingsKanalCode.valueOf(kanal);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("kanal må være gyldig");
		}
	}

	private void endreKanalDistribusjonsinfoJournalpostOgFerdigstill(long journalpostId, String utsendingskanal) {
		final String accessToken = tokenService.hentAccessToken(applicationProperties.getEndpoints().getDokarkiv().getScope());
		OppdaterDistribusjonsinfoRequest endreKanalDistribusjonsinfoRequest = mapEndreKanalDistribusjonsinfoRequest(utsendingskanal);
		log.info("Sender request for å endre ursendingskanal og dato_ekspedert for journalpost med journalpostId={}", journalpostId);
		restClient.patch()
				.uri(uriBuilder ->
						uriBuilder.path("/rest/journalpostapi/v1/journalpost/{journalpostId}/oppdaterDistribusjonsinfo")
								.build(journalpostId))
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(APPLICATION_JSON)
				.body(endreKanalDistribusjonsinfoRequest)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (_, response) -> {
					if (!new String(response.getBody().readAllBytes()).contains("Journalposten har journalpoststatus=E")) {
						throw new RestClientResponseException("", response.getStatusCode().value(), response.getStatusText(), null, null, null);
					}
					log.warn("Journalpost med journalpostId={} hadde allerede status=E, dato_ekspedert har ikke blitt oppdatert. Fortsetter til ferdigstilling...", journalpostId);
				})
				.toBodilessEntity();
		log.info("Endret distribusjonsinfo for journalpost med journalpostId={}, satt utsendingskanal={}, dato_ekspedert=<nå>", journalpostId, utsendingskanal);
		log.info("Sender request for å ferdigstille journalpost med journalpostId={}", journalpostId);
		restClient.patch()
			.uri(uriBuilder ->
				uriBuilder.path("/rest/journalpostapi/v1/journalpost/{journalpostId}/ferdigstill")
					.build(journalpostId))
			.headers(h -> h.setBearerAuth(accessToken))
			.contentType(APPLICATION_JSON)
			.body(new FerdigstillJournalpostRequest(MASKINELL_ENHET))
			.retrieve().toBodilessEntity();
	}

	private OppdaterDistribusjonsinfoRequest mapEndreKanalDistribusjonsinfoRequest(String utsendingskanal) {
		return new OppdaterDistribusjonsinfoRequest(true, utsendingskanal, null, null);
	}
}
