package com.example.user.iotclassproject.data;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.AsyncTask;
import com.example.user.iotclassproject.BluetoothLeService;
import java.util.ArrayList;

/**
 * Created by YUAN on 2017/6/20.
 */

public class MyTask extends AsyncTask<ArrayList<ArrayList<BluetoothGattCharacteristic>>, Void, Void> {
    private BluetoothLeService mBluetoothLeService = new BluetoothLeService();

    @Override
    protected Void doInBackground(ArrayList<ArrayList<BluetoothGattCharacteristic>>... params) {
        int i = 0;
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = params[0];
        while (i < 10){
            mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2).setValue(i + "0123456789012345678");
            mBluetoothLeService.writeCharacteristic(mGattCharacteristics.get(mGattCharacteristics.size() -1).get(2));
        }
        return null;
    }
}
