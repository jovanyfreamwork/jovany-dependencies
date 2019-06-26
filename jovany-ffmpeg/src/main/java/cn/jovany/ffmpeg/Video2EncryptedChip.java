package cn.jovany.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.jovany.command.CommandApi;
import cn.jovany.ffmpeg.DirectorySignatureProvider.DirectorySignatureContext;
import cn.jovany.ffmpeg.M3U8Builder.M3U8Opt;
import cn.jovany.ffmpeg.signature.SignatureProvider;
import cn.jovany.ffmpeg.signature.SignatureTaskExecuter;
import cn.jovany.ffmpeg.signature.SignatureToken;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

@Component
public class Video2EncryptedChip extends Video2Chip implements SignatureTaskExecuter<M3U8Opt> {

	protected final OpensslApi opensslApi;

	@Autowired
	public Video2EncryptedChip(CommandApi ffmpeg, OpensslApi opensslApi) {
		super(ffmpeg);
		this.opensslApi = opensslApi;
	}

	@Override
	public Callable<M3U8Opt> callback(SignatureToken token, SignatureProvider provider) throws Exception {
		return () -> {
			DirectorySignatureContext context = ((DirectorySignatureProvider) provider).context(token);
			String outFilename = getOutputFilename(token);
			if (Files.exists(context.directory(outFilename))) { // 如果
				return new M3U8Builder(context).m3u8(outFilename).finish(() -> {
					provider.unlock(token); // 完成后一定不能忘记解锁（不然无法提前解锁）
					return null;
				});
			}

			Path encKeyPath = getEncKeyPath(token, context);
			opensslApi.rand16SaveTo(encKeyPath);

			StringBuffer videokeyInfo = new StringBuffer();
			videokeyInfo.append(URI.create(getkeyUri(token)));
			videokeyInfo.append(System.getProperty("line.separator"));
			videokeyInfo.append(encKeyPath);
			videokeyInfo.append(System.getProperty("line.separator"));
			Files.write(context.directory("enc.keyinfo"), videokeyInfo.toString().getBytes());

			File file = new File(context.data("file").toString());

			return ffmpeg //
					.append("-y") //
					.append("-i", file::getAbsolutePath)//
					.append("-c", "copy"::toString)//
					.append("-bsf:v", "h264_mp4toannexb"::toString)//
					.append("-hls_time", "1"::toString)//
					.append("-hls_list_size", "0"::toString)//
					.append("-hls_key_info_file", "enc.keyinfo"::toString)//
					.append("-hls_segment_filename", "out.%d.enc.ts"::toString)//
					.append(outFilename)//
					.directory(context.directory())//
					.execute()//
					.waitFor()// 必须要有，不然会异步执行，下面秒过，可能获取不到结果
					.call(() -> new M3U8Builder(context))//
					.m3u8(outFilename)//
//					.write("playlist.enc.m3u8")
					.templateKeyUri(context.directory(getKeyFilename(token)).toString())// 替换密钥地址为绝对路径
					.replaceChip(t -> true, old -> context.directory(old).toString())// 替换分片地址为绝对路径
					.write(outFilename)// 覆写文件（此文件将无法播放，是用来构建新的m3u8文件的模版）
					.finish(() -> {
						provider.unlock(token);
						return null;
					});
		};
	}

	@Override
	public DirectorySignatureProvider signatureProvider(SignatureToken token) throws Exception {
		File file = new File(token.getData().get("file").toString());
		setMd5(token, file);
		setKeyFilename(token, "enc.key");
		setOutputFilename(token, "out.enc.m3u8");
		setKeyUri(token, getkeyUri(token));
		Path directory = Paths.get(token.getData().get("directory").toString(), getMd5(token));
		return new DirectorySignatureProvider(directory);
	}

	public String getKeyFilename(SignatureToken token) {
		return token.getEnv().get("key").toString();
	}

	public Path getEncKeyPath(SignatureToken token, DirectorySignatureContext context)
			throws IOException, TemplateException {
		String encKeyFilename = getKeyFilename(token);
		return context.directory(encKeyFilename);
	}

	public void setKeyFilename(SignatureToken token, String filename) {
		token.getEnv().put("key", filename);
	}

	public String getkeyUri(SignatureToken token) {
		return "http://download-uri-to-video-key";
	}

	public void setKeyUri(SignatureToken token, String keyUri) throws IOException, TemplateException {
		setKeyUri(token, keyUri, new HashMap<>());
	}

	public void setKeyUri(SignatureToken token, String keyUri, Map<String, Object> params)
			throws IOException, TemplateException {
		params.putAll(token.getData());
		Template template = new Template("strTpl", keyUri, new Configuration(new Version("2.3.23")));
		StringWriter result = new StringWriter();
		template.process(params, result);
		token.getEnv().put("keyUri", result.toString());
	}

}
