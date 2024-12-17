package no.nav.dokvaktmester;

import no.nav.dokvaktmester.commands.SettAvbruttBrevRedigerbart;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command(group = "doksys")
public class DoksysCommands {

	private final SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart;

	public DoksysCommands(SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart) {
		this.settAvbruttBrevRedigerbart = settAvbruttBrevRedigerbart;
	}

	@Command(command = "sett-avbrutt-brev-redigerbart",
			description = """
					Setter brev som er feilaktiv avbrutt av saksbehandler tilbake til en redigerbare tilstand i dokarkiv og dokprod
					""")
	public void settAvbruttBrevRedigerbart(@Option(required = true) long journalpostId) {
		settAvbruttBrevRedigerbart.execute(journalpostId);
	}
}
