package com.gdt.lianxuezhang.galaxydragontravel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by paulzhang on 17/11/2015.
 */
public class API {

    private ErrorHandler errorHandler;

    public API(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static final String BASEURL = "http://www.aoas.com.au/app/";

    public static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    public JSONObject httpGet(String url) {
        JSONObject object = null;
        HttpURLConnection urlConnection = null;
        try {
            URL urlString = new URL(BASEURL + url);
            urlConnection = (HttpURLConnection) urlString.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder result = new StringBuilder("");
            String content;
            while ((content = in.readLine()) != null) {
                result.append(content);
            }
            in.close();
            try {
                object = new JSONObject(result.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        if (object == null && this.errorHandler != null) {
            this.errorHandler.run();
        }
        return object;
    }

    public String getCsrfToken() {
        JSONObject object = httpGet("get_csrf_token");
        if (object == null && this.errorHandler != null) {
            this.errorHandler.run();
            return null;
        }

        String token = null;
        try {
            token = object.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return token;
    }

    public JSONObject httpPost(String url, String data) {
        JSONObject object = null;
        HttpURLConnection urlConnection = null;
        String token = getCsrfToken();

        CookieHandler.setDefault(cookieManager);

        try {
            URL urlString = new URL(BASEURL + url);
            if (!data.equals("")) {
                data += "&csrfmiddlewaretoken=" + token;
            } else {
                data = "csrfmiddlewaretoken=" + token;
            }

            urlConnection = (HttpURLConnection) urlString.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            urlConnection.setRequestProperty("X-CSRFToken", token);
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Cookie", "csrftoken=" + token);

            urlConnection.setUseCaches(false);
            urlConnection.setFixedLengthStreamingMode(data.length());
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(data);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder result = new StringBuilder("");
            String content;
            while ((content = in.readLine()) != null) {
                result.append(content);
            }
            in.close();
            try {
                object = new JSONObject(result.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        if (object == null && this.errorHandler != null) {

            this.errorHandler.run();
        }
        return object;
    }
}

interface ErrorHandler {
    void run();
}
