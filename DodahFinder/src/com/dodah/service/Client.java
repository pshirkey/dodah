package com.dodah.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Client {

	private final String SERVER_URL = "http://10.0.2.2:8081/service/";
	private final String client_key = "B902A730A4D211DFA5C53432E0D72085";
	private String access_token = "";
	private String errorMessage = "";

	private final String RESULT = "result";
	private final String SUCCESS = "success";
	private final String RESPONSE = "response";
	private final String STATUS = "status";
	private final String ERROR_MESSAGE = "error_message";

	/**
	 * default ctor
	 */
	public Client() {

	}

	/**
	 * ctor
	 */
	public Client(String access_token) {
		this.setAccess_token(access_token);
	}

	/**
	 * access_token getter
	 * 
	 * @return string
	 */
	public String getAccess_token() {
		return access_token;
	}

	/**
	 * gets the error message for the last call
	 * 
	 * @return string
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * access_token setter
	 * 
	 * @param access_token
	 *            the value for the access token
	 */
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	/**
	 * authenticates the current user
	 * 
	 * @param email
	 *            email address for user
	 * @param clearTextPassword
	 *            teh password in clear text
	 * @return the access_token for this user
	 */
	public String Authenticate(String email, String clearTextPassword) {
		this.access_token = "";
		this.errorMessage = "";
		if (email != null && email.length() > 0 && clearTextPassword != null
				&& clearTextPassword.length() > 0) {
			String md5Password = this.md5(clearTextPassword);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("service_client", client_key);
			params.put("email", email);
			params.put("password", md5Password);
			JSONObject result = this.Call("authenticate", params);
			JSONObject response = this.getReponse(result);
			if (response != null) {
				try {
					this.access_token = response.getString("access_token");
				} catch (JSONException e) {
					Log.i("DoDahService", e.getMessage());
					this.access_token = "";
				}
			}
		}
		return this.access_token;
	}

	/**
	 * Makes the call to the server, returns the json array from the server
	 * 
	 * @param method
	 *            method name to call on the server
	 * @param parameters
	 *            hashmap of paramters
	 * @return JSONArray
	 */
	private JSONObject Call(String method, HashMap<String, String> parameters) {
		String callUrl = this.BuildMethodCall(method, parameters);
		if (callUrl != "") {
			HttpClient httpclient = new DefaultHttpClient();

			// Prepare a request object
			HttpPost httpget = new HttpPost(callUrl);
			HttpResponse response;
			try {
				response = httpclient.execute(httpget);
				// Examine the response status
				Log.i("DoDahService", response.getStatusLine().toString());

				// Get hold of the response entity
				HttpEntity entity = response.getEntity();
				// If the response does not enclose an entity, there is no need
				// to worry about connection release

				if (entity != null) {

					// A Simple JSON Response Read
					InputStream instream = entity.getContent();
					String result = convertStreamToString(instream);
					// Closing the input stream will trigger connection release
					instream.close();

					Log.i("DoDahService", result);

					JSONObject json = new JSONObject(result);
					return json;
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Builds the full url to the server with the parameters added
	 * 
	 * @param method
	 *            the name of the method to call
	 * @param parameters
	 *            the parameters to call it with
	 * @return the full usrl string or ""
	 */
	private String BuildMethodCall(String method,
			HashMap<String, String> parameters) {
		if (method != null && method.length() > 0) {
			StringBuilder fullUrl = new StringBuilder(SERVER_URL);
			fullUrl.append(method);
			fullUrl.append("?client_key=");
			fullUrl.append(client_key);
			if (parameters != null && !parameters.isEmpty()) {
				Iterator<Entry<String, String>> iter = parameters.entrySet()
						.iterator();
				while (iter.hasNext()) {
					Map.Entry pairs = iter.next();
					fullUrl.append("&");
					fullUrl.append(pairs.getKey());
					fullUrl.append("=");
					fullUrl.append(pairs.getValue());
				}
			}
			return fullUrl.toString();
		}
		return "";
	}

	/**
	 * Converts the inputstream to a string for json parsing
	 * 
	 * @param is
	 * @return json string
	 */
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * generates the md5 version of a string
	 * 
	 * @param input
	 *            the string input
	 * @return md5 string
	 */
	private String md5(String input) {
		String res = "";
		try {
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(input.getBytes());
			byte[] md5 = algorithm.digest();
			String tmp = "";
			for (int i = 0; i < md5.length; i++) {
				tmp = (Integer.toHexString(0xFF & md5[i]));
				if (tmp.length() == 1) {
					res += "0" + tmp;
				} else {
					res += tmp;
				}
			}
		} catch (NoSuchAlgorithmException ex) {
		}
		return res;
	}

	/**
	 * pulls the response object out and sets the error message if there is one
	 * 
	 * @param callResult
	 *            the return from the call
	 * @return JSONObject
	 */
	private JSONObject getReponse(JSONObject callResult) {
		this.errorMessage = "";
		try {
			if (callResult != null) {
				JSONObject res = callResult.getJSONObject(RESULT);
				if (res != null) {
					if (res.getString(STATUS).equals(SUCCESS)) {
						return res.getJSONObject(RESPONSE);
					} else {
						this.errorMessage = res.getString(ERROR_MESSAGE);
					}
				}
			}
		} catch (JSONException e) {
			Log.i("DoDahService", e.getMessage());
		}
		return null;
	}
}