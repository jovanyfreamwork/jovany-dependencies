package cn.jovany.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import cn.jovany.command.Command;
import cn.jovany.command.CommandBuilder;
import cn.jovany.command.CommandExecuter;
import cn.jovany.command.CommandResultSet;
import cn.jovany.command.Executor;
import cn.jovany.command.ExecutorBuilder;
import cn.jovany.command.ValueGenerator;

public class FfmpegBuilder implements Command, CommandBuilder {

	private final File ffmpage;
	private final FfmpegArgs args;

	private Consumer<Throwable> error;

	public Consumer<Throwable> error() {
		return error;
	}

	public FfmpegBuilder error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	public <R, T> R apply(T t, Function<T, R> error) {
		return error.apply(t);
	}

	public <T> void accept(T t, Consumer<T> error) {
		error.accept(t);
	}

	public FfmpegBuilder(File ffmpeg) {
		super();
		this.ffmpage = ffmpeg;
		this.args = new FfmpegArgs(this);
	}

	public FfmpegAttrValues append(String attr) {
		return args.append(attr);
	}

	public FfmpegBuilder append(String attr, ValueGenerator<?>... vgs) {
		FfmpegAttrValues ffmpegAttrValues = args.append(attr);
		for (ValueGenerator<?> gen : vgs) {
			ffmpegAttrValues.value(gen);
		}
		return this;
	}

	@Override
	public String toCommand() {
		return MessageFormat.format("{0} {1}", ffmpage.getAbsolutePath(), args.toCommand());
	}

	@Override
	public List<String> build() {
		List<String> command = new ArrayList<>();
		command.add(ffmpage.getAbsolutePath());

		if (args.isEmpty()) {
			return command;
		}

		args.forEach(ffmpegAttrValues -> {
			command.add(ffmpegAttrValues.attr());
			ffmpegAttrValues.forEach(value -> command.add(value.toString()));
		});

		return command;

	}

	public <R> R execute(Executor<CommandResultSet, R> result) {
		try {
			CommandResultSet commandResultSet = new CommandExecuter(this).error(error).execute();
			return commandResultSet.on(commandResultSet, new ExecutorBuilder<CommandResultSet, R>(result).error(error));
		} catch (IOException e) {
			this.accept(e, error);
			throw new NullPointerException();
		}
	}

}
