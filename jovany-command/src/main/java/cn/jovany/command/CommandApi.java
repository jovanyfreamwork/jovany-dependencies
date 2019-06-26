package cn.jovany.command;

import java.io.File;
import java.util.function.Consumer;

/**
 * <h2>命令API <small>门面</small></h2>
 * <ul>
 * <li>可以通过这一个对象操作整个命令模块的功能</li>
 * <li>可单例使用</li>
 * </ul>
 * 
 * 
 * @author wangqi
 *
 */
public class CommandApi {

	private final File commandFile;

	private Consumer<Throwable> error;

	/**
	 * 必须且只能指定一个本地可执行命令文件（绝对路径）
	 * 
	 * @param commandFile 本地可执行命令文件（绝对路径）
	 */
	public CommandApi(File commandFile) {
		super();
		this.commandFile = commandFile;
	}

	/**
	 * 基于新的异常监听器构建当前命令的上下文（推荐）
	 * 
	 * @param error
	 * @return 当前命令的上下文
	 */
	public CommandContext error(Consumer<Throwable> error) {
		return new CommandContext(commandFile).error(error);
	}

	/**
	 * 使用默认配置构建当前命令的上下文，并增添一组子命令（推荐）
	 * 
	 * @param attr 子命令的名称
	 * @param vgs  子命令的参数构建器（此构建器将在构建命令时被调用）
	 * @return 当前命令的上下文
	 */
	public CommandContext append(String attr, ValueGenerator<?>... vgs) {
		return new CommandContext(commandFile).error(error).append(attr, vgs);
	}

	/**
	 * 使用默认配置构建当前命令的上下文
	 * 
	 * @return
	 */
	public CommandContext build() {
		return new CommandContext(commandFile).error(error);
	}

	/**
	 * 获取默认配置的异常监听器
	 * 
	 * @return
	 */
	public Consumer<Throwable> getDefaultError() {
		return error;
	}

	/**
	 * 替换默认配置的异常监听器
	 * 
	 * @param error 异常监听器
	 * @return
	 */
	public CommandApi setError(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

}
