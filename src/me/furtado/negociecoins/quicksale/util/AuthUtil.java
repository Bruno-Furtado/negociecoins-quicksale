/*
 * MIT License
 *
 * Copyright (c) 2018 Bruno Tortato Furtado
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.furtado.negociecoins.quicksale.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import me.furtado.negociecoins.quicksale.Constants;

public final class AuthUtil {
	
	private static final String HMACSHA256_ALGORITHM = "HmacSHA256";
	private static final String MD5_ALGORITHM = "MD5";
	
	private AuthUtil() { }
	
	public static String amxHeader(final String apiUrl, 
			final String apiId, final String apiKey, 
			final String apiFunction, final String apiMethod, final String apiBody) 
					throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		
		final String url = URLEncoder.encode(apiUrl, Constants.CHARSET); // UnsupportedEncodingException
		final String method = apiMethod.toUpperCase();
		final String body = buildBody(apiBody); // NoSuchAlgorithmException
		final String time = Instant.now().getEpochSecond() + "";
		final String nonce = UUID.randomUUID().toString().replace("-", "");
		
		final String rawData = buildRawData(apiId, method, url, time, nonce, body);
		final String signature = buildSignature(rawData, apiKey); // InvalidKeyException
		
		return String.format("amx %s:%s:%s:%s", apiId, signature, nonce, time);
	}
	
	private static String buildSignature(final String data, final String apiKey) 
			throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
		
		final byte[] bytes = Base64Util.decodeToBase64(apiKey); // UnsupportedEncodingException
		final SecretKey secretKey = new SecretKeySpec(bytes, HMACSHA256_ALGORITHM);
		
		final Mac mac = Mac.getInstance(HMACSHA256_ALGORITHM); // NoSuchAlgorithmException
		mac.init(secretKey); // InvalidKeyException
		
		final byte[] macBytes = mac.doFinal(data.getBytes());
		return new String(Base64Util.encodeToBase64(macBytes)).trim();
	}
	
	private static String buildBody(final String apiBody) throws NoSuchAlgorithmException {
		if (apiBody == null || apiBody.length() == 0) {
			return "";
		}
		
		byte[] bytes = apiBody.getBytes();
		MessageDigest md5 = MessageDigest.getInstance(MD5_ALGORITHM); // NoSuchAlgorithmException
		
		return Base64Util.encodeToString(md5.digest(bytes));
	}

	private static String buildRawData(final String apiId, final String method, final String url, 
			final String time, final String nonce, final String body) {
		return String.format("%s%s%s%s%s%s", apiId, method, url, time, nonce, body);
	}
	
}
