package com.example.user.iotclassproject;

import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by YUAN on 2017/6/8.
 */

public class Server {
    private static final String TAG = "Server";
    private static String Content_Type = "application/json";
    private static final String ADDRESS = "http://web.daychen.tw/auth/convert-token";
    private static final String GRANT_TYPE = "convert_token";
    private static final String CLIENT_ID = "kPDTHhFwmLmJ2uJaqf6G1HSuDnVyekFUse5596FV";
    private static final String CLIENT_SECRET = "ipL0cIbuPfAcUAXgPsFsyZinWVOLdY0VhO2PujBr3bkoqvF4U96GC5RSVDWKbIvAzGdqj3jqQQ1TfRuiOrjQY0Y7bDg9JjmGaaWqR1Ev2agL3QkGX8T7oXxAcj3iGnY5";
    private static final String BACKEND = "facebook";

    public void login(String token){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(ADDRESS);

        FormBody.Builder params = new FormBody.Builder();
        params.add("grant_type", GRANT_TYPE);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("backend", BACKEND);
        params.add("token", token);
        FormBody formBody = params.build();

        builder.method("POST", formBody);
        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: " + e.toString());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    Log.d(TAG, "onResponse: Successful");
                }
            }
        });
    }
}
