package no.nav.dokvaktmester;

import no.nav.dokvaktmester.commands.SettAvbruttBrevRedigerbart;
import org.springframework.shell.command.annotation.Command;

@Command
public class Commands {

	private final SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart;

	public Commands(SettAvbruttBrevRedigerbart settAvbruttBrevRedigerbart) {
		this.settAvbruttBrevRedigerbart = settAvbruttBrevRedigerbart;
	}

	@Command(command = "sett-avbrutt-brev-redigerbart")
	public void settAvbruttBrevRedigerbart(long journalpostId) {
		settAvbruttBrevRedigerbart.execute(journalpostId);
	}
}
