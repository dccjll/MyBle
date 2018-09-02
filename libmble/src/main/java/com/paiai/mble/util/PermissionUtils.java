package com.paiai.mble.util;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;

import com.blankj.subutil.util.Utils;
import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class PermissionUtils {

    private static final String TAG = "PermissionUtils";

    private static final List<String> PERMISSIONS = getPermissions();

    private static PermissionUtils sInstance;

    private OnRationaleListener mOnRationaleListener;
    private SimpleCallback      mSimpleCallback;
    private FullCallback        mFullCallback;
    private ThemeCallback       mThemeCallback;
    private Set<String> mPermissions;
    private List<String>        mPermissionsRequest;
    private List<String>        mPermissionsGranted;
    private List<String>        mPermissionsDenied;
    private List<String>        mPermissionsDeniedForever;

    /**
     * 获取应用权限
     *
     * @return 清单文件中的权限列表
     */
    public static List<String> getPermissions() {
        return getPermissions(Utils.getApp().getPackageName());
    }

    /**
     * 获取应用权限
     *
     * @param packageName The name of the package.
     * @return 清单文件中的权限列表
     */
    public static List<String> getPermissions(final String packageName) {
        PackageManager pm = Utils.getApp().getPackageManager();
        try {
            return Arrays.asList(
                    pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                            .requestedPermissions
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 判断权限是否被授予
     *
     * @param permissions 权限
     * @return {@code true}: 是<br>{@code false}: 否
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isGranted(final String... permissions) {
        for (String permission : permissions) {
            if (!isGranted(permission)) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean isGranted(final String permission) {
        boolean granted;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            granted = PackageManager.PERMISSION_GRANTED == PermissionChecker.checkSelfPermission(Utils.getApp(), permission);
            if (permission.equals(Manifest.permission.CAMERA)) {
                granted = isPermissionGrantedByOps("26");
            }
        } else {
            granted = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(Utils.getApp(), permission);
        }
        return granted;
    }

    private static boolean isPermissionGrantedByOps(String permissionCode) {
        try {
            Object object = Utils.getApp().getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);

            if (method == null) {
                return false;
            }
            Object[] arrayOfObject = new Object[3];
            arrayOfObject[0] = Integer.valueOf(permissionCode);
            arrayOfObject[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject[2] = Utils.getApp().getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject)).intValue();
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*private static boolean isPermissionGranted(String permission) {
        String permissionCode = "-1";
        if (permission.equals(Manifest.permission.READ_CALENDAR)) {
            permissionCode = "8";
        } else if (permission.equals(Manifest.permission.WRITE_CALENDAR)) {
            permissionCode = "9";
        } else if (permission.equals(Manifest.permission.CAMERA)) {
            permissionCode = "26";
        } else if (permission.equals(Manifest.permission.READ_CONTACTS)) {
            permissionCode = "4";
        } else if (permission.equals(Manifest.permission.WRITE_CONTACTS)) {
            permissionCode = "5";
        } else if (permission.equals(Manifest.permission.GET_ACCOUNTS)) {
            permissionCode = "62";
        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionCode = "1";
        } else if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionCode = "0";
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            permissionCode = "27";
        } else if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
            permissionCode = "51";
        } else if (permission.equals(Manifest.permission.READ_PHONE_NUMBERS)) {
            permissionCode = "65";
        } else if (permission.equals(Manifest.permission.CALL_PHONE)) {
            permissionCode = "13";
        } else if (permission.equals(Manifest.permission.ANSWER_PHONE_CALLS)) {
            permissionCode = "69";
        } else if (permission.equals(Manifest.permission.READ_CALL_LOG)) {
            permissionCode = "6";
        } else if (permission.equals(Manifest.permission.WRITE_CALL_LOG)) {
            permissionCode = "7";
        } else if (permission.equals(Manifest.permission.ADD_VOICEMAIL)) {
            permissionCode = "52";
        } else if (permission.equals(Manifest.permission.USE_SIP)) {
            permissionCode = "53";
        } else if (permission.equals(Manifest.permission.PROCESS_OUTGOING_CALLS)) {
            permissionCode = "54";
        } else if (permission.equals(Manifest.permission.BODY_SENSORS)) {
            permissionCode = "56";
        } else if (permission.equals(Manifest.permission.SEND_SMS)) {
            permissionCode = "20";
        } else if (permission.equals(Manifest.permission.RECEIVE_SMS)) {
            permissionCode = "16";
        } else if (permission.equals(Manifest.permission.READ_SMS)) {
            permissionCode = "14";
        } else if (permission.equals(Manifest.permission.RECEIVE_WAP_PUSH)) {
            permissionCode = "19";
        } else if (permission.equals(Manifest.permission.RECEIVE_MMS)) {
            permissionCode = "18";
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionCode = "59";
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionCode = "60";
        } else {
            permissionCode = "-1";
        }
        return isPermissionGrantedByOps(permissionCode);
    }*/

    /**
     * 打开应用具体设置
     */
    public static void openAppSettings() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.parse("package:" + Utils.getApp().getPackageName()));
        Utils.getApp().startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    /**
     * 设置请求权限
     *
     * @param permissions 要请求的权限
     * @return {@link PermissionUtils}
     */
    public static PermissionUtils permission(@PermissionConstants.Permission final String... permissions) {
        return new PermissionUtils(permissions);
    }

    private PermissionUtils(final String... permissions) {
        mPermissions = new LinkedHashSet<>();
        for (String permission : permissions) {
            for (String aPermission : PermissionConstants.getPermissions(permission)) {
                if (PERMISSIONS.contains(aPermission)) {
                    mPermissions.add(aPermission);
                }
            }
        }
        sInstance = this;
    }

    /**
     * 设置拒绝权限后再次请求的回调接口
     *
     * @param listener 拒绝权限后再次请求的回调接口
     * @return {@link PermissionUtils}
     */
    public PermissionUtils rationale(final OnRationaleListener listener) {
        mOnRationaleListener = listener;
        return this;
    }

    /**
     * 设置回调
     *
     * @param callback 简单回调接口
     * @return {@link PermissionUtils}
     */
    public PermissionUtils callback(final SimpleCallback callback) {
        mSimpleCallback = callback;
        return this;
    }

    /**
     * 设置回调
     *
     * @param callback 完整回调接口
     * @return {@link PermissionUtils}
     */
    public PermissionUtils callback(final FullCallback callback) {
        mFullCallback = callback;
        return this;
    }

    /**
     * 设置主题
     *
     * @param callback 主题回调接口
     * @return {@link PermissionUtils}
     */
    public PermissionUtils theme(final ThemeCallback callback) {
        mThemeCallback = callback;
        return this;
    }

    /**
     * 开始请求
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public PermissionUtils request() {
        mPermissionsGranted = new ArrayList<>();
        mPermissionsRequest = new ArrayList<>();
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionsGranted.addAll(mPermissions);
            requestCallback();
        } else {
            for (String permission : mPermissions) {
                if (isGranted(permission)) {
                    mPermissionsGranted.add(permission);
                } else {
                    mPermissionsRequest.add(permission);
                }
            }
            if (mPermissionsRequest.isEmpty()) {
                requestCallback();
            } else {
                startPermissionActivity();
            }
        }*/
        for (String permission : mPermissions) {
            if (isGranted(permission)) {
                mPermissionsGranted.add(permission);
            } else {
                mPermissionsRequest.add(permission);
            }
        }
        if (mPermissionsRequest.isEmpty()) {
            requestCallback();
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                sInstance.getPermissionsStatus(ActivityUtils.getTopActivity());
                requestCallback();
                return this;
            }
            startPermissionActivity();
        }
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startPermissionActivity() {
        mPermissionsDenied = new ArrayList<>();
        mPermissionsDeniedForever = new ArrayList<>();
        Intent intent = new Intent(Utils.getApp(), PermissionTransActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.getApp().startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean rationale(final Activity activity) {
        boolean isRationale = false;
        if (mOnRationaleListener != null) {
            for (String permission : mPermissionsRequest) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    getPermissionsStatus(activity);
                    mOnRationaleListener.rationale(new OnRationaleListener.ShouldRequest() {
                        @Override
                        public void again(boolean again) {
                            if (again) {
                                startPermissionActivity();
                            } else {
                                requestCallback();
                            }
                        }
                    });
                    isRationale = true;
                    break;
                }
            }
            mOnRationaleListener = null;
        }
        return isRationale;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getPermissionsStatus(final Activity activity) {
        if (mPermissionsGranted == null) {
            mPermissionsGranted = new ArrayList<>();
        }
        if ((mPermissionsDenied == null)) {
            mPermissionsDenied = new ArrayList<>();
        }
        if (mPermissionsDeniedForever == null) {
            mPermissionsDeniedForever = new ArrayList<>();
        }
        for (String permission : mPermissionsRequest) {
            if (isGranted(permission)) {
                mPermissionsGranted.add(permission);
            } else {
                mPermissionsDenied.add(permission);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    mPermissionsDeniedForever.add(permission);
                } else {
                    if (!activity.shouldShowRequestPermissionRationale(permission)) {
                        mPermissionsDeniedForever.add(permission);
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCallback() {
        if (mSimpleCallback != null) {
            if (mPermissionsRequest.size() == 0
                    || mPermissions.size() == mPermissionsGranted.size()) {
                mSimpleCallback.onGranted();
            } else {
                if (!mPermissionsDenied.isEmpty()) {
                    mSimpleCallback.onDenied();
                }
            }
            mSimpleCallback = null;
        }
        if (mFullCallback != null) {
            if (mPermissionsRequest.size() == 0
                    || mPermissions.size() == mPermissionsGranted.size()) {
                mFullCallback.onGranted(mPermissionsGranted);
            } else {
                if (!mPermissionsDenied.isEmpty()) {
                    mFullCallback.onDenied(mPermissionsDeniedForever, mPermissionsDenied);
                }
            }
            mFullCallback = null;
        }
        mOnRationaleListener = null;
        mThemeCallback = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onRequestPermissionsResult(final Activity activity) {
        getPermissionsStatus(activity);
        requestCallback();
    }

    /**
     * 权限过渡界面
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static class PermissionTransActivity extends Activity {

        @Override
        public void setTheme(int resid) {
            if (sInstance.mThemeCallback != null && sInstance.mThemeCallback.getPermissionThemeResId() != null) {
                super.setTheme(sInstance.mThemeCallback.getPermissionThemeResId());
            } else {
                super.setTheme(resid);
            }
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (sInstance.mThemeCallback != null && sInstance.mThemeCallback.getPermissionContentViewResId() != null) {
                setContentView(sInstance.mThemeCallback.getPermissionContentViewResId());
            }
            if (sInstance.rationale(this)) {
                finish();
                return;
            }
            if (sInstance.mPermissionsRequest != null) {
                int size = sInstance.mPermissionsRequest.size();
                requestPermissions(sInstance.mPermissionsRequest.toArray(new String[size]), 1);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            sInstance.onRequestPermissionsResult(this);
            finish();
        }
    }

    public interface OnRationaleListener {

        void rationale(ShouldRequest shouldRequest);

        interface ShouldRequest {
            void again(boolean again);
        }
    }

    public interface SimpleCallback {
        void onGranted();

        void onDenied();
    }

    public interface FullCallback {
        void onGranted(List<String> permissionsGranted);

        void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied);
    }

    public interface ThemeCallback {
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