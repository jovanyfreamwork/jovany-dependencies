package cn.jovany.ffmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.jovany.command.CommandApi;
import cn.jovany.ffmpeg.DirectorySignatureProvider.DirectorySignatureContext;
import cn.jovany.ffmpeg.M3U8Builder.M3U8Opt;
import cn.jovany.ffmpeg.signature.SignatureProvider;
import cn.jovany.ffmpeg.signature.SignatureTaskExecuter;
import cn.jovany.ffmpeg.signature.SignatureToken;

@Component
public class Video2Chip implements SignatureTaskExecuter<M3U8Opt> {

	protected final CommandApi ffmpeg;

	@Autowired
	public Video2Chip(CommandApi ffmpeg) {
		super();
		this.ffmpeg = ffmpeg;
	}

	@Override
	public Callable<M3U8Opt> callback(SignatureToken token, SignatureProvider provider) throws Exception {
		return () -> {
			DirectorySignatureContext context = ((DirectorySignatureProvider) provider).context(token);
			String outFilename = getOutputFilename(token);
			if (Files.exists(context.directory(outFilename))) {
				return new M3U8Builder(context).m3u8(outFilename).finish(() -> {
					provider.unlock(token); // 完成后一定不能忘记解锁（不然无法提前解锁）
					return null;
				});
			}

			File file = new File(context.data("file").toString());
			return ffmpeg//
					.append("-i", file::getAbsolutePath)//
					.append("-codec", "copy"::toString)//
					.append("-vbsf", "h264_mp4toannexb"::toString)//
					.append("-map", "0"::toString)//
					.append("-f", "segment"::toString)//
					.append("-segment_list", outFilename::toString)//
					.append("-segment_time", "1"::toString)//
					.append("out.%d.ts")//
					.directory(context.directory())//
					.execute()//
					.waitFor()// 必须要有，不然会异步执行，下面秒过，可能获取不到结果
					.call(() -> new M3U8Builder(context))//
					.m3u8(outFilename)
//					.write("playlist.m3u8") // 写入此文件
					.replaceChip(t -> true, old -> context.directory(old).toString())// 替换分片地址为绝对路径
					.write(outFilename)// 覆写文件（此文件将无法播放，是用来构建新的m3u8文件的模版）
					.finish(() -> {
						provider.unlock(token); // 完成后一定不能忘记解锁（不然无法提前解锁）
						return null;
					});
		};
	}

	@Override
	public DirectorySignatureProvider signatureProvider(SignatureToken token) throws Exception {
		File file = new File(token.getData().get("file").toString());
		setMd5(token, file);
		setOutputFilename(token, "out.m3u8");
		return new DirectorySignatureProvider(getChipDirectory(token));
	}

	/**
	 * 设置md5参数至环境变量
	 * 
	 * @param token
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void setMd5(SignatureToken token, File file) throws FileNotFoundException, IOException {
		token.getEnv().put("md5", DigestUtils.md5Hex(new FileInputStream(file)));
	}

	/**
	 * 从环境变量获取md5
	 * 
	 * @param token
	 * @return
	 */
	public String getMd5(SignatureToken token) {
		return token.getEnv().get("md5").toString();
	}

	/**
	 * 从环境变量获取输出m3u8文件名
	 * 
	 * @param token
	 * @return
	 */
	public String getOutputFilename(SignatureToken token) {
		return token.getEnv().get("out").toString();
	}

	/**
	 * 获取分片目录
	 * 
	 * @param token
	 * @return
	 */
	public Path getChipDirectory(SignatureToken token) {
		return Paths.get(token.getData().get("directory").toString(), getMd5(token));
	}

	/**
	 * 设置输出m3u8文件名至环境变量
	 * 
	 * @param token
	 * @param filename
	 */
	public void setOutputFilename(SignatureToken token, String filename) {
		token.getEnv().put("out", filename);
	}

}
