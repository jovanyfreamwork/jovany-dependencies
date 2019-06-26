package cn.jovany.ffmpeg;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.jovany.ffmpeg.signature.SignatureProvider;
import cn.jovany.ffmpeg.signature.SignatureToken;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * 文件签名授权处理
 * 
 * @author wangqi
 *
 */
public class DirectorySignatureProvider implements SignatureProvider {

	private final Path filePath;

	/**
	 * 文件签名授权处理构造函数
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public DirectorySignatureProvider(Path directory) throws IOException {
		super();
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}
		if (Files.isDirectory(directory)) {
			this.filePath = Paths.get(directory.toString() + ".signature");
		} else {
			this.filePath = Paths.get(directory.getParent().toString() + ".signature");
		}
	}

	@Override
	public boolean existsSignature() {
		return Files.exists(filePath);
	}

	@Override
	public byte[] readSignature() throws IOException {
		return Files.readAllBytes(filePath);
	}

	@Override
	public void writeSignature(byte[] signature) throws IOException {
		Files.write(filePath, signature);
	}

	@Override
	public void deleteSignature() throws IOException {
		Files.deleteIfExists(filePath);
	}

	@Override
	public boolean isLocked() throws IOException {
		return Files.exists(Paths.get(filePath.toString() + ".lock"));
	}

	@Override
	public void lockSignature() throws IOException {
		Path lockPath = Paths.get(filePath.toString() + ".lock");
		if (Files.notExists(lockPath))
			Files.createFile(lockPath);
	}

	@Override
	public void unlockSignature() throws IOException {
		Files.deleteIfExists(Paths.get(filePath.toString() + ".lock"));
	}

	/**
	 * 构建文件签名授权上下文
	 * 
	 * @param token
	 * @return
	 */
	public DirectorySignatureContext context(SignatureToken token) {
		return new DirectorySignatureContext(token);
	}

	public class DirectorySignatureContext {

		private final SignatureToken token;

		public DirectorySignatureContext(SignatureToken token) {
			super();
			this.token = token;
		}

		/**
		 * 获取子路径
		 * 
		 * @param more
		 * @return
		 * @throws IOException
		 * @throws TemplateException
		 */
		public Path directory(String... more) throws IOException, TemplateException {
			return directory(new HashMap<>(), more);
		}

		/**
		 * 获取token参数
		 * 
		 * @param field
		 * @return
		 */
		public Object data(String field) {
			return token.getData().get(field);
		}

		/**
		 * 获取token环境变量
		 * 
		 * @param field
		 * @return
		 */
		public Object env(String field) {
			return token.getEnv().get(field);
		}

		/**
		 * 获取token签名
		 * 
		 * @return
		 */
		public String getSignature() {
			return token.getSignature();
		}

		/**
		 * 获取token密钥
		 * 
		 * @return
		 */
		public String getSecret() {
			return token.getSecret();
		}

		/**
		 * 开放性扩展接口
		 * 
		 * @param handle
		 * @return
		 */
		public <R> R apply(Function<DirectorySignatureContext, R> handle) {
			return handle.apply(this);
		}

		/**
		 * 
		 * @param keyUri
		 * @return
		 * @throws TemplateException
		 * @throws IOException
		 */
		public URI uri(String keyUri) throws TemplateException, IOException {
			return create(keyUri, new HashMap<>());
		}

		/**
		 * 模版字符串支持token参数和环境变量
		 * 
		 * @param keyUri
		 * @return
		 * @throws TemplateException
		 * @throws IOException
		 */
		public String template(String keyUri) throws TemplateException, IOException {
			return template(keyUri, new HashMap<>());
		}

		/**
		 * 将子路径替换模版参数
		 * 
		 * @param params
		 * @param more
		 * @return
		 * @throws IOException
		 * @throws TemplateException
		 */
		public Path directory(Map<String, Object> params, String... more) throws IOException, TemplateException {
			params.putAll(token.getData());
			List<String> morelist = new ArrayList<String>();
			for (String tmp : more) {
				Template template = new Template("strTpl", tmp, new Configuration(new Version("2.3.23")));
				StringWriter result = new StringWriter();
				template.process(params, result);
				morelist.add(result.toString());
			}
			String filePathStr = filePath.toString();

			Path path = Paths.get(filePathStr.substring(0, filePathStr.length() - ".signature".length()),
					morelist.toArray(new String[] {}));
			if (Files.isDirectory(path)) {
				Files.createDirectories(path);
			} else {
				Files.createDirectories(path.getParent());
			}
			return path;

		}

		/**
		 * 构建URI参数
		 * 
		 * @param keyUri
		 * @param params
		 * @return
		 * @throws IOException
		 * @throws TemplateException
		 */
		public URI create(String keyUri, Map<String, Object> params) throws IOException, TemplateException {
			return URI.create(template(keyUri, params));
		}

		/**
		 * 模版字符串支持token参数和环境变量
		 * 
		 * @param tpl
		 * @param params
		 * @return
		 * @throws IOException
		 * @throws TemplateException
		 */
		public String template(String tpl, Map<String, Object> params) throws IOException, TemplateException {
			params.putAll(token.getData());
			params.putAll(token.getEnv());
			Template template = new Template("strTpl", tpl, new Configuration(new Version("2.3.23")));
			StringWriter result = new StringWriter();
			template.process(params, result);
			return result.toString();
		}

	}

}
