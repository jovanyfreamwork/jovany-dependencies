package cn.jovany.command;

/**
 * 命令接口
 * 
 * @author wangqi
 *
 */
public interface Command {

	/**
	 * 构建命令字符串
	 * 
	 * @return
	 */
	String toCommand();

}
