package cn.jovany.command;

import java.util.List;

/**
 * 可执行命令构建器
 * 
 * @author wangqi
 *
 */
public interface CommandBuilder {

	/**
	 * 构建可执行命令
	 * 
	 * @return 可执行的命令（按命令单词分割）
	 */
	List<String> build();

}
