package cn.jovany.command;

import java.util.function.Consumer;
import java.util.function.Function;

public class ExecutorBuilder<T, R> implements Function<T, R> {

	private Consumer<Throwable> error;

	private final Executor<T, R> executor;

	public ExecutorBuilder(Executor<T, R> executor) {
		super();
		this.executor = executor;
	}

	@Override
	public R apply(T t) {
		try {
			return executor.execute(t);
		} catch (Throwable e) {
			if (error != null) {
				error.accept(e);
			}
			return null;
		}
	}

	public Consumer<Throwable> error() {
		return error;
	}

	public ExecutorBuilder<T, R> error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	public Executor<T, R> executor() {
		return executor;
	}

}
