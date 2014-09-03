package nilenso.com.kulu_mobile;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class KuluBackend {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    public KuluBackend() {
        client = new OkHttpClient();
    }

    public String createInvoice(String url, String s3Location) throws IOException {
        Map<String, String> requestMap = new HashMap<String, String>();

        requestMap.put("storage_key", s3Location);
        JSONObject json = new JSONObject(requestMap);

        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }
}
