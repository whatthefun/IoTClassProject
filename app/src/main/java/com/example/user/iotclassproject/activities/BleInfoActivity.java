package com.example.user.iotclassproject.activities;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.user.iotclassproject.BluetoothLeService;
import com.example.user.iotclassproject.MyDialog;
import com.example.user.iotclassproject.R;
import com.example.user.iotclassproject.data.MyRSA;
import com.example.user.iotclassproject.data.MyTask;
import com.example.user.iotclassproject.data.SampleGattAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.user.iotclassproject.BluetoothLeService.EXTRA_DATA;

/**
 * Created by YUAN on 2017/04/25.
 */

public class BleInfoActivity extends AppCompatActivity implements MyDialog.DialogListener {

    private final static String TAG = BleInfoActivity.class.getSimpleName();
    private TextView txtConnectState;
    private ImageButton btnOpener, imgBtnOwner;
    private FloatingActionButton fab;
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService = new BluetoothLeService();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
        new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private String mDeviceAddress;
    private int isOn = 0;
    private MyRSA RSA = new MyRSA();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleinfo);

        Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra("MAC");
        //((TextView) findViewById(R.id.txtMAC)).setText(mDeviceAddress);
        ////String UUID = intent.getStringExtra("UUID");
        //txtUUID =  (TextView) findViewById(R.id.txtUUID);
        txtConnectState =  (TextView) findViewById(R.id.txtConnectState);
        txtConnectState.setText(R.string.disconnected);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //SharedPreferences preferences = getSharedPreferences("result", MODE_PRIVATE);
                //String private_key = preferences.getString("private_key", "");
                //String public_key = preferences.getString("public_key", "");
                //Log.d(TAG, "private_key: " + private_key);
                //Log.d(TAG, "public_key: " + public_key);
                //
                //String data = "0";
                //try {
                //    byte[] base64_data = Base64.encode(data.getBytes(), 0);
                //    byte[] base64_privateKey = Base64.decode(private_key, Base64.DEFAULT);
                //    byte[] secret = RSA.encrypt(base64_data, base64_privateKey);
                //    Log.d(TAG, "Length: " + secret.length);
                //
                //    byte[] base64_publicKey = Base64.decode(public_key, Base64.DEFAULT);
                //    byte[] bContext = RSA.decrypt(secret, base64_publicKey);
                //
                //    byte[] encoded = Base64.decode(bContext, 0);
                //    String printMe = new String(encoded, "UTF-8");
                //    Log.d(TAG, "plain: " + printMe);
                //} catch (Exception e) {
                //    e.printStackTrace();
                //}
                MyDialog dialog = new MyDialog();
                dialog.show(getFragmentManager(), TAG);
            }
        });

        if (intent.getStringExtra("Name") == null){
            getSupportActionBar().setTitle("NoNameDevice");
        }else {
            getSupportActionBar().setTitle(intent.getStringExtra("Name"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        btnOpener = (ImageButton) findViewById(R.id.btnOpener);
        btnOpener.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int i = (isOn == 0) ? 1:0;
                setSwitch(i);

                //integer to byte[]
                byte b[] = new byte[4];

                b[0] = (byte)( (i & 0xff000000) >>> 24);
                b[1] = (byte)( (i & 0x00ff0000) >>> 16);
                b[2] = (byte)( (i & 0x0000ff00) >>> 8);
                b[3] = (byte)( (i & 0x000000ff) );
                isOn = i;

                mGattCharacteristics.get(mGattCharacteristics.size() -1).get(0).setValue(b);
                mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(0));

            }
        });

        imgBtnOwner = (ImageButton) findViewById(R.id.imgBtnOwner);
        imgBtnOwner.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("result", MODE_PRIVATE);
                String public_key = preferences.getString("public_key", "");
                Log.d(TAG, "length: " + public_key.length());
                String owner = preferences.getString("owner", "");
                String key1 = "*" + public_key.substring(0,14);
                String key2 = public_key.substring(15) + "*";

                MyTask task = new MyTask();
                task.execute(mGattCharacteristics);

                //int i = 0;
                //while (i < 10){
                //    mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2).setValue(i + "0123456789012345678");
                //    mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2));
                //}

                //mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2).setValue(key1);
                //mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2));

                //mGattCharacteristics.get(mGattCharacteristics.size() -1).get(1).setValue(owner);
                //mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(1));
                //
                //mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2).setValue(key2);
                //mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2));
            }
        });

    }

    @Override protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        Log.d(TAG, "onCreateOptionsMenu");
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    //連 or 斷線控制
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.MyBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//已連線
                mConnected = true;
                txtConnectState.setText(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {//已斷線
                mConnected = false;
                txtConnectState.setText(R.string.disconnected);
                mBluetoothLeService.close();
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.
                ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //抓周邊目前資料
                String tmp = (String)intent.getExtras().get(EXTRA_DATA);
                try {
                    setSwitch(Integer.valueOf(tmp.substring(2, tmp.length()-1)));
                }catch (Exception e){
                    Log.e(TAG, e.toString());
                }
            }else if (BluetoothLeService.ACTION_BONDED.equals(action)){
                mBluetoothLeService.connect(mDeviceAddress);
            }
        }
    };

    private void setSwitch(int isOn){
        Log.d("setSwitch", isOn + "");
        if (isOn == 0){
            btnOpener.setImageResource(R.drawable.stop);
        }else if(isOn == 1){
            btnOpener.setImageResource(R.drawable.ok);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        //存資料
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
            = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put("Name", SampleGattAttributes.lookup(uuid));
            currentServiceData.put("UUID", uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                    "Name", SampleGattAttributes.lookup(uuid));
                currentCharaData.put("UUID", uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }

        final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(2).get(0);
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            mBluetoothLeService.readCharacteristic(characteristic);
        }

        //SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
        //    this,
        //    gattServiceData,
        //    android.R.layout.simple_expandable_list_item_2,
        //    new String[] {"Name", "UUID"},
        //    new int[] { android.R.id.text1, android.R.id.text2 },
        //    gattCharacteristicData,
        //    android.R.layout.simple_expandable_list_item_2,
        //    new String[] {"NAME", "UUID"},
        //    new int[] { android.R.id.text1, android.R.id.text2 }
        //);
        //mGattServicesList.setAdapter(gattServiceAdapter);
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_BONDED);
        return intentFilter;
    }

    @Override public void onDialogPositiveClick(String username) {

    }
}
