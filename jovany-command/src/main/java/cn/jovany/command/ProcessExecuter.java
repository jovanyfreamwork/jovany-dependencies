package cn.jovany.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * 命令执行进程&结果处理器
 * 
 * @author wangqi
 *
 */
public class ProcessExecuter implements ProcessResult {
	private final Process process;

	/**
	 * 命令执行进程&结果处理器构造函数
	 * 
	 * @param process 进程
	 */
	public ProcessExecuter(Process process) {
		super();
		this.process = process;
	}

	/**
	 * 等待进程处理结果
	 * 
	 * @return 结果进程处理器
	 * @throws InterruptedException
	 */
	public WaitForProcess waitFor() throws InterruptedException {
		return new WaitForProcess();
	}

	/**
	 * 结果进程处理器
	 * 
	 * @author wangqi
	 *
	 */
	public class WaitForProcess implements ProcessResult {

		/**
		 * 结束码
		 */
		private final int exitCode;

		/**
		 * 结果进程处理器构造函数
		 * 
		 * @throws InterruptedException
		 */
		public WaitForProcess() throws InterruptedException {
			this.exitCode = process.waitFor();
		}

		/**
		 * 判断当前进程是否执行完
		 * 
		 * @return
		 */
		public boolean isDone() {
			return exitCode == 0;
		}

		@Override
		public BufferedReader bufferedReader() {
			return ProcessExecuter.this.bufferedReader();
		}

		@Override
		public String getBody() throws IOException {
			return ProcessExecuter.this.getBody();
		}

		@Override
		public String getBody(CharSequence delimiter) throws IOException {
			return ProcessExecuter.this.getBody(delimiter);
		}

		@Override
		public Stream<String> lines() {
			return ProcessExecuter.this.lines();
		}

		@Override
		public Runnable runnable(Consumer<Stream<String>> consumer) {
			return ProcessExecuter.this.runnable(consumer);
		}

	}

	@Override
	public BufferedReader bufferedReader() {
		return new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	@Override
	public String getBody() throws IOException {
		return getBody("");
	}

	@Override
	public String getBody(CharSequence delimiter) throws IOException {
		return String.join(delimiter, lines().toArray(String[]::new));
	}

	@Override
	public Stream<String> lines() {
		return bufferedReader().lines();
	}

	@Override
	public Runnable runnable(Consumer<Stream<String>> consumer) {
		return () -> consumer.accept(lines());
	}

}