package nilenso.com.kulu_mobile2;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginClient {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private final String organizationNameKey = "team_name";
    private final String emailKey = "user_email";
    private final String passwordKey = "password";
    private final String credsKey = "creds";

    public LoginClient() {
        client = new OkHttpClient();
    }

    public void login(String url, String orgName, String email, String password, Callback callback) {
        Map<String, Object> requestMap = new HashMap();
        Map<String, Object> subRequestMap = new HashMap();

        subRequestMap.put(organizationNameKey, orgName);
        subRequestMap.put(emailKey, email);
        subRequestMap.put(passwordKey, password);

        requestMap.put(credsKey, subRequestMap);

        makeRequest(url, requestMap, callback);
    }

    private void makeRequest(String url, Map<String, Object> requestMap, Callback callback) {
        Gson gson = new Gson();
        String json = gson.toJson(requestMap);

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
