package no.nav.dokvaktmester;

import no.nav.dokvaktmester.commands.SettDistribusjonFeilet;
import no.nav.dokvaktmester.commands.SettAvbruttBrevRedigerbart;
import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

@Component
public class DoksysCommands {

	private final SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart;
	private final SettDistribusjonFeilet settDistribusjonFeilet;

	public DoksysCommands(SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart,
			SettDistribusjonFeilet settDistribusjonFeilet) {
		this.settAvbruttBrevRedigerbart = settAvbruttBrevRedigerbart;
		this.settDistribusjonFeilet = settDistribusjonFeilet;
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
	public void settDistribusjonFeilet(@Option(required = true) String distribusjonId, @Option(required = true) String mmaSak) {
		settDistribusjonFeilet.execute(distribusjonId, mmaSak);
	}
}
