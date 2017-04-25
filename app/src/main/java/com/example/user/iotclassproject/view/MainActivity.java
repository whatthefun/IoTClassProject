package com.example.user.iotclassproject.view;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.user.iotclassproject.R;
import com.example.user.iotclassproject.data.MyAdapter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MyAdapter.ListItemClickListener{

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 11;
    private static final int REQUEST_ENABLE_BT = 12;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDataList = new ArrayList<BluetoothDevice>();
    private boolean mScanning = false;
    private Handler mHandler;
    private Button btnScan;
    private MyAdapter adapter;
    private BluetoothGatt mBluetoothGatt;
    

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // android 6.0 up 權限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect bluetooth. ");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener(){
                    @Override
                    public void onDismiss(DialogInterface dialog){
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        //set scan button
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mScanning ^= true;
                if (mScanning){
                    btnScan.setText("Stop");
                }else {
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

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        requestEnableBLE();
        mHandler = new Handler();
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

    //要求使用者打開藍芽
    private void requestEnableBLE(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // TODO: 2017/04/23 收到藍芽關掉的廣播也要把scan關掉 

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
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

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi,
                final byte[] scanRecord) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("main", "callback");
                        Log.d("callback", scanRecord.toString());
                        mDataList.add(device);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

    @Override public void onListItemClickListener(int position) {
        Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, BleInfoActivity.class);
        intent.putExtra("data", mDataList.get(position));
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }
}
