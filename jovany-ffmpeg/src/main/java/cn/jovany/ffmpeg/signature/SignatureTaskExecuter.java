package cn.jovany.ffmpeg.signature;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.util.concurrent.ListenableFutureTask;

import io.jsonwebtoken.JwtException;

/**
 * 签名任务构造器
 * 
 * @author wangqi
 *
 * @param <T>
 */
public interface SignatureTaskExecuter<T> {

	/**
	 * 构建验证签名通过后需要执行的业务回调
	 * 
	 * @param token    已验证签名的令牌
	 * @param provider 签名验证器
	 * @return 业务回调
	 * @throws Exception
	 */
	Callable<T> callback(SignatureToken token, SignatureProvider provider) throws Exception;

	/**
	 * 构建此业务支持的签名验证器
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	SignatureProvider signatureProvider(SignatureToken token) throws Exception;

	/**
	 * 申请签名直至成功，之后构建任务监听执行器
	 * 
	 * @param secret      用户自定义本次请求的密钥
	 * @param milliSecond 签名有效期（毫秒）
	 * @param data        需要加入签名的请求数据
	 * @return
	 * @throws Exception
	 */
	public default ListenableFutureTask<T> buildTask(String secret, int milliSecond, Map<String, Object> data)
			throws Exception {
		SignatureToken token = null;
		while (token == null) {
			try {
				token = apply(secret, milliSecond, data);
			} catch (Exception e) {
				Thread.sleep(1000l);
				token = null;
			}
		}
		return buildTask(secret, token.getSignature(), data);
	}

	/**
	 * 申请签名直至成功，之后构建任务回调
	 * 
	 * @param secret
	 * @param milliSecond
	 * @param data
	 * @return
	 */
	public default Callable<SignatureToken> buildSignatureTokenCallback(final String secret, final int milliSecond,
			Map<String, Object> data) {
		return () -> {
			SignatureToken token = null;
			while (token == null) {
				try {
					token = apply(secret, milliSecond, data);
				} catch (Exception e) {
					Thread.sleep(1000l);
					token = null;
				}
			}
			return token;
		};
	}

	/**
	 * 申请签名直至成功，之后构建任务回调
	 * 
	 * @param secret
	 * @param milliSecond
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public default Callable<T> buildTaskCallback(final String secret, final int milliSecond, Map<String, Object> data)
			throws Exception {
		SignatureToken token = null;
		while (token == null) {
			try {
				token = apply(secret, milliSecond, data);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				Thread.sleep(1000l);
				token = null;
			}
		}
		return buildTaskCallback(secret, token.getSignature(), data);
	}

	/**
	 * 
	 * 申请签名
	 * 
	 * @param secret      用户自定义本次请求的密钥
	 * @param milliSecond 签名有效期（毫秒）
	 * @param data        需要加入签名的请求数据
	 * @return
	 * @throws Exception
	 */
	public default SignatureToken apply(final String secret, final int milliSecond, Map<String, Object> data)
			throws Exception {
		SignatureToken token = new SignatureToken();
		token.setSecret(secret);
		token.setData(data);
		SignatureProvider provider = signatureProvider(token);
		token = provider.applySignatureLock(token, milliSecond);
		return token;
	}

	/**
	 * 构建获取签名监听任务
	 * 
	 * @param secret
	 * @param milliSecond
	 * @param data
	 * @return
	 */
	public default ListenableFutureTask<SignatureToken> buildSignatureTokenApplyTask(String secret, int milliSecond,
			Map<String, Object> data) {
		return new ListenableFutureTask<>(() -> {
			SignatureToken token = null;
			while (token == null) {
				try {
					token = apply(secret, milliSecond, data);
				} catch (Exception e) {
					token = null;
					Thread.sleep(200l);
				}
			}
			return token;
		});
	}

	public default Callable<T> buildTaskCallback(String secret, String signature, Map<String, Object> data)
			throws Exception {
		try {
			SignatureToken token = new SignatureToken();
			token.setSecret(secret);
			token.setData(data);
			token.setSignature(signature);

			SignatureProvider provider = signatureProvider(token);

			provider.verifyToken(token);

			provider.lockSignature();

			return callback(token, provider);
		} catch (InvalidParameterException | InvalidSignatureException e) {
			throw e;
		} catch (JwtException e) {
			throw e;
		} catch (SignatureNotFoundException e) {
			throw e;
		}
	}

	/**
	 * 执行请求
	 * 
	 * @param secret    用户申请签名时传递的密钥
	 * @param signature 用户申请签名时获取的签名
	 * @param data      用户申请签名时返回的参数
	 * @return
	 * @throws Exception
	 */
	public default ListenableFutureTask<T> buildTask(String secret, String signature, Map<String, Object> data)
			throws Exception {
		try {
			SignatureToken token = new SignatureToken();
			token.setSecret(secret);
			token.setData(data);
			token.setSignature(signature);

			SignatureProvider provider = signatureProvider(token);

			provider.verifyToken(token);

			provider.lockSignature();

			Callable<T> callback = callback(token, provider);
			ListenableFutureTask<T> future = new ListenableFutureTask<>(callback);

			return future;
		} catch (InvalidParameterException | InvalidSignatureException e) {
			throw e;
		} catch (JwtException e) {
			throw e;
		} catch (SignatureNotFoundException e) {
			throw e;
		}
	}

}
