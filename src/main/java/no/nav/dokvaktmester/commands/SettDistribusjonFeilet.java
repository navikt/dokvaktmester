package no.nav.dokvaktmester.commands;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokvaktmester.ApplicationProperties;
import no.nav.dokvaktmester.security.TokenService;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.ProxySelector;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Duration.ofSeconds;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Slf4j
@Component
public class SettDistribusjonFeilet {

	private static final String OPPDATER_DISTRIBUSJONSTATUS_URI = "/rest/v1/administrerforsendelse/oppdaterdistribusjonstatus";

	// Database-kolonnene distribusjon_id og endret_av i dokumentdistribusjon-db støtter henholdsvis 255 og 20 tegns lengde
	private static final int MAKS_LENGDE_DISTRIBUSJON_ID = 255;
	private static final int MAKS_LENGDE_KILDE = 20;

	private final ApplicationProperties applicationProperties;
	private final TokenService tokenService;
	private final RestClient restClient;

	public SettDistribusjonFeilet(ApplicationProperties applicationProperties,
									TokenService tokenService,
									RestClient.Builder restClientBuilder) {
		this.applicationProperties = applicationProperties;
		this.tokenService = tokenService;
		HttpClientSettings settings = HttpClientSettings.defaults()
				.withConnectTimeout(ofSeconds(15)).withReadTimeout(ofSeconds(30));
		this.restClient = restClientBuilder
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk()
						.withHttpClientCustomizer(builder -> builder.proxy(ProxySelector.getDefault()).build())
						.build(settings))
				.build();
	}

	public void execute(String distribusjonId, String referanse) {
		OppdaterDistribusjonstatusRequest request = new OppdaterDistribusjonstatusRequest(distribusjonId, referanse);
		log.info("Forsøker å sette distribusjon med distribusjonId={} til feilet, referanse={}", request.distribusjonId(), request.kilde());
		settDistribusjonFeilet(request);
		log.info("Har satt distribusjon med distribusjonId={} til feilet, referanse={}", request.distribusjonId(), request.kilde());
	}

	private void settDistribusjonFeilet(OppdaterDistribusjonstatusRequest request) {
		final String accessToken = tokenService.hentAccessToken(applicationProperties.getEndpoints().getDokdistadmin().getScope());

		restClient.put()
				.uri(fromUri(applicationProperties.getEndpoints().getDokdistadmin().getUrl())
						.path(OPPDATER_DISTRIBUSJONSTATUS_URI)
						.build().toUri())
				.headers(h -> h.setBearerAuth(accessToken))
				.contentType(APPLICATION_JSON)
				.body(request)
				.retrieve()
				.onStatus(httpStatusCode -> !httpStatusCode.is2xxSuccessful(), (clientRequest, response) -> {
					throw new SettDistribusjonFeiletException("Klarte ikke sette distribusjon til feilet. respons=" + IOUtils.toString(response.getBody(), UTF_8));
				}).toBodilessEntity();
	}

	public record OppdaterDistribusjonstatusRequest(
			String distribusjonId,
			String distribusjonstatus,
			String dokumentstatus,
			String kilde
	) {
		private static final String FEILET = "FEILET";

		public OppdaterDistribusjonstatusRequest {
			if (!hasText(distribusjonId) || distribusjonId.length() > MAKS_LENGDE_DISTRIBUSJON_ID) {
				throw new IllegalArgumentException("distribusjonId må være satt og kan maks. ha 255 tegn");
			}

			if (!hasText(kilde) || kilde.length() > MAKS_LENGDE_KILDE) {
				throw new IllegalArgumentException("referanse må være satt og kan maks. ha 20 tegn");
			}
		}

		public OppdaterDistribusjonstatusRequest(String distribusjonId, String referanse) {
			this(distribusjonId, FEILET, FEILET, referanse);
		}

	}
}
