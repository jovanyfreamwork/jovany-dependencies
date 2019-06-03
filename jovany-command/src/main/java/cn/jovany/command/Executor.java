package cn.jovany.command;

public interface Executor<T, R> {

	R execute(T t) throws Throwable;
}
