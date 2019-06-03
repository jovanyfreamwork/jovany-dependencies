package cn.jovany.ffmpeg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import cn.jovany.command.Command;
import cn.jovany.command.ValueGenerator;

public class FfmpegAttrValues implements Command, Iterable<Object> {

	private final String ffmpegAttr;

	private final Collection<Object> attrValues;

	private final FfmpegArgs ffmpegArgs;

	public FfmpegAttrValues(FfmpegArgs ffmpegArgs, String ffmpegAttr) {
		this.ffmpegArgs = ffmpegArgs;
		this.ffmpegAttr = ffmpegAttr;
		this.attrValues = new ArrayList<>();
	}

	public FfmpegAttrValues apply(Function<FfmpegAttrValues, FfmpegAttrValues> function) {
		return function.apply(this);
	}

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

	public String attr() {
		return ffmpegAttr;
	}

	public FfmpegArgs andThen() {
		return ffmpegArgs;
	}

	public FfmpegBuilder and() {
		return ffmpegArgs.ffmpegBuilder();
	}

	@Override
	public Iterator<Object> iterator() {
		return attrValues.iterator();
	}

	@Override
	public String toCommand() {
		StringBuilder builder = new StringBuilder();
		builder.append(ffmpegAttr);
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
