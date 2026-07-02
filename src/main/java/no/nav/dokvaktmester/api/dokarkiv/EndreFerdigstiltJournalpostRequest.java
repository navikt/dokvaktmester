package no.nav.dokvaktmester.api.dokarkiv;

public record EndreFerdigstiltJournalpostRequest(
		String brukerId,
		EndreSak sak,
		String tema,
		String begrunnelseNokkel
) {
	public static EndreFerdigstiltJournalpostRequest endreFagsak(String fagsakId, Fagsaksystem fagsaksystem, String referanse) {
		return new EndreFerdigstiltJournalpostRequest(null, new EndreSak("FAGSAK", fagsakId, fagsaksystem), null, referanse);
	}
}
