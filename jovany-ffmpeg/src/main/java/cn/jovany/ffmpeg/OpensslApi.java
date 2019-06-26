package cn.jovany.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import cn.jovany.command.CommandApi;

/**
 * OpensslApi 命令Api
 * 
 * @author wangqi
 *
 */
public class OpensslApi {

	private final CommandApi openssl;

	public OpensslApi(File opensslFile) {
		this.openssl = new CommandApi(opensslFile);
	}

	public String rand16() throws IOException, InterruptedException {
		return openssl.append("rand").append("-hex").append(16).execute().getBody();
	}

	public boolean rand16SaveTo(Path path) throws IOException, InterruptedException {
		openssl.append("rand").append(16).append("-out", path::toString).execute().waitFor();
		return Files.exists(path);
	}

}
