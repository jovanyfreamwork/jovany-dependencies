package cn.jovany.ffmpeg.signature;

/**
 * 无效的签名
 * 
 * @author wangqi
 *
 */
public class InvalidSignatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidSignatureException() {
		super();
	}

	public InvalidSignatureException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidSignatureException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidSignatureException(String message) {
		super(message);
	}

	public InvalidSignatureException(Throwable cause) {
		super(cause);
	}

}
