package cn.jovany.command;

/**
 * 值构建器
 * 
 * @author wangqi
 *
 * @param <V>
 */
public interface ValueGenerator<V> {

	/**
	 * 构建值
	 * 
	 * @return 构建的值
	 */
	V generate();

}
