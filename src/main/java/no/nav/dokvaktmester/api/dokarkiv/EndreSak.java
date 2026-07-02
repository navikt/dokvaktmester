package no.nav.dokvaktmester.api.dokarkiv;

public record EndreSak(
		String sakstype,
		String fagsakId,
		Fagsaksystem fagsaksystem
) {

}
