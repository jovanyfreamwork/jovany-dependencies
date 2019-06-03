package cn.jovany.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

public class CommandExecuter {

	private final List<String> command;

	private Consumer<Throwable> error;

	public Consumer<Throwable> error() {
		return error;
	}

	public CommandExecuter error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	public CommandExecuter(List<String> command) {
		super();
		this.command = command;
	}

	public CommandExecuter(CommandBuilder builder) {
		super();
		this.command = builder.build();
	}

	public CommandResultSet execute() throws IOException {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command(command);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader buf = null;
		String line = null;
		buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		StringBuffer sb = new StringBuffer();
		while ((line = buf.readLine()) != null) {
			sb.append(line);
			continue;
		}
		return new CommandResultSet(sb.toString());
	}

	public List<String> getCommand() {
		return command;
	}

}
