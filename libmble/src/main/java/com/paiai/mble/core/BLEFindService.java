package com.paiai.mble.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.paiai.mble.BLEConfig;
import com.paiai.mble.BLEGattCallback;
import com.paiai.mble.BLEManage;
import com.paiai.mble.util.LogUtils;

import java.util.List;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 13:28<br>
 * 功能描述：寻找服务<br>
 */
public class BLEFindService {

    private final static String TAG = BLEFindService.class.getSimpleName();
    private BLEManage bleManage;

    /**
     * gatt服务器找服务监听器
     */
    public interface OnGattBLEFindServiceListener{
        void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices);
        void onFindServiceFail(String errorMsg, int loglevel);
    }

    /**
     * 找服务
     */
    public BLEFindService(BLEManage bleManage) {
        this.bleManage = bleManage;
    }

    /**
     * 开始找服务
     */
    public void findService(){
        LogUtils.i(TAG, "准备开始找服务");
        if(bleManage.getBluetoothGatt() == null){
            bleManage.handleError(-10021);
            return;
        }
        if(bleManage.getBleGattCallback() == null){
            bleManage.handleError(-10022);
            return;
        }
        BluetoothAdapter bluetoothAdapter = bleManage.getBluetoothAdapter();
        if(bluetoothAdapter == null){
            bleManage.handleError(-10002);
            return;
        }
        bleManage.getBleGattCallback().registerOnGattBLEFindServiceListener(
                new OnGattBLEFindServiceListener() {
                    @Override
                    public void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices) {
                        afterFindServiceSuccess(bluetoothGatt, status, bluetoothGattServices);
                    }

                    @Override
                    public void onFindServiceFail(String errorMsg, int loglevel) {
                        LogUtils.i(TAG, "找服务失败\nerrorMsg=" + errorMsg + "\nloglevel=" + LogUtils.getLogTag(loglevel));
                        bleManage.getBleConnect().connect();
                    }
                }
        );
        if (!bleManage.getRunning()) {
            LogUtils.i(TAG, "任务已停止");
            return;
        }
        LogUtils.i(TAG, "开始找服务");
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (!bleManage.getBluetoothGatt().discoverServices()) {
                    bleManage.getBleConnect().connect();
                }
            }
        });
    }

    private void afterFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices) {
        LogUtils.i(TAG, "已找到服务");
        if (bluetoothGattServices == null || bluetoothGattServices.size() == 0) {
            LogUtils.e(TAG, "服务列表为空");
            bleManage.getBleConnect().connect();
            return;
        }
        //遍历服务
        for(BluetoothGattService bluetoothGattService : bluetoothGattServices){
            LogUtils.i(TAG, "++++service uuid:" + bluetoothGattService.getUuid());
            for(BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()){
                LogUtils.i(TAG, "--------characteristics uuid:" + bluetoothGattCharacteristic.getUuid());
                for(BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattCharacteristic.getDescriptors()){
                    LogUtils.i(TAG, "------------descriptor uuid:" + bluetoothGattDescriptor.getUuid());
                }
            }
        }
        if(bleManage.getListenterObject() instanceof OnBLEFindServiceListener){
            ((OnBLEFindServiceListener)bleManage.getListenterObject()).onFindServiceSuccess(bluetoothGatt, status, bluetoothGattServices, bleManage.getBleGattCallback());
            return;
        }
        if(bleManage.getNeedOpenNotification()){
            if (BLEConfig.START_OPEN_NOTIFICATION_INTERVAL > 0) {
                LogUtils.i(TAG, "找服务成功,休眠" + BLEConfig.START_OPEN_NOTIFICATION_INTERVAL + "ms开始打开通知,当前手机型号:" + Build.MODEL);
                SystemClock.sleep(BLEConfig.START_OPEN_NOTIFICATION_INTERVAL);
                if (!bleManage.getRunning()) {
                    LogUtils.e(TAG, "准备打开通知时任务已停止");
                    return;
                }
            }
            BLEOpenNotification bleOpenNotification = new BLEOpenNotification(bleManage);
            bleOpenNotification.openNotification();
            return;
        }
        BLEWriteData bleWriteData = new BLEWriteData(bleManage);
        bleWriteData.writeData();
    }

    /**
     * 作者：dccjll<br>
     * 创建时间：2017/11/6 11:30<br>
     * 功能描述：找服务监听<br>
     */
    public interface OnBLEFindServiceListener {
        void onFindServiceSuccess(BluetoothGatt bluetoothGatt, int status, List<BluetoothGattService> bluetoothGattServices, BLEGattCallback bleGattCallback);
        void onFindServiceFail(int errorCode);
    }
}