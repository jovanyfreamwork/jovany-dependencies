package cn.jovany.ffmpeg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.jovany.command.Command;

public class FfmpegArgs implements Command, Iterable<FfmpegAttrValues> {

	private List<FfmpegAttrValues> args = new ArrayList<>();

	private final FfmpegBuilder ffmpegBuilder;
	
	public FfmpegArgs(FfmpegBuilder ffmpegBuilder) {
		this.ffmpegBuilder = ffmpegBuilder;
	}

	public FfmpegAttrValues append(String ffmpegAttr) {
		FfmpegAttrValues arrValues = new FfmpegAttrValues(this, ffmpegAttr);
		args.add(arrValues);
		return arrValues;
	}

	@Override
	public String toCommand() {
		return null;
	}

	public List<FfmpegAttrValues> getArgs() {
		return args;
	}

	public boolean isEmpty() {
		return args.isEmpty();
	}

	@Override
	public Iterator<FfmpegAttrValues> iterator() {
		return args.iterator();
	}

	public FfmpegBuilder ffmpegBuilder() {
		return ffmpegBuilder;
	}
	
	
		

}
