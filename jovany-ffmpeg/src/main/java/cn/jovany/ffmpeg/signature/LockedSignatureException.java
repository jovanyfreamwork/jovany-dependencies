package cn.jovany.ffmpeg.signature;

/**
 * 签名已上锁，无法获取新对签名
 * 
 * @author wangqi
 *
 */
public class LockedSignatureException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SignatureToken request;

	public LockedSignatureException(SignatureToken request) {
		super("the signature target is locked");
		this.request = request;
	}

	public SignatureToken getRequest() {
		return request;
	}

}
