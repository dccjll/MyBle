package com.paiai.mble.helper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.paiai.mble.util.LogUtils;

/**
 * 作者：云渡山<br>
 * 创建时间：2018/2/11 15 30 星期日<br>
 * 功能描述：ble开关管理器<br>
 */
public class BLESwitchHelper {

    private static final String TAG = "BLESwitchHelper";
    public static final int REQUESTCODE_ENABLE_BT = 0x900001;
    private boolean currentStatus;
    private BluetoothAdapter bluetoothAdapter;

    private Activity activity;
    private boolean switcher;
    private OnBLESwitchListener onBLESwitchListener;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //获取蓝牙设备实例【如果无设备链接会返回null，如果在无实例的状态下调用了实例的方法，会报空指针异常】
            //主要与蓝牙设备有关系
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
             if (BluetoothAdapter.ACTION_STATE_CHANGED.equalsIgnoreCase(action)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_OFF) {
//                    Toast.makeText(context, "蓝牙关闭", Toast.LENGTH_SHORT).show();
                    if (currentStatus && !switcher) {//要执行关闭，关闭成功后返回
                        activity.unregisterReceiver(broadcastReceiver);
                        onBLESwitchListener.disableSuccess();
                    }
                } else if (blueState == BluetoothAdapter.STATE_ON) {
//                    Toast.makeText(context, "蓝牙开启", Toast.LENGTH_SHORT).show();
//                    if (switcher) {//要开启，开启成功后返回
//                        onBLESwitchListener.enableSuccess();
//                    }
                }
            }
        }
    };

    /**
     *  默认执行开启ble操作
     */
    public BLESwitchHelper(Activity activity, OnBLESwitchListener onBLESwitchListener) {
        this(activity, true, onBLESwitchListener);
    }

    public BLESwitchHelper(Activity activity, boolean switcher, OnBLESwitchListener onBLESwitchListener) {
        this.activity = activity;
        this.switcher = switcher;
        this.onBLESwitchListener = onBLESwitchListener;
    }

    public void doSwitch() {
        bluetoothAdapter = BLEHelper.getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            onBLESwitchListener.switchFail(switcher, -10002);
            return;
        }
        currentStatus = bluetoothAdapter.isEnabled();
        if (switcher) {//要执行开启蓝牙操作
            if (!currentStatus) {//但是蓝牙未开启
                //请求开启蓝牙
                enableBT();
                return;
            }
            //蓝牙已开启，直接返回开启成功
            onBLESwitchListener.enableSuccess();
            return;
        }
        //要执行关闭蓝牙操作
        if (currentStatus) {//但是蓝牙是开启的
            //请求关闭蓝牙
            disableBT();
            return;
        }
        //蓝牙已关闭，直接返回关闭成功
        onBLESwitchListener.disableSuccess();
    }

    private void enableBT() {
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBTIntent, REQUESTCODE_ENABLE_BT);
    }

    private void disableBT() {
        registerFilter();
        bluetoothAdapter.disable();
    }

    private void registerFilter() {
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "requestCode=" + requestCode + "\nresultCode=" + requestCode + "\ndata=" + data);
        if (requestCode == REQUESTCODE_ENABLE_BT) {
            if (!bluetoothAdapter.isEnabled()) {
                onBLESwitchListener.switchFail(switcher, -10049);
                return;
            }
            onBLESwitchListener.enableSuccess();
        }
    }

    /**
     * 作者：云渡山<br>
     * 创建时间：2018/2/11 15 45 星期日<br>
     * 功能描述：ble开关监听器<br>
     */
    public interface OnBLESwitchListener {
        /**
         * 蓝牙开启成功
         */
        void enableSuccess();

        /**
         * 蓝牙关闭成功
         */
        void disableSuccess();

        /**
         * 蓝牙开启失败或关闭失败
         */
        void switchFail(Boolean todoFlag, int errorCode);
    }
}
