package no.nav.dokvaktmester;

import no.nav.dokvaktmester.commands.EndreJournalfoertFagsak;
import no.nav.dokvaktmester.commands.SettDistribusjonFeilet;
import no.nav.dokvaktmester.commands.SettAvbruttBrevRedigerbart;
import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

@SuppressWarnings("unused")
@Component
public class Commands {

	private final SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart;
	private final SettDistribusjonFeilet settDistribusjonFeilet;
	private final EndreJournalfoertFagsak endreJournalfoertFagsak;

	public Commands(SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart,
					SettDistribusjonFeilet settDistribusjonFeilet,
					EndreJournalfoertFagsak endreJournalfoertFagsak) {
		this.settAvbruttBrevRedigerbart = settAvbruttBrevRedigerbart;
		this.settDistribusjonFeilet = settDistribusjonFeilet;
		this.endreJournalfoertFagsak = endreJournalfoertFagsak;
	}

	@Command(name = "sett-avbrutt-brev-redigerbart",
			description = """
					Setter brev som er feilaktiv avbrutt av saksbehandler tilbake til en redigerbare tilstand i dokarkiv og dokprod
					""",
			group = "doksys"
	)
	public void settAvbruttBrevRedigerbart(@Option(required = true) long journalpostId) {
		settAvbruttBrevRedigerbart.execute(journalpostId);
	}

	@Command(name = "sett-distribusjon-feilet",
			description = """
					Setter distribusjon og tilhørende dokumenter til FEILET i dokdist via dokdistadmin
					""",
			group = "doksys"
	)
	public void settDistribusjonFeilet(@Option(required = true) String distribusjonId, @Option(required = true) String referanse) {
		settDistribusjonFeilet.execute(distribusjonId, referanse);
	}

	@Command(name = "endre-journalfoert-fagsak",
			description = """
					Endrer journalført fagsak på en journalpost
					""",
			group = "doksys"
	)
	public void endreJournalfoertFagsak(@Option(required = true) long journalpostId, @Option(required = true) String fagsakId, @Option(required = true) String fagsaksystem, String referanse) {
		endreJournalfoertFagsak.execute(journalpostId, fagsakId, fagsaksystem, referanse);
	}
}
