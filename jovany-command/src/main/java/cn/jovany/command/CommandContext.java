package cn.jovany.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 命令上下文
 * 
 * @author wangqi
 *
 */
public class CommandContext implements Command, CommandBuilder {

	private final File command;
	private final CommandArgs args;

	/**
	 * 当前上下文的异常监听器
	 */
	private Consumer<Throwable> error;

	/**
	 * 获取当前上下文的异常监听器
	 * 
	 * @return 异常监听器
	 */
	public Consumer<Throwable> error() {
		return error;
	}

	/**
	 * 设置当前上下文的异常监听器
	 * 
	 * @param error
	 * @return this
	 */
	public CommandContext error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	/**
	 * 开放性扩展接口（有返回结果）
	 * 
	 * @param t    开放性扩展接口参数
	 * @param func 扩展接口
	 * @return 扩展接口执行结果
	 */
	public <R, T> R apply(T t, Function<T, R> func) {
		return func.apply(t);
	}

	/**
	 * 执行扩展接口（有返回结果）
	 * 
	 * @param func 扩展接口
	 * @return 扩展接口执行结果
	 */
	public <R> R apply(Function<CommandContext, R> func) {
		return func.apply(this);
	}

	/**
	 * 开放性扩展接口（无返回结果）
	 * 
	 * @param t    开放性扩展接口参数
	 * @param func 扩展接口
	 * @return this
	 */
	public <T> CommandContext accept(T t, Consumer<T> func) {
		func.accept(t);
		return this;
	}

	/**
	 * 命令上下文构造器
	 * 
	 * @param command 可执行的命令文件（绝对地址）
	 */
	public CommandContext(File command) {
		super();
		this.command = command;
		this.args = new CommandArgs(this);
	}

	/**
	 * 增添一组子命令
	 * 
	 * @param attr 子命令的名称
	 * @param vgs  子命令的参数构建器（此构建器将在构建命令时被调用）
	 * @return 当前命令的上下文
	 */
	public CommandContext append(Object attr, ValueGenerator<?>... vgs) {
		CommandAttrValues commandAttrValues = args.append(attr.toString());
		for (ValueGenerator<?> gen : vgs) {
			commandAttrValues.value(gen);
		}
		return this;
	}

	@Override
	public String toCommand() {
		return MessageFormat.format("{0} {1}", command.getAbsolutePath(), args.toCommand());
	}

	@Override
	public List<String> build() {
		List<String> command = new ArrayList<>();
		command.add(this.command.getAbsolutePath());

		if (args.isEmpty()) {
			return command;
		}

		args.forEach(ffmpegAttrValues -> {
			command.add(ffmpegAttrValues.attr());
			ffmpegAttrValues.forEach(value -> command.add(value.toString()));
		});

		return command;

	}

	/**
	 * <h3>构建命令执行器，并指定命令的作用目录<small> 相当于先执行了<code> cd ${path} </code>命令
	 * </small></h3>
	 * 
	 * 
	 * @param path 绝对路径，且必须是目录
	 * @return 命令执行器
	 * @throws IOException
	 */
	public CommandExecuter directory(Path path) throws IOException {
		return new CommandExecuter(this).directory(path);
	}

	/**
	 * 构建命令执行器
	 * 
	 * * @return 命令执行器
	 */
	public CommandExecuter toCommandExecuter() {
		return new CommandExecuter(this);
	}

	/**
	 * <h3>构建命令执行器后，直接执行命令 <small> 可更改默认的执行进程参数 </small></h3>
	 * 
	 * @param processBuilder 进程构建器扩展接口
	 * @return
	 * @throws IOException
	 */
	public ProcessExecuter execute(Function<ProcessBuilder, ProcessBuilder> processBuilder) throws IOException {
		return new CommandExecuter(this).execute(processBuilder);
	}

	/**
	 * <h3>构建命令执行器后，直接执行命令</h3>
	 * 
	 * @return
	 * @throws IOException
	 */
	public ProcessExecuter execute() throws IOException {
		return new CommandExecuter(this).execute();
	}

	@Override
	public String toString() {
		return toCommand();
	}

}
