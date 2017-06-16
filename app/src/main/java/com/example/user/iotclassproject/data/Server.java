package com.example.user.iotclassproject.data;

import android.util.Log;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by YUAN on 2017/6/8.
 */

public class Server {
    private static final String TAG = "Server";
    private static String Content_Type = "application/json";
    private static final String AUTH_ADDRESS = "http://web.daychen.tw/auth/convert-token";
    private static final String DEVICE_ADDRESS = "http://web.daychen.tw/docs/#/device";
    private static final String DOC_ADDRESS = "http://web.daychen.tw/docs/";
    private static final String KEY_ADDRESS = "http://web.daychen.tw/device/key/";
    private static final String USERNAME_ADDRESS = "http://web.daychen.tw/device/users/get_username/";
    private static final String GRANT_TYPE = "convert_token";
    private static final String CLIENT_ID = "kPDTHhFwmLmJ2uJaqf6G1HSuDnVyekFUse5596FV";
    private static final String CLIENT_SECRET = "ipL0cIbuPfAcUAXgPsFsyZinWVOLdY0VhO2PujBr3bkoqvF4U96GC5RSVDWKbIvAzGdqj3jqQQ1TfRuiOrjQY0Y7bDg9JjmGaaWqR1Ev2agL3QkGX8T7oXxAcj3iGnY5";
    private static final String BACKEND = "facebook";
    //private JSONObject result = null;


    public void login(String token, final okHttpCallback callback){
        final OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(AUTH_ADDRESS);
        FormBody.Builder params = new FormBody.Builder();
        params.add("grant_type", GRANT_TYPE);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("backend", BACKEND);
        params.add("token", token);
        FormBody formBody = params.build();

        builder.method("POST", formBody);
        final Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onLoginFailure: " + e.toString());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    Log.d(TAG, "onLoginResponse: Successful");
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: " + e.toString());
                    }
                }
                response.body().close();
            }
        });
    }

    public void generalKey(String token, String token_type , final okHttpCallback callback){
        if (token == null || token_type == null){
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(KEY_ADDRESS + "generate_private_key/");
        requestBuilder.method("GET", null);
        requestBuilder.addHeader("Authorization", token_type + " " + token);

        final Request request = requestBuilder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGeneralKeyFailure: " + e.toString());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onGeneralKeyResponse: " + response.toString());
                Log.d(TAG, "onGeneralKeyResponse: " + response.body().string());
                if (response.isSuccessful()){
                    Log.d(TAG, "onLoginResponse: Successful");
                    try {
                         JSONObject result = new JSONObject(response.body().string());
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: " + e.toString());
                    }catch (Exception e){
                        Log.e(TAG, "onResponse: " + e.toString());
                    }
                }
                response.body().close();
            }
        });
    }

    public void getUsername(String token, String token_type, final okHttpCallback callback){
        if (token == null || token_type == null){
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder().url(USERNAME_ADDRESS);
        requestBuilder.method("GET", null);
        requestBuilder.addHeader("Authorization", token_type + " " + token);

        Request request = requestBuilder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGetUsernameFailure: " + e.toString());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onGetUsernameResponse: " + response.toString());
                if (response.isSuccessful()){
                    Log.d(TAG, "onLoginResponse: Successful");
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "onResponse: " + e.toString());
                    }
                }
                response.body().close();
            }
        });
    }

    public void getPublicKey(String token, String token_type, String username, final okHttpCallback callback){
        if (token == null || token_type == null){
            return;
        }
        final OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(AUTH_ADDRESS);
        FormBody.Builder params = new FormBody.Builder();
        params.add("username", username);
        FormBody formBody = params.build();

        builder.method("POST", formBody);
        final Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGetPublicKeyFailure: " + e.toString());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){
                    Log.d(TAG, "onGetPublicKeyResponse: Successful");
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        callback.onSuccess(result);
                    } catch (JSONException e) {
                        Log.e(TAG, "onGetPublicKeyResponse: " + e.toString());
                    }
                }
                response.body().close();
            }
        });
    }

    public interface okHttpCallback{
        void onSuccess(JSONObject result);
    }
}
