package cn.jovany.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 执行结果处理
 * 
 * @author wangqi
 *
 */
public interface ProcessResult {

	/**
	 * 获取结果流读取器
	 * 
	 * @return
	 */
	BufferedReader bufferedReader();

	/**
	 * 获取结果字符串
	 * 
	 * @return
	 * @throws IOException
	 */
	String getBody() throws IOException;

	/**
	 * 获取结果字符串
	 * 
	 * @param delimiter
	 * @return
	 * @throws IOException
	 */
	String getBody(CharSequence delimiter) throws IOException;

	/**
	 * 获取行的结果流
	 * 
	 * @return
	 */
	Stream<String> lines();

	/**
	 * 构建行结果流的线程执行体
	 * 
	 * @param consumer
	 * @return
	 */
	Runnable runnable(Consumer<Stream<String>> consumer);

	/**
	 * 执行开放性扩展接口
	 * 
	 * @param func 开放性扩展接口
	 * @return 开放性扩展接口执行结果
	 */
	public default <R> R apply(Function<ProcessResult, R> func) {
		return func.apply(this);
	}

	/**
	 * 开放性回调接口
	 * 
	 * @param callable 回调接口
	 * @return
	 * @throws Exception
	 */
	public default <R> R call(Callable<R> callable) throws Exception {
		return callable.call();
	}

}
