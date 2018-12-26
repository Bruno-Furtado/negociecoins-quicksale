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

package me.furtado.negociecoins.quicksale.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import me.furtado.negociecoins.quicksale.Constants;
import me.furtado.negociecoins.quicksale.util.AuthUtil;

abstract class Service {

	private static final String BASE_URL = "https://broker.negociecoins.com.br/tradeapi/v1";
	private static final String USER_AGENT = "NegocieCoins-QuickSale";
	private static final String CONTENT_TYPE = "application/json; charset=" + Constants.CHARSET;
	private static final int TIMEOUT = 15000;
		
	protected static final String request(final String apiId, final String apiKey, 
			final String function, final String method, final String data) 
					throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		
		final URL url = buildUrl(function); // IOException
		final HttpsURLConnection connection = buildConnection(url, apiId, apiKey, function, method, data); // IOException, InvalidKeyException, NoSuchAlgorithmException
		
		if ("POST".equals(method.toUpperCase())) {
			buildPost(connection, data);
		}
		
		BufferedReader bufferedReader = null;
		final StringBuilder buffer = new StringBuilder();
		
		try {
			final InputStreamReader inputReader = new InputStreamReader(connection.getInputStream());
			bufferedReader = new BufferedReader(inputReader);
			
			final char[] chars = new char[1024];
			int read = 0;
			
			while ((read = bufferedReader.read(chars)) != -1) {
				buffer.append(chars, 0, read);
			}
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}

		return buffer.toString();
	}
	
	private static URL buildUrl(final String function) throws MalformedURLException {
		return new URL(BASE_URL + function);
	}
	
	private static HttpsURLConnection buildConnection(final URL url, final String apiId, final String apiKey, 
			final String function, final String method, final String data) 
					throws IOException, InvalidKeyException, NoSuchAlgorithmException {

		final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); // IOException
		
		final String header = AuthUtil.amxHeader(
				url.toString(), apiId, apiKey, function, method, data);  // InvalidKeyException, NoSuchAlgorithmException
		
		System.out.println("[me.furtado.negociecoins.quicksale.service.Service.buildConnection(70)]\n" + header + "\n");
		
		connection.setRequestProperty("Authorization", header);
		connection.addRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("Content-Type", CONTENT_TYPE);
		connection.setRequestMethod(method); // IOException
		connection.setReadTimeout(TIMEOUT);
		connection.setConnectTimeout(TIMEOUT);

		return connection;
	}
	
	private static void buildPost(final HttpsURLConnection connection, final String data) throws IOException {
		connection.setRequestProperty("Content-Length", Integer.toString(data.length()));
		connection.setRequestProperty("Content", data);
		
		try (OutputStream os = connection.getOutputStream()) { // IOException
			os.write(data.getBytes(Constants.CHARSET));
		}
	}
	
}
