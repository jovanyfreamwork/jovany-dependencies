package cn.jovany.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 子命令参数集合
 * 
 * @author wangqi
 *
 */
public class CommandArgs implements Command, Iterable<CommandAttrValues> {

	private List<CommandAttrValues> args = new ArrayList<>();

	private final CommandContext commandContext;

	/**
	 * 
	 * @param commandContext 命令上下文
	 */
	public CommandArgs(CommandContext commandContext) {
		this.commandContext = commandContext;
	}

	/**
	 * 新增一组子命令参数
	 * 
	 * @param ffmpegAttr
	 * @return
	 */
	public CommandAttrValues append(String ffmpegAttr) {
		CommandAttrValues arrValues = new CommandAttrValues(this, ffmpegAttr);
		args.add(arrValues);
		return arrValues;
	}

	@Override
	public String toCommand() {
		return String.join(" ", args.stream().map(CommandAttrValues::toCommand).toArray(String[]::new));
	}

	/**
	 * 获取所有的子命令组
	 * 
	 * @return
	 */
	public List<CommandAttrValues> getArgs() {
		return args;
	}

	/**
	 * 判断当前子命令集合是否为空
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return args.isEmpty();
	}

	/**
	 * 使子命令集合可以被遍历
	 */
	@Override
	public Iterator<CommandAttrValues> iterator() {
		return args.iterator();
	}

	/**
	 * 获取当前关联的命令上下文
	 * 
	 * @return 当前关联的命令上下文
	 */
	public CommandContext commandContext() {
		return commandContext;
	}

	@Override
	public String toString() {
		return toCommand();
	}

}
