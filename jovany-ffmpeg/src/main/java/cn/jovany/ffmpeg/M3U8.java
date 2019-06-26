package cn.jovany.ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;

import cn.jovany.ffmpeg.M3U8.Chip;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * M3u8文件解析内容体
 * 
 * @author wangqi
 *
 */
public class M3U8 implements Iterable<Chip> {

	private List<Header> headers = new ArrayList<>();
	private List<Chip> chips = new ArrayList<>();

	public M3U8(M3U8 builder) {
		super();
		this.headers = builder.headers;
		this.chips = builder.chips;
	}

	@SuppressWarnings("resource")
	public M3U8(File file) throws FileNotFoundException {
		this(new BufferedReader(new InputStreamReader(new FileInputStream(file))).lines());
	}

	public M3U8(Path path) throws FileNotFoundException {
		this(path.toFile());
	}

	/**
	 * 行流构造器
	 * 
	 * @param lines
	 */
	private M3U8(Stream<String> lines) {
		List<StringBuilder> content = new ArrayList<>();
		lines.forEach(line -> {
			if (Arrays.asList(EXTM3U, ENDLIST).contains(line)) {
				return;
			}
			if (line.matches("^#EXT-X-\\S+:\\S+$")) {
				putHeader(line.substring(EXT_X.length(), line.indexOf(":")), line.substring(line.indexOf(":") + 1));
			}
			if (line.matches("^#EXTINF:\\d+.\\d+,$")) {
				content.add(new StringBuilder(line));
			}
			if (!line.matches("^#EXTINF:\\d+.\\d+,$") && !content.isEmpty()) {
				content.get(content.size() - 1).append(line);
			}
		});
		content.forEach(b -> {
			String line = b.toString();
			putChip(line.substring((EXTINF + ":").length(), line.indexOf(",")), line.substring(line.indexOf(",") + 1));
		});
	}

	private static final String EXT = "#EXT";
	private static final String EXT_X = EXT + "-X-";
	private static final String EXTM3U = "#EXTM3U";
	private static final String ENDLIST = EXT_X + "ENDLIST";
	private static final String EXTINF = EXT + "INF";
	private static final String SEPARATOR = System.getProperty("line.separator");

	/**
	 * 增加一条头部信息
	 * 
	 * @param key
	 * @param value
	 */
	public void putHeader(String key, String value) {
		headers.add(new Header(key, value));
	}

	/**
	 * 增加一条分片信息
	 * 
	 * @param key
	 * @param value
	 */
	public void putChip(String key, String value) {
		chips.add(new Chip(key, value));
	}

	/**
	 * <h2>KEY-URI设置参数</h2>
	 * 
	 * @param url 要替换的值
	 * @return
	 * @throws Exception
	 */
	public M3U8 setKeyUri(String url) throws Exception {
		return replaceHeader(t -> "KEY".equals(t.key), old -> {
			return old.replaceAll("URI=\"\\S+\"", MessageFormat.format("URI=\"{0}\"", url));
		});
	}

	/**
	 * <h2>替换KeyURI模版字符串</h2>
	 * 
	 * 
	 * @param tpl
	 * @return
	 * @throws Exception
	 */
	public M3U8 replaceKeyUri(String tpl) throws Exception {
		return replaceHeader(t -> "KEY".equals(t.key), old -> {
			Pattern pattern = Pattern.compile("URI=\"\\S+\"");
			Matcher matcher = pattern.matcher(old);
			String match = null;
			while (matcher.find()) {
				match = matcher.group();
			}
			match = match.substring("URI=\"".length(), match.length() - 1);
			Map<String, Object> params = new HashMap<>();
			params.put("old", match);
			try {
				params.put("md5", DigestUtils.md5Hex(new FileInputStream(match)));
			} catch (Exception e) {
			}
			params.put("filename", match.substring(match.lastIndexOf('/') + 1));
			Template template = new Template("strTpl", tpl, new Configuration(new Version("2.3.23")));
			StringWriter result = new StringWriter();
			template.process(params, result);
			return old.replaceAll("URI=\"\\S+\"", MessageFormat.format("URI=\"{0}\"", result.toString()));
		});
	}

	/**
	 * <h2>替换ChipURI模版字符串</h2>
	 * 
	 * @param tpl
	 * @return
	 * @throws Exception
	 */
	public M3U8 replaceChipUri(String tpl) throws Exception {
		return replaceChip(t -> true, old -> {
			Map<String, Object> params = new HashMap<>();
			params.put("old", old);
			try {
				params.put("md5", DigestUtils.md5Hex(new FileInputStream(old)));
			} catch (Exception e) {
			}
			params.put("filename", old.substring(old.lastIndexOf('/') + 1));
			Template template = new Template("strTpl", tpl, new Configuration(new Version("2.3.23")));
			StringWriter result = new StringWriter();
			template.process(params, result);
			return result.toString();
		});
	}

	/**
	 * 同时替换KeyURI和ChipURI
	 * 
	 * @param tpl
	 * @return
	 * @throws Exception
	 */
	public M3U8 replace(String tpl) throws Exception {
		return replaceKeyUri(tpl).replaceChipUri(tpl);
	}

	/**
	 * 将指定位置的切片地址替换成参数
	 * 
	 * @param index
	 * @param tpl
	 * @return
	 * @throws Exception
	 */
	public M3U8 templateChipUri(int index, String tpl) throws Exception {
		chips.get(index).replace(old -> {
			Map<String, Object> params = new HashMap<>();
			params.put("old", old);
			try {
				params.put("md5", DigestUtils.md5Hex(new FileInputStream(old)));
			} catch (Exception e) {
			}
			params.put("filename", old.substring(old.lastIndexOf('/') + 1));
			Template template = new Template("strTpl", tpl, new Configuration(new Version("2.3.23")));
			StringWriter result = new StringWriter();
			template.process(params, result);
			return result.toString();
		});
		return this;
	}

	/**
	 * 向指定位置的切片地址模版传入参数
	 * 
	 * @param index
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public M3U8 replaceChipUri(int index, Map<String, Object> params) throws Exception {
		chips.get(index).replace(old -> {
			Template template = new Template("strTpl", old, new Configuration(new Version("2.3.23")));
			StringWriter result = new StringWriter();
			template.process(params, result);
			return result.toString();
		});
		return this;
	}

	/**
	 * 获取固定位置的切片
	 * 
	 * @param index
	 * @return
	 */
	public Chip chip(int index) {
		return chips.get(index);
	}

	/**
	 * 按条件查询一个头信息
	 * 
	 * @param filter
	 * @return
	 */
	public Header getHeader(Predicate<Header> filter) {
		try {
			return headers.stream().filter(filter).findFirst().get();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 替换切片地址的通用接口
	 * 
	 * @param filter
	 * @param valueReplacer
	 * @return
	 * @throws Exception
	 */
	public M3U8 replaceChip(Predicate<Chip> filter, ValueReplacer<String> valueReplacer) throws Exception {
		Chip[] chips = this.chips.stream().filter(filter).toArray(Chip[]::new);
		for (Chip chip : chips) {
			chip.replace(valueReplacer);
		}
		return this;
	}

	/**
	 * 替换头信息的通用接口
	 * 
	 * @param filter
	 * @param valueReplacer
	 * @return
	 * @throws Exception
	 */
	public M3U8 replaceHeader(Predicate<Header> filter, ValueReplacer<String> valueReplacer) throws Exception {
		Header[] headers = this.headers.stream().filter(filter).toArray(Header[]::new);
		for (Header header : headers) {
			header.replace(valueReplacer);
		}
		return this;
	}

	/**
	 * 头信息
	 * 
	 * @author wangqi
	 *
	 */
	public class Header {
		private final String key;
		private String value;

		public Header(String key) {
			super();
			this.key = key;
		}

		public Header(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public Header setValue(String value) {
			this.value = value;
			return this;
		}

		public Header replace(ValueReplacer<String> valueReplacer) throws Exception {
			this.value = valueReplacer.replace(value);
			return this;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

	}

	/**
	 * 切片信息
	 * 
	 * @author wangqi
	 *
	 */
	public class Chip {
		private String inf;
		private String chip;

		public Chip(String key, String value) {
			super();
			this.inf = key;
			this.chip = value;
		}

		public Chip setValue(String value) {
			this.chip = value;
			return this;
		}

		public Chip replace(ValueReplacer<String> valueReplacer) throws Exception {
			this.chip = valueReplacer.replace(chip);
			return this;
		}

		public Chip replace(Map<String, Object> params) throws Exception {
			return replace(old -> {
				Template template = new Template("strTpl", old, new Configuration(new Version("2.3.23")));
				StringWriter result = new StringWriter();
				template.process(params, result);
				return result.toString();
			});
		}

		public String getInf() {
			return inf;
		}

		public String getChip() {
			return chip;
		}

	}

	/**
	 * 值替换器
	 * 
	 * @author wangqi
	 *
	 * @param <T>
	 */
	public interface ValueReplacer<T> {

		/**
		 * 已旧值替换新值
		 * 
		 * @param old 旧值
		 * @return 新值
		 * @throws Exception
		 */
		T replace(T old) throws Exception;

	}

	/**
	 * 将整个文档填补模版参数
	 * 
	 * @param params
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public String toString(Map<String, Object> params) throws IOException, TemplateException {
		Template template = new Template("strTpl", toString(), new Configuration(new Version("2.3.23")));
		StringWriter result = new StringWriter();
		template.process(params, result);
		return result.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(EXTM3U).append(SEPARATOR);
		headers.forEach((entry) -> {
			builder.append(EXT_X).append(entry.key).append(':').append(entry.value).append(SEPARATOR);
		});
		chips.forEach((entry) -> {
			builder.append(EXTINF).append(":").append(entry.inf).append(',').append(SEPARATOR).append(entry.chip)
					.append(SEPARATOR);
		});
		builder.append(ENDLIST);
		return builder.toString();
	}

	/**
	 * 获得所有头信息
	 * 
	 * @return
	 */
	public Map<String, String> getHeaders() {
		Map<String, String> map = new HashMap<>();
		headers.forEach(e -> map.put(e.key, e.value));
		return map;
	}

	/**
	 * 获得所有分片信息
	 * 
	 * @return
	 */
	public List<Chip> getChips() {
		return chips;
	}

	/**
	 * 使文档能够按分片遍历
	 */
	@Override
	public Iterator<Chip> iterator() {
		return chips.iterator();
	}

}
