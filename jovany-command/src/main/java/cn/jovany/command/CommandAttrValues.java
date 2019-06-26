package cn.jovany.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * 子命令组
 * 
 * @author wangqi
 *
 */
public class CommandAttrValues implements Command, Iterable<Object> {

	/**
	 * 子命令名称
	 */
	private final String attr;

	/**
	 * 子命令参数
	 */
	private final Collection<Object> attrValues;

	/**
	 * 当前子命令绑定的子命令集合
	 */
	private final CommandArgs commandArgs;

	/**
	 * 
	 * @param commandArgs 当前子命令绑定的子命令集合
	 * @param attr        子命令名称
	 */
	public CommandAttrValues(CommandArgs commandArgs, String attr) {
		this.commandArgs = commandArgs;
		this.attr = attr;
		this.attrValues = new ArrayList<>();
	}

	/**
	 * 执行扩展接口
	 * 
	 * @param function 扩展接口
	 * @return 当前对象
	 */
	public CommandAttrValues apply(Function<CommandAttrValues, CommandAttrValues> function) {
		return function.apply(this);
	}

	/**
	 * 从参数获取器构建参数，并关联子命令
	 * 
	 * @param generator
	 * @return
	 */
	public <V> V value(ValueGenerator<V> generator) {
		V value = generator.generate();
		if (value instanceof Iterable) {
			((Iterable<?>) value).forEach(attrValues::add);
			return value;
		}
		attrValues.add(value);
		return value;
	}

	@Override
	public String toString() {
		return toCommand();
	}

	/**
	 * 获取子命令名称
	 * 
	 * @return 子命令名称
	 */
	public String attr() {
		return attr;
	}

	/**
	 * 获取关联的子命令组
	 * 
	 * @return 子命令组
	 */
	public CommandArgs andThen() {
		return commandArgs;
	}

	/**
	 * 获取关联的命令上下文
	 * 
	 * @return 命令上下文
	 */
	public CommandContext and() {
		return commandArgs.commandContext();
	}

	/**
	 * 使子命令能够遍历参数
	 * 
	 * @return
	 */
	@Override
	public Iterator<Object> iterator() {
		return attrValues.iterator();
	}

	@Override
	public String toCommand() {
		StringBuilder builder = new StringBuilder();
		builder.append(attr);
		if (attrValues.isEmpty()) {
			return builder.toString();
		}
		int i = 0;
		Iterator<Object> attrValues = this.attrValues.iterator();
		while (attrValues.hasNext()) {
			if (i++ < this.attrValues.size()) {
				builder.append(' ');
			}
			builder.append(attrValues.next());
		}
		return builder.toString();
	}

}
