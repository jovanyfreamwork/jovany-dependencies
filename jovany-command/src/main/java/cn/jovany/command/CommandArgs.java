package cn.jovany.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommandArgs implements Command, Iterable<CommandAttrValues> {

	private List<CommandAttrValues> args = new ArrayList<>();

	private final CommandContext commandContext;

	public CommandArgs(CommandContext commandContext) {
		this.commandContext = commandContext;
	}

	public CommandAttrValues append(String ffmpegAttr) {
		CommandAttrValues arrValues = new CommandAttrValues(this, ffmpegAttr);
		args.add(arrValues);
		return arrValues;
	}

	@Override
	public String toCommand() {
		return String.join(" ", args.stream().map(CommandAttrValues::toCommand).toArray(String[]::new));
	}

	public List<CommandAttrValues> getArgs() {
		return args;
	}

	public boolean isEmpty() {
		return args.isEmpty();
	}

	@Override
	public Iterator<CommandAttrValues> iterator() {
		return args.iterator();
	}

	public CommandContext commandContext() {
		return commandContext;
	}

	@Override
	public String toString() {
		return toCommand();
	}

}
