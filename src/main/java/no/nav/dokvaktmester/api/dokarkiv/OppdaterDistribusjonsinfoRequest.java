package no.nav.dokvaktmester.api.dokarkiv;

import java.time.OffsetDateTime;

public record OppdaterDistribusjonsinfoRequest(
	Boolean settStatusEkspedert,
	String utsendingsKanal,
	OffsetDateTime datoLest,
	Boolean tilbakestillJournalpost) {
}
