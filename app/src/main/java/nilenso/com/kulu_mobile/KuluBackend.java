package nilenso.com.kulu_mobile;


import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

public class KuluBackend {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;
    private final String idKey = "id";
    private final String requestKey = "storage_key";
    private final String remarksKey = "remarks";
    private final String expenseTypeKey = "expense_type";
    private final String dateKey = "date";
    private final String invoice = "invoice";

    public KuluBackend() {
        client = new OkHttpClient();
    }

    public String createInvoice(String url, String s3Location, ExpenseEntry expense) throws IOException {
        Map<String, Map<String, String>> requestMap = new HashMap<String, Map<String, String>>();
        Map<String, String> subRequestMap = new HashMap<String, String>();
        subRequestMap.put(idKey, expense.getId());
        subRequestMap.put(requestKey, FileUtils.getLastPartOfFile(s3Location));
        subRequestMap.put(remarksKey, expense.getComments());
        subRequestMap.put(expenseTypeKey, expense.getExpenseType());
        subRequestMap.put(dateKey , getDate(expense));
        requestMap.put(invoice, subRequestMap);


        return makeRequest(url, requestMap);
    }

    public String createInvoice(String url, ExpenseEntry expense) throws IOException {
        Map<String, Map<String, String>> requestMap = new HashMap<String, Map<String, String>>();
        Map<String, String> subRequestMap = new HashMap<String, String>();
        subRequestMap.put(idKey, expense.getId());
        subRequestMap.put(remarksKey, expense.getComments());
        subRequestMap.put(expenseTypeKey, expense.getExpenseType());
        subRequestMap.put(dateKey , getDate(expense));
        requestMap.put(invoice, subRequestMap);

        return makeRequest(url, requestMap);
    }

    private String getDate(ExpenseEntry expense) {
        return new DateTime(expense.getCreatedAt()).toString("yyyy-MM-dd");
    }

    private String makeRequest(String url, Map<String, Map<String, String>> requestMap) throws IOException {
        JSONObject json = new JSONObject(requestMap);

        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();
    }

}
