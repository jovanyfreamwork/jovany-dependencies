package cn.jovany.ffmpeg.signature;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.binary.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public interface SignatureProvider {

	/**
	 * <h2>JWT加密算法 <small>默认: SignatureAlgorithm.HS256</small></h2>
	 * 
	 * @return JWT加密算法
	 */
	public default SignatureAlgorithm signatureAlgorithm() {
		return SignatureAlgorithm.HS256;
	}

	/**
	 * 申请签名锁
	 * 
	 * @param token       签名
	 * @param milliSecond 过期时间
	 * @return
	 * @throws IOException
	 * @throws LockedSignatureException 已上锁，请等待释放或过期
	 */
	public default SignatureToken applySignatureLock(SignatureToken token, int milliSecond)
			throws IOException, LockedSignatureException {
		String signature = encode(token, milliSecond);
		decode(token, signature).forEach((k, v) -> token.getData().put(k, v));
		if (existsSignature()) {
			byte[] signatureExist = readSignature();
			if (signatureExist != null && signatureExist.length != 0) {
				try {
					String signatureExistStr = new String(signatureExist);
					verify(token, signatureExistStr);
					throw new LockedSignatureException(token);
				} catch (ExpiredJwtException e) {
					unlock(token);
				} catch (JwtException e) {
				}
			}
		}
		if (isLocked()) {
			throw new LockedSignatureException(token);
		}
		writeSignature(signature.getBytes());
		return new SignatureToken(token, signature);
	}

	/**
	 * 上锁
	 * 
	 * @throws IOException
	 */
	void lockSignature() throws IOException;

	/**
	 * 开锁
	 * 
	 * @throws IOException
	 */
	void unlockSignature() throws IOException;

	/**
	 * 提前释放签名锁，未调用则等待签名过期
	 * 
	 * @param token
	 * @throws IOException
	 */
	public default void unlock(SignatureToken token) throws IOException {

		try {
			unlockSignature();
			deleteSignature();
		} catch (ExpiredJwtException e) {
		}
	}

	/**
	 * 判断签名是否存在
	 * 
	 * @return
	 */
	boolean existsSignature();

	/**
	 * 读取签名
	 * 
	 * @return
	 * @throws IOException
	 */
	byte[] readSignature() throws IOException;

	/**
	 * 保存签名
	 * 
	 * @param signature
	 * @throws IOException
	 */
	void writeSignature(byte[] signature) throws IOException;

	/**
	 * 删除签名
	 * 
	 * @throws IOException
	 */
	void deleteSignature() throws IOException;

	/**
	 * 产生Jwt签名
	 * 
	 * @param token
	 * @param milliSecond
	 * @return
	 */
	public default String encode(SignatureToken token, int milliSecond) {
		Calendar issuedAt = Calendar.getInstance();
		Calendar expiration = Calendar.getInstance();
		expiration.add(Calendar.MILLISECOND, milliSecond);
		return Jwts.builder().setClaims(token.getData()).setId(UUID.randomUUID().toString())
				.setIssuedAt(issuedAt.getTime()).setExpiration(expiration.getTime())
				.signWith(signatureAlgorithm(), signingKey(token)).compact();
	}

	/**
	 * 验证签名
	 * 
	 * @param token
	 * @param signature
	 * @return
	 * @throws ExpiredJwtException
	 * @throws UnsupportedJwtException
	 * @throws MalformedJwtException
	 * @throws SignatureException
	 * @throws IllegalArgumentException
	 */
	public default Jws<Claims> verify(SignatureToken token, String signature) throws ExpiredJwtException,
			UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
		return Jwts.parser().setSigningKey(signingKey(token)).parseClaimsJws(signature);
	}

	/**
	 * 验证并解读签名
	 * 
	 * @param token
	 * @param signature
	 * @return
	 * @throws ExpiredJwtException
	 * @throws UnsupportedJwtException
	 * @throws MalformedJwtException
	 * @throws SignatureException
	 * @throws IllegalArgumentException
	 */
	public default Claims decode(SignatureToken token, String signature) throws ExpiredJwtException,
			UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
		return verify(token, signature).getBody();
	}

	/**
	 * 获取密钥
	 * 
	 * @param event
	 * @return
	 */
	public default String signingKey(SignatureToken event) {
		return event.getSecret();
	}

	/**
	 * 验证令牌
	 * 
	 * @param token
	 * @throws IOException
	 * @throws SignatureNotFoundException
	 * @throws InvalidParameterException
	 * @throws InvalidSignatureException
	 */
	public default void verifyToken(SignatureToken token)
			throws IOException, SignatureNotFoundException, InvalidParameterException, InvalidSignatureException {
		if (!existsSignature()) {
			throw new SignatureNotFoundException();
		}

		String signature = new String(readSignature());

		Claims claims = null;
		try {
			claims = decode(token, new String(readSignature()));
		} catch (ExpiredJwtException e) {
			throw e;
		}

		if (!StringUtils.equals(token.getSignature(), signature)) {
			throw new InvalidSignatureException();
		}

		Set<String> fields = new HashSet<>();

		claims.forEach((k, v) -> {
			if (!Arrays.asList("iss", "sub", "aud", "exp", "nbf", "iat", "jti").contains(k)
					&& !v.equals(token.getData().get(k))) {
				fields.add(k);
			}
		});

		if (!fields.isEmpty()) {
			throw new InvalidParameterException(
					MessageFormat.format("the field value is incorrect: {0}", String.join(", ", fields)));
		}
	}

	boolean isLocked() throws IOException;

}
