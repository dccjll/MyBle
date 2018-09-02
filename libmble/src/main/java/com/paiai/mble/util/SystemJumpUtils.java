package com.paiai.mble.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

/**
 * 作者：云渡山<br>
 * 创建时间：2018/2/11 09 51 星期日<br>
 * 功能描述：界面跳转工具类<br>
 */
public class SystemJumpUtils {

    private static final String TAG = "SystemJumpUtils";

    /**
     * Build.MANUFACTURER
     */
    private static final String MANUFACTURER_HUAWEI = "huawei";//华为
    private static final String MANUFACTURER_MEIZU = "meizu";//魅族
    private static final String MANUFACTURER_XIAOMI = "xiaomi";//小米
    private static final String MANUFACTURER_SONY = "sony";//索尼
    private static final String MANUFACTURER_OPPO = "oppo";
    private static final String MANUFACTURER_LG = "lg";
    private static final String MANUFACTURER_VIVO = "vivo";
    private static final String MANUFACTURER_SAMSUNG = "samsung";//三星
    private static final String MANUFACTURER_LETV = "letv";//乐视
    private static final String MANUFACTURER_ZTE = "ZTE";//中兴
    private static final String MANUFACTURER_YULONG = "YuLong";//酷派
    private static final String MANUFACTURER_LENOVO = "LENOVO";//联想

    /**
     * 此函数可以自己定义
     * @param activity
     */
    public static void goToPermissionSetting(Activity activity, Integer requestCode){
        Log.i(TAG, "goToStting,Build.MANUFACTURER=" + Build.MANUFACTURER);
        if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_HUAWEI)) {
            huawei(activity, requestCode);
        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_MEIZU)) {
            meizu(activity, requestCode);

        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_XIAOMI)) {
            xiaomi(activity, requestCode);

        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_SONY)) {
            sony(activity, requestCode);

        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_OPPO)) {
            oppo(activity, requestCode);

        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_LG)) {
            lg(activity, requestCode);

        } else if (Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_LETV)) {
            letv(activity, requestCode);

        } else {
            applicationInfo(activity, requestCode);
            Log.e("goToPermissionSetting", "目前暂不支持此系统");

        }
    }

    private static void huawei(Activity activity, Integer requestCode) {
        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("packageName", activity.getPackageName());//配置了应用的包名也无法跳转到应用的权限设置界面，不知道为什么
        ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void meizu(Activity activity, Integer requestCode) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", activity.getPackageName());
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void xiaomi(Activity activity, Integer requestCode) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        intent.setComponent(componentName);
        intent.putExtra("extra_pkgname", activity.getPackageName());
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void sony(Activity activity, Integer requestCode) {
        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void oppo(Activity activity, Integer requestCode) {
        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void lg(Activity activity, Integer requestCode) {
        Intent intent = new Intent("android.intent.action.MAIN");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static void letv(Activity activity, Integer requestCode) {
        Intent intent = new Intent();
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 只能打开到自带安全软件
     * @param activity
     */
    public static void _360(Activity activity, Integer requestCode) {
        Intent intent = new Intent("android.intent.action.MAIN");
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", activity.getPackageName());
        ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
        intent.setComponent(comp);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * 应用信息界面
     * @param activity
     */
    @SuppressLint("ObsoleteSdkInt")
    public static void applicationInfo(Activity activity, Integer requestCode){
        Intent localIntent = new Intent();
//        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
        }
        if (requestCode == null) {
            activity.startActivity(localIntent);
        } else {
            activity.startActivityForResult(localIntent, requestCode);
        }
    }

    /**
     * 系统设置界面
     * @param activity
     */
    public static void systemConfig(Activity activity, Integer requestCode) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        if (requestCode == null) {
            activity.startActivity(intent);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }
}
