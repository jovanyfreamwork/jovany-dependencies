package cn.jovany.ffmpeg.signature;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SignatureToken {

	/**
	 * 签名令牌密钥
	 */
//	@JsonIgnore
	private String secret;

	/**
	 * 签名
	 */
	private String signature;

	/**
	 * 请求参数
	 */
	private Map<String, Object> data;

	/**
	 * 环境参数
	 */
	private Map<String, Object> env;

	/**
	 * 申请令牌时调用的构造器
	 * 
	 * @param secret 请求密钥
	 * @param data   请求参数
	 */
	public SignatureToken(String secret, Map<String, Object> data) {
		this(secret, data, new LinkedHashMap<>());
	}

	/**
	 * 申请令牌时调用的构造器
	 * 
	 * @param secret 请求密钥
	 * @param data   请求参数
	 * @param env    环境变量
	 */
	protected SignatureToken(String secret, Map<String, Object> data, Map<String, Object> env) {
		super();
		this.secret = secret;
		this.data = data;
		this.env = env;
	}

	/**
	 * 获取到签名时调用的构造器
	 * 
	 * @param request
	 * @param signature
	 */
	public SignatureToken(SignatureToken request, String signature) {
		this(request.secret, request.data, request.env);
		setSignature(signature);
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSecret() {
		return secret;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Map<String, Object> getEnv() {
		return env;
	}

	public void setEnv(Map<String, Object> env) {
		this.env = env;
	}

}
