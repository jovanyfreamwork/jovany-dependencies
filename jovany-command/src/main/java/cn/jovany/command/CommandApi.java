package cn.jovany.command;

import java.io.File;
import java.util.function.Consumer;

public class CommandApi {

	private final File ffmpeg;

	private Consumer<Throwable> error;

	public CommandApi(File ffmpeg) {
		super();
		this.ffmpeg = ffmpeg;
	}

	public CommandContext error(Consumer<Throwable> error) {
		return new CommandContext(ffmpeg).error(error);
	}

	public CommandContext append(String attr, ValueGenerator<?>... vgs) {
		return new CommandContext(ffmpeg).error(error).append(attr, vgs);
	}

	public CommandContext build() {
		return new CommandContext(ffmpeg).error(error);
	}
	
	public Consumer<Throwable> getError() {
		return error;
	}

	public CommandApi setError(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

}
