package cn.jovany.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * 命令执行器
 * 
 * @author wangqi
 *
 */
public class CommandExecuter {

	private final ProcessBuilder command;

	/**
	 * 命令执行器构造器
	 * 
	 * @param command 可执行的命令（按命名单词分割）
	 */
	public CommandExecuter(List<String> command) {
		super();
		this.command = new ProcessBuilder();
		this.command.command(command);
		this.command.redirectErrorStream(true);

	}

	/**
	 * 命令执行器构造器
	 * 
	 * @param builder 命令构建器
	 */
	public CommandExecuter(CommandBuilder builder) {
		this(builder.build());
	}

	/**
	 * <h2>指定当前命令的执行目录 <small>相当于执行了<code>cd ${path}命令</code></small></h2>
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public CommandExecuter directory(Path path) throws IOException {
		if (Files.notExists(path)) {
			Files.createDirectories(path);
		}
		command.directory(path.toFile());
		return this;
	}

	/**
	 * <h3>执行命令 <small> 可更改默认的执行进程参数 </small></h3>
	 * 
	 * @param processBuilder 进程构建器扩展接口
	 * @return
	 * @throws IOException
	 */
	public ProcessExecuter execute(Function<ProcessBuilder, ProcessBuilder> processBuilder) throws IOException {
		return new ProcessExecuter(processBuilder.apply(this.command).start());
	}

	/**
	 * <h3>执行命令</h3>
	 * 
	 * @return
	 * @throws IOException
	 */
	public ProcessExecuter execute() throws IOException {
		return new ProcessExecuter(command.start());
	}

}
