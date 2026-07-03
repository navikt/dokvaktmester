package no.nav.dokvaktmester.commands;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.ApplicationProperties;
import no.nav.dokvaktmester.api.dokarkiv.EndreFerdigstiltJournalpostRequest;
import no.nav.dokvaktmester.api.dokarkiv.Fagsaksystem;
import no.nav.dokvaktmester.security.TokenService;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.ProxySelector;

import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class EndreJournalfoertFagsak {
	private static final int FAGSAK_MAX_LENGTH = 40;
	private static final int REFERANSE_MAX_LENGTH = 20;

	private final ApplicationProperties applicationProperties;
	private final TokenService tokenService;
	private final RestClient restClient;

	public EndreJournalfoertFagsak(ApplicationProperties applicationProperties,
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

	public void execute(long journalpostId, String fagsakId, String fagsaksystem, String referanse) {
		validate(fagsakId, fagsaksystem, referanse);

		log.info("Endre journalført fagsak på journalpostId={} til fagsakId={}, fagsaksystem={} med referanse={}",
				journalpostId, fagsakId, fagsaksystem, referanse);
		endreJournalfoertFagsak(journalpostId, fagsakId, fagsaksystem, referanse);
		log.info("Endret journalført fagsak på journalpostId={} til fagsakId={}, fagsaksystem={} med referanse={}",
				journalpostId, fagsakId, fagsaksystem, referanse);
	}

	private void validate(String fagsakId, String fagsaksystem, String referanse) {
		if (fagsakId.length() > FAGSAK_MAX_LENGTH) {
			throw new IllegalArgumentException("fagsakId kan ikke være lengre enn " + FAGSAK_MAX_LENGTH + " tegn");
		}
		if (fagsaksystem.length() > FAGSAK_MAX_LENGTH) {
			throw new IllegalArgumentException("fagsaksystem kan ikke være lengre enn " + FAGSAK_MAX_LENGTH + " tegn");
		}
		try {
			Fagsaksystem.valueOf(fagsaksystem);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("fagsaksystem må være gyldig");
		}
		if (referanse.length() > REFERANSE_MAX_LENGTH) {
			throw new IllegalArgumentException("referanse kan ikke være lengre enn " + REFERANSE_MAX_LENGTH + " tegn");
		}
	}

	private void endreJournalfoertFagsak(long journalpostId, String fagsakId, String fagsaksystem, String referanse) {
		final String accessToken = tokenService.hentAccessToken(applicationProperties.getEndpoints().getDokarkiv().getScope());
		EndreFerdigstiltJournalpostRequest endreFerdigstiltJournalpostRequest = mapRequest(fagsakId, fagsaksystem, referanse);
		restClient.patch()
				.uri(uriBuilder ->
						uriBuilder.path("/rest/internal/journalpostapi/v1/journalpost/{journalpostId}/endreFerdigstiltJournalpost")
								.build(journalpostId))
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(APPLICATION_JSON)
				.body(endreFerdigstiltJournalpostRequest)
				.retrieve().toBodilessEntity();
	}

	private EndreFerdigstiltJournalpostRequest mapRequest(String fagsakId, String fagsaksystem, String referanse) {
		return EndreFerdigstiltJournalpostRequest.endreFagsak(fagsakId, Fagsaksystem.valueOf(fagsaksystem), referanse);
	}
}
