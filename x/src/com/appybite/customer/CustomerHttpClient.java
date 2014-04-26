package com.appybite.customer;

import java.util.ArrayList;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

public class CustomerHttpClient {

	private static final String BASE_URL = "https://www.appyorder.com/pro_version/webservice_smart_app/";

	public static AsyncHttpClient client = new AsyncHttpClient();
	private static ArrayList<RequestHandle> handleArray = new ArrayList<RequestHandle>();

	public static void stop() {
		for (int i = 0; i < handleArray.size(); i++) {
			
			handleArray.get(i).cancel(true);
		}
		
		handleArray.clear();
	}

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if (params == null)
			Log.i("HTTP GET >>>", getAbsoluteUrl(url));
		else
			Log.i("HTTP GET >>>", getAbsoluteUrl(url) + "?" + params.toString());
		RequestHandle handle = client.get(getAbsoluteUrl(url), params,responseHandler);
		handleArray.add(handle);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

		if (params == null)
			Log.i("HTTP POST >>>", getAbsoluteUrl(url));
		else
			Log.i("HTTP POST >>>",getAbsoluteUrl(url) + "\ndata: " + params.toString());
		RequestHandle handle = client.post(getAbsoluteUrl(url), params, responseHandler);
		handleArray.add(handle);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}

	public static void getFromFullService(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		if (params == null)
			Log.i("HTTP GET >>>", url);
		else
			Log.i("HTTP GET >>>", url + "?" + params.toString());
		RequestHandle handle = client.get(url, params, responseHandler);
		handleArray.add(handle);
	}
}