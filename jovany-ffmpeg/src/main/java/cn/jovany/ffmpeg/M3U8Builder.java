package cn.jovany.ffmpeg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import cn.jovany.ffmpeg.DirectorySignatureProvider.DirectorySignatureContext;
import cn.jovany.ffmpeg.M3U8.Chip;
import cn.jovany.ffmpeg.M3U8.Header;
import cn.jovany.ffmpeg.M3U8.ValueReplacer;
import freemarker.template.TemplateException;

/**
 * m3u8构建器
 * 
 * @author wangqi
 *
 */
public class M3U8Builder {

	/**
	 * 文件夹的授权上下文
	 */
	private final DirectorySignatureContext context;

	/**
	 * 文件夹
	 */
	private final Path directory;

	/**
	 * 搜索全部切片的搜索器
	 */
	private static final Predicate<Chip> ALL = t -> true;

	/**
	 * 
	 * @param context 当前文件夹的授权上下文
	 * @throws IOException
	 * @throws TemplateException
	 */
	public M3U8Builder(DirectorySignatureContext context) throws IOException, TemplateException {
		this.context = context;
		this.directory = context.directory();
	}

	/**
	 * 指定m3u8文件名称，解析m3u8文件，并构建m3u8操作对象
	 * 
	 * @param filename
	 * @return
	 * @throws FileNotFoundException
	 */
	public M3U8Opt m3u8(String filename) throws FileNotFoundException {
		Path path2M3U8 = Paths.get(directory.toString(), filename);
		M3U8 file = new M3U8(path2M3U8);
		return new M3U8Opt(file, path2M3U8);
	}

	/**
	 * 获取目录
	 * 
	 * @return
	 */
	public Path getDirectory() {
		return directory;
	}

	/**
	 * m3u8操作对象
	 * 
	 * @author wangqi
	 *
	 */
	public class M3U8Opt {

		private final M3U8 m3u8;
		private final Path path;

		/**
		 * M3U8操作器
		 * 
		 * @param m3u8 m3u8文件内容体
		 * @param path m3u8文件的绝对地址
		 */
		public M3U8Opt(M3U8 m3u8, Path path) {
			this.m3u8 = m3u8;
			this.path = path;
		}

		/**
		 * 获取m3u8文件内容体
		 * 
		 * @return m3u8文件内容体
		 */
		public M3U8 m3u8() {
			return m3u8;
		}

		/**
		 * 设置指定位置的分片地址
		 * 
		 * @param index
		 * @param tpl
		 * @return
		 * @throws Exception
		 */
		public M3U8Opt templateChipUri(int index, String tpl) throws Exception {
			m3u8.templateChipUri(index, tpl);
			return this;
		}

		/**
		 * 替换指定位置的分片模版参数
		 * 
		 * @param index
		 * @param params
		 * @return
		 * @throws Exception
		 */
		public M3U8Opt replaceChipUri(int index, Map<String, Object> params) throws Exception {
			m3u8.replaceChipUri(index, params);
			return this;
		}

		/**
		 * 获取当前目录下的子路径
		 * 
		 * @param more
		 * @return
		 * @throws IOException
		 * @throws TemplateException
		 */
		public Path directory(String... more) throws IOException, TemplateException {
			return context.directory(more);
		}

		/**
		 * 
		 * @param baseURI
		 * @return
		 * @throws Exception
		 */
		public M3U8Opt chipBaseURI(String baseURI) throws Exception {
			String baseURITpl = context.template(baseURI);
			m3u8.replaceChip(ALL, (ValueReplacer<String>) old -> baseURITpl + "/" + old);
			return this;
		}

		public DirectorySignatureContext context() {
			return context;
		}

		public M3U8Opt write(String path) throws IOException, TemplateException {
			Files.write(context.directory(path), m3u8.toString().getBytes());
			return this;
		}

		/**
		 * 任务完成时调用此函数
		 * 
		 * @param func
		 * @return
		 * @throws Exception
		 */
		public M3U8Opt finish(Callable<?> func) throws Exception {
			func.call();
			return this;
		}

		public M3U8Opt replace(String tpl) throws Exception {
			m3u8.replace(tpl);
			return this;
		}

		public M3U8Opt replaceChip(Predicate<Chip> filter, ValueReplacer<String> valueReplacer) throws Exception {
			m3u8.replaceChip(filter, valueReplacer);
			return this;
		}

		public M3U8Opt replaceHeader(Predicate<Header> filter, ValueReplacer<String> valueReplacer) throws Exception {
			m3u8.replaceHeader(filter, valueReplacer);
			return this;
		}

		public M3U8Opt templateKeyUri(String tpl) throws Exception {
			m3u8.setKeyUri(tpl);
			return this;
		}

		public Path path() {
			return path;
		}

	}

}
