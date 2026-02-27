package no.nav.dokvaktmester;

import no.nav.dokvaktmester.commands.SettAvbruttBrevRedigerbart;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

public class DoksysCommands {

	private final SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart;

	public DoksysCommands(SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart) {
		this.settAvbruttBrevRedigerbart = settAvbruttBrevRedigerbart;
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
}
