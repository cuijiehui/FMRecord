package com.khmer.fm.adnroid_recordd.record;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.khmer.fm.adnroid_recordd.Constants;


public class HeadSetReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("BroadcastReceiver","action= "+ action);
        if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (BluetoothProfile.STATE_DISCONNECTED == adapter.getProfileConnectionState(BluetoothProfile.HEADSET)) {
                //Bluetooth headset is now disconnected
            }
        } else if ("android.intent.action.HEADSET_PLUG".equals(action)) {

            if (intent.hasExtra("state")){
                if (intent.getIntExtra("state", 0) == 0){
//                    Toast.makeText(context, "headset not connected", Toast.LENGTH_LONG).show();
                   Constants.isHeadSet =true;
                }
                else if (intent.getIntExtra("state", 0) == 1){
//                    Toast.makeText(context, "headset connected", Toast.LENGTH_LONG).show();
                    Constants.isHeadSet =false;

                }
            }
        }
    }
}
