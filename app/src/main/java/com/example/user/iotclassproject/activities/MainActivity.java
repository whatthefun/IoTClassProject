package com.example.user.iotclassproject.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.user.iotclassproject.R;
import com.example.user.iotclassproject.data.MyAdapter;
import com.example.user.iotclassproject.data.Server;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements MyAdapter.ListItemClickListener {

    private final static String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 11;
    private static final int REQUEST_ENABLE_BT = 12;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDataList = new ArrayList<BluetoothDevice>();
    private boolean mScanning = false;
    private Handler mHandler;
    private Button btnScan;
    private MyAdapter adapter;
    private CallbackManager callbackManager;
    private Server server = new Server();
    private SharedPreferences sharedPreferences;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region GET TOKEN
        /*try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }*/
        //endregion

        //region is_BLE_usable
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        //endregion

        //region android 6.0 up 權限
        if (Build.VERSION.SDK_INT >= M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage(
                    "Please grant location access so this app can detect bluetooth. ");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                            PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
        //endregion

        //set scan button
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mScanning ^= true;
                if (mScanning) {
                    btnScan.setText("Stop");
                } else {
                    btnScan.setText("Scan");
                }
                scanLeDevice(mScanning);
            }
        });

        //set recyclerview
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter(mDataList, this);
        recyclerView.setAdapter(adapter);

        final BluetoothManager bluetoothManager =
            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mHandler = new Handler();

        //FB
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance()
            .registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "onLoginSuccess");
                    invalidateOptionsMenu();
                    server.login(loginResult.getAccessToken().getToken(), new Server.okHttpCallback() {
                        @Override public void onSuccess(JSONObject result) {
                            saveData(result);
                            sharedPreferences = getSharedPreferences("result", MODE_PRIVATE);
                            Log.d(TAG, "key: " + sharedPreferences.getString("private_key", null));
                            //如果還沒有金鑰
                            //if (sharedPreferences.getString("private_key", null) == null) {
                                Log.d(TAG, "key is null.");
                                String token = sharedPreferences.getString("access_token", null);
                                String token_type = sharedPreferences.getString("token_type", null);

                                //region save key
                                //server.generalKey(token, token_type, new Server.okHttpCallback() {
                                //    @Override public void onSuccess(JSONObject result) {
                                //        Log.d(TAG, "onSuccess: " + result);
                                //        try {
                                //            String private_key = result.getString("private_key");
                                //            private_key = private_key.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "");
                                //            private_key = private_key.replace("-----END ENCRYPTED PRIVATE KEY-----", "");
                                //
                                //            String public_key = result.getString("public_key");
                                //            public_key = public_key.replace("-----BEGIN RSA PUBLIC KEY-----", "");
                                //            public_key = public_key.replace("-----END RSA PUBLIC KEY-----", "");
                                //            sharedPreferences.edit()
                                //                .putString("private_key", private_key)
                                //                .putString("public_key", public_key)
                                //                .apply();
                                //            Log.d(TAG, "key: " + result.getString("private_key") + ", " + result.getString("public_key"));
                                //        } catch (JSONException e) {
                                //            Log.e(TAG, "onSaveKeySuccess: " + e.toString());
                                //        }
                                //    }
                                //});
                                //endregion

                                //region save username
                                server.getUsername(token, token_type, new Server.okHttpCallback() {
                                    @Override public void onSuccess(JSONObject result) {
                                        try {
                                            Log.d(TAG, "result: " + result);
                                            Log.d(TAG, "username: " + result.getString("username"));
                                            sharedPreferences.edit()
                                                .putString("username", result.getString("username"))
                                                .apply();
                                        } catch (JSONException e) {
                                            Log.e(TAG, "onGetUsernameSuccess: " + e.toString());
                                        }
                                    }
                                });
                                //endregion
                            //}
                        }
                    });


                }

                @Override public void onCancel() {}

                @Override public void onError(FacebookException error) {
                    Log.e(TAG, "onError: " + error.toString());
                }
            });
    }

    @Override protected void onResume() {
        super.onResume();

        requestEnableBLE();
    }

    @Override protected void onPause() {
        super.onPause();
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        mDataList.clear();
        adapter.notifyDataSetChanged();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);

        if (AccessToken.getCurrentAccessToken() != null) {
            menu.findItem(R.id.fb_login).setVisible(false);
            menu.findItem(R.id.fb_logout).setVisible(true);
        } else {
            menu.findItem(R.id.fb_login).setVisible(true);
            menu.findItem(R.id.fb_logout).setVisible(false);
        }
        invalidateOptionsMenu();
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fb_login:
                LoginManager.getInstance()
                    .logInWithReadPermissions(this,
                        Arrays.asList("user_status", "email", "user_friends"));
                invalidateOptionsMenu();
                return true;
            case R.id.fb_logout:
                LoginManager.getInstance().logOut();
                Log.d(TAG, "onOptionsItemSelected: logout");
                SharedPreferences preferences = getSharedPreferences("result", 0);
                preferences.edit()
                    .putString("username", "")
                    .apply();
                invalidateOptionsMenu();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //要求使用者打開藍芽
    private void requestEnableBLE() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    btnScan.setText("Scan");
                }
            }, SCAN_PERIOD);//10 sec 後關掉

            mScanning = true;
            mDataList.clear();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            btnScan.setText("Stop");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // TODO: 2017/05/05  1.將搜尋到的BLE放進List 
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        //Log.d("main", "callback");
                        //Log.d("callback", scanRecord.toString());
                        mDataList.add(device);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

    @Override public void onListItemClickListener(int position) {
        //Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
        BluetoothDevice device = mDataList.get(position);
        Intent intent = new Intent(this, BleInfoActivity.class);
        intent.putExtra("MAC", device.getAddress());
        intent.putExtra("Name", device.getName());
        //intent.putExtra("UUID", device.getUuids().toString());

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void saveData(JSONObject result) {
        sharedPreferences = getSharedPreferences("result", MODE_PRIVATE);
        try {
            sharedPreferences.edit()
                .putString("access_token", result.getString("access_token"))
                .putString("token_type", result.getString("token_type"))
                .apply();
        } catch (JSONException e) {
            Log.e(TAG, "saveData: " + e.toString());
        }
    }
}
