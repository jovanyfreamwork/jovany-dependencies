package cn.jovany.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import cn.jovany.command.CommandExecuter;
import cn.jovany.command.CommandResultSet;
import cn.jovany.command.Executor;
import cn.jovany.command.ExecutorBuilder;

public class Ffmpeg {

	private final File ffmpeg;

	private Consumer<Throwable> error;

	public Ffmpeg(File ffmpeg) {
		super();
		this.ffmpeg = ffmpeg;
	}

	public FfmpegBuilder make(Consumer<Throwable> error) {
		return new FfmpegBuilder(ffmpeg).error(error);
	}

	public FfmpegBuilder build() {
		return new FfmpegBuilder(ffmpeg).error(error);
	}

	public Consumer<Throwable> error() {
		return error;
	}

	public Ffmpeg error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	public static <R> Function<FfmpegBuilder, R> execute(File file, Executor<CommandResultSet, R> result,
			Consumer<Throwable> error) {
		return ffmpeg -> {
			ffmpeg.append("-i").value(file::getAbsolutePath);
			try {
				CommandResultSet commandResultSet = new CommandExecuter(ffmpeg).execute();
				return commandResultSet.on(commandResultSet,
						new ExecutorBuilder<CommandResultSet, R>(result).error(error));
			} catch (IOException e) {
				ffmpeg.accept(e, error);
				throw new NullPointerException();
			}
		};
	}

}
