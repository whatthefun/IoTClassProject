package com.example.user.iotclassproject.view;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.user.iotclassproject.BluetoothLeService;
import com.example.user.iotclassproject.R;
import com.example.user.iotclassproject.SampleGattAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by YUAN on 2017/04/25.
 */

public class BleInfoActivity extends AppCompatActivity {

    private final static String TAG = BleInfoActivity.class.getSimpleName();
    private TextView txtConnectState, txtUUID;
    private boolean mConnected = false;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
        new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private String mDeviceAddress;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleinfo);

        Intent intent = getIntent();

        mDeviceAddress = intent.getStringExtra("MAC");
        ((TextView) findViewById(R.id.txtMAC)).setText(mDeviceAddress);
        //String UUID = intent.getStringExtra("UUID");
        txtUUID =  (TextView) findViewById(R.id.txtUUID);
        txtConnectState =  (TextView) findViewById(R.id.txtConnectState);
        txtConnectState.setText(R.string.disconnected);

        if (intent.getStringExtra("Name") == null){
            getSupportActionBar().setTitle("NoNameDevice");
        }else {
            getSupportActionBar().setTitle(intent.getStringExtra("Name"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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
                invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.
                ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the
                // user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
            //else if (BluetoothLeService.ACTION_RSSI_CHANGE.equals(action)) {
            //    int rssi = intent.getIntExtra("Rssi", 0);
            //    txtConnectState.setText(rssi + "");
            //}
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        //String unknownServiceString = getResources().getString(R.string.unknown_service);
        //String unknownCharaString = getResources().getString(R.string.unknown_characteristic);onCharacteristicReadonCharacteristicReadonCharacteristicRead
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
            = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
//// TODO: 2017/04/29 here
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
            txtUUID.setText(uuid);


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
        //intentFilter.addAction(BluetoothLeService.ACTION_RSSI_CHANGE);
        return intentFilter;
    }
}
