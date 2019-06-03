package cn.jovany.command;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.oro.text.regex.MatchResult;

public class MatchResultSet implements MatchResult {

	private final MatchResult matchResult;

	public MatchResultSet(MatchResult matchResult) {
		super();
		this.matchResult = matchResult;
	}

	private Consumer<Throwable> error;

	public Consumer<Throwable> error() {
		return error;
	}

	public MatchResultSet error(Consumer<Throwable> error) {
		this.error = error;
		return this;
	}

	public <R> R apply(Function<MatchResultSet, R> func) {
		return func.apply(this);
	}

	public IndexOf first() {
		return new IndexOf(matchResult, 0);
	}

	public IndexOf indexOf(int index) {
		return new IndexOf(matchResult, index);
	}

	public <R, T> R apply(T t, Function<T, R> error) {
		return error.apply(t);
	}

	public <T> void accept(T t, Consumer<T> error) {
		error.accept(t);
	}

	@Override
	public int begin(int arg0) {
		return matchResult.begin(arg0);
	}

	@Override
	public int beginOffset(int arg0) {
		return matchResult.beginOffset(arg0);
	}

	@Override
	public int end(int arg0) {
		return matchResult.end(arg0);
	}

	@Override
	public int endOffset(int arg0) {
		return matchResult.endOffset(arg0);
	}

	@Override
	public String group(int arg0) {
		return matchResult.group(arg0 + 1);
	}

	@Override
	public int groups() {
		return matchResult.groups();
	}

	@Override
	public int length() {
		return matchResult.length();
	}

	public class IndexOf {

		private final CommandResultSet commandResultSet;

		public IndexOf(MatchResult matchResult, int index) {
			super();
			this.commandResultSet = new CommandResultSet(matchResult.group(index + 1));
		}

		protected <R> R get(Function<CommandResultSet, R> func) {
			return func.apply(commandResultSet);
		}

		public <R> R get(Executor<CommandResultSet, R> func) {
			return get(func, null);
		}

		public <R> R get(Executor<CommandResultSet, R> func, Consumer<Throwable> callback) {
			return new ExecutorBuilder<>(func).error(callback == null ? error : callback).apply(commandResultSet);
		}
	}

}
