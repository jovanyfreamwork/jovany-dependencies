package cn.jovany.ffmpeg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import cn.jovany.command.ValueGenerator;

public class FfmpegAttrValues implements Iterable<Object> {

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

}
