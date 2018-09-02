package com.paiai.mble.helper;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.blankj.subutil.util.Utils;
import com.paiai.mble.R;
import com.paiai.mble.util.LocationUtils;
import com.paiai.mble.util.LogUtils;
import com.paiai.mble.util.PermissionUtils;

import org.jetbrains.annotations.NotNull;

/**
 * 作者：dccjll<br>
 * 创建时间：2017/11/6 11:29<br>
 * 功能描述：蓝牙工具类<br>
 */
public class BLEHelper {

    private static final String TAG = "BLEHelper";
    public static final int LOCATION_PERMISSION_DENIED = 0x0001;//定位权限被阻止
    public static final int LOCATION_FEATURE_DENIED = 0x0002;//定位功能被阻止
    public static final int CANCEL_OPEN_LOCATION_SETTING_ACTIVITY = 0x0003;//取消打开定位设置界面
    private static SparseArray<String> errorMap = new SparseArray<>();
    private static BLEHelper sBleHelper;
    private Object locationPermissionAndFeatureCheckListenerObject;

    static {
        errorMap.put(LOCATION_PERMISSION_DENIED, Utils.getApp().getString(R.string.base_location_permission_denied));
        errorMap.put(LOCATION_FEATURE_DENIED, Utils.getApp().getString(R.string.mble_location_feature_denied));
        errorMap.put(CANCEL_OPEN_LOCATION_SETTING_ACTIVITY, Utils.getApp().getString(R.string.mble_cancel_open_location_setting));
    }

    public static String parseBLEHelperErrorCode(int errorCode) {
        return errorMap.get(errorCode);
    }

    /**
     * 验证系统是否支持低功耗蓝牙
     * @return true 支持 false 不支持
     */
    public static boolean hasBLEFeature() {
        return Utils.getApp().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 获取本地的蓝牙管理服务
     * @return 本地的蓝牙管理服务
     */
    public static BluetoothManager getBluetoothManager() {
        return (BluetoothManager) Utils.getApp().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /**
     * 获取本地的蓝牙适配器
     * @return 本地的蓝牙适配器
     */
    public static BluetoothAdapter getBluetoothAdapter() {
        BluetoothManager bluetoothManager = (BluetoothManager) Utils.getApp().getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager == null ? null : bluetoothManager.getAdapter();
    }

    /**
     * 验证蓝牙是否开启
     * @return true 已开启 false 未开启
     */
    public static boolean bleIsEnabled() {
        BluetoothManager bluetoothManager = (BluetoothManager) Utils.getApp().getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager != null && bluetoothManager.getAdapter() != null && bluetoothManager.getAdapter().isEnabled();
    }

    /**
     * 检查ble扫描是否需要定位权限<br/>
     * 6.0及以上需要该权限
     */
    public static boolean bleScanNeedLocationPermisstion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 检查ble扫描是否需要定位功能<br/>
     * 6.0及以上需要该权限
     */
    public static boolean bleScanNeedLocationFeature() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 检查BLE扫描需要的位置权限是否开启
     */
    public static boolean hasLocationPermission() {
        return !bleScanNeedLocationPermisstion() || PermissionHelper.hasLocationPermisstion();
    }

    /**
     * 检查BLE扫描需要的位置功能是否打开
     */
    public static boolean hasLocationFeature() {
        return !bleScanNeedLocationFeature() || LocationUtils.locationIsEnable();
    }

    /**
     * 请求BLE扫描需要的位置权限、位置功能
     */
    public static void requestLocationPermissionAndFeature(@NotNull final Object locationPermissionAndFeatureCheckListenerObject) {
        if (!(locationPermissionAndFeatureCheckListenerObject instanceof OnLocationPermissionAndFeatureCheckListener)) {
            throw new IllegalArgumentException("error locationPermissionAndFeatureCheckListenerObject");
        }
        sBleHelper = new BLEHelper();
        sBleHelper.locationPermissionAndFeatureCheckListenerObject = locationPermissionAndFeatureCheckListenerObject;
        if (!bleScanNeedLocationPermisstion()) {//6.0以下蓝牙扫描不需要位置权限
            if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
            } else {
                ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
            }
            return;
        }
        PermissionHelper.requestLocation(new PermissionHelper.OnFullPermissionListener() {
            @Override
            public void denied() {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onFail(LOCATION_PERMISSION_DENIED);
                } else {
                    ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onFail(LOCATION_PERMISSION_DENIED);
                }
            }

            @Override
            public void granted() {
                if (!bleScanNeedLocationFeature() || LocationUtils.locationIsEnable()) {//6.0以下蓝牙扫描不需要位置功能，或者定位功能已打开
                    if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                        ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
                    } else {
                        ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
                    }
                    return;
                }
                //尝试强制打开gps
                LogUtils.i(TAG, "尝试强制打开gps");
                try {
                    LocationUtils.openGPS();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                    //强制打开失败
                    LogUtils.e(TAG, "强制打开失败,捕获异常");
                    openLocationSettingActivity(locationPermissionAndFeatureCheckListenerObject);
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (LocationUtils.locationIsEnable()) {
                    //已强制打开gps
                    LogUtils.i(TAG, "已强制打开gps");
                    if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                        ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
                    } else {
                        ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onSuccess();
                    }
                    return;
                }
                //强制打开失败
                LogUtils.e(TAG, "强制打开失败,校验异常");
                openLocationSettingActivity(locationPermissionAndFeatureCheckListenerObject);
            }

            @Override
            public void confirmShowPermissionSettingTransActivity(PermissionHelper.OnPermissionConfirmListener onPermissionConfirmListener) {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).confirmShowPermissionSettingTransActivity(onPermissionConfirmListener);
                } else {
                    ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).confirmShowPermissionSettingTransActivity(onPermissionConfirmListener);
                }
            }

            @Override
            public void showRationaleDialog(PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest) {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).showRationaleDialog(shouldRequest);
                } else {
                    ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).showRationaleDialog(shouldRequest);
                }
            }

            @Override
            public Integer getPermissionSettingThemeResId() {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    return ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).getPermissionSettingThemeResId();
                } else {
                    return null;
                }
            }

            @Override
            public Integer getPermissionSettingContentViewResId() {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    return ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).getPermissionSettingContentViewResId();
                } else {
                    return null;
                }
            }

            @Override
            public Integer getPermissionThemeResId() {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    return ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).getPermissionThemeResId();
                } else {
                    return null;
                }
            }

            @Override
            public Integer getPermissionContentViewResId() {
                if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    return ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).getPermissionContentViewResId();
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * 请求打开位置设置界面
     */
    private static void openLocationSettingActivity(@NotNull final Object locationPermissionAndFeatureCheckListenerObject) {
        if (locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
            ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).confirmShowLocationSettingTransActivity(new PermissionHelper.OnPermissionConfirmListener() {
                @Override
                public void onAgree() {
                    Intent intent = new Intent(Utils.getApp(), LocationSettingTransActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.getApp().startActivity(intent);
                }

                @Override
                public void onRefuse() {
                    ((OnSimpleLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onFail(CANCEL_OPEN_LOCATION_SETTING_ACTIVITY);
                }
            });
        } else {
            ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).confirmShowLocationSettingTransActivity(new PermissionHelper.OnPermissionConfirmListener() {
                @Override
                public void onAgree() {
                    Intent intent = new Intent(Utils.getApp(), LocationSettingTransActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.getApp().startActivity(intent);
                }

                @Override
                public void onRefuse() {
                    ((OnLocationPermissionAndFeatureCheckListener)locationPermissionAndFeatureCheckListenerObject).onFail(CANCEL_OPEN_LOCATION_SETTING_ACTIVITY);
                }
            });
        }
    }

    /**
     * 定位设置过渡界面
     */
    public static class LocationSettingTransActivity extends Activity {

        @Override
        public void setTheme(int resid) {
            if (sBleHelper.locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener
                    && ((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).getLocationSettingThemeResId() != null) {
                super.setTheme(((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).getLocationSettingThemeResId());
            } else {
                super.setTheme(resid);
            }
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (sBleHelper.locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener
                    && ((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).getLocationSettingContentViewResId() != null) {
                setContentView(((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).getLocationSettingContentViewResId());
            }
            LocationUtils.openGpsSettingsForResult(this, 1);//在当前activity任务栈中打开定位设置界面
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (!LocationUtils.locationIsEnable()) {
                LogUtils.e(TAG, "用户选择定位功能后，定位未开启");
                if (sBleHelper.locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                    ((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).onFail(LOCATION_FEATURE_DENIED);
                } else {
                    ((OnLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).onFail(LOCATION_FEATURE_DENIED);
                }
                finish();
                return;
            }
            if (sBleHelper.locationPermissionAndFeatureCheckListenerObject instanceof OnSimpleLocationPermissionAndFeatureCheckListener) {
                ((OnSimpleLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).onSuccess();
            } else {
                ((OnLocationPermissionAndFeatureCheckListener)sBleHelper.locationPermissionAndFeatureCheckListenerObject).onSuccess();
            }
            finish();
        }
    }

    /**
     * 基础的BLE扫描需要的位置权限是否开启、位置功能是否打开的监听器
     */
    public interface OnBaseLocationPermissionAndFeatureCheckListener {

        /**
         * 检测成功
         */
        void onSuccess();

        /**
         * 检测失败
         */
        void onFail(int errorCode);
    }

    /**
     * 简单的BLE扫描需要的位置权限是否开启、位置功能是否打开的监听器
     */
    public interface OnLocationPermissionAndFeatureCheckListener extends OnBaseLocationPermissionAndFeatureCheckListener {

        /**
         * 需要显示解释权限请求的对话框
         */
        void showRationaleDialog(PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest);

        /**
         * 确认显示权限设置界面的对话框
         */
        void confirmShowPermissionSettingTransActivity(PermissionHelper.OnPermissionConfirmListener onPermissionConfirmListener);

        /**
         * 需要显示定位设置界面的对话框
         */
        void confirmShowLocationSettingTransActivity(PermissionHelper.OnPermissionConfirmListener onPermissionConfirmListener);
    }

    /**
     * BLE扫描需要的位置权限是否开启、位置功能是否打开的监听器
     */
    public interface OnSimpleLocationPermissionAndFeatureCheckListener extends OnLocationPermissionAndFeatureCheckListener {

        /**
         * 获取定位设置过渡界面主题资源id
         */
        Integer getLocationSettingThemeResId();

        /**
         * 获取定位设置过渡界面布局资源id
         */
        Integer getLocationSettingContentViewResId();

        /**
         * 获取权限设置过渡界面主题资源id
         */
        Integer getPermissionSettingThemeResId();

        /**
         * 获取权限设置过渡界面布局资源id
         */
        Integer getPermissionSettingContentViewResId();

        /**
         * 获取权限过渡界面主题资源id
         */
        Integer getPermissionThemeResId();

        /**
         * 获取权限过渡界面布局资源id
         */
        Integer getPermissionContentViewResId();
    }
}
