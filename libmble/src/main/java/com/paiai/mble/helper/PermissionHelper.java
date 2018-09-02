package com.paiai.mble.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.blankj.subutil.util.Utils;
import com.blankj.utilcode.constant.PermissionConstants;
import com.paiai.mble.util.LogUtils;
import com.paiai.mble.util.PermissionUtils;
import com.paiai.mble.util.SystemJumpUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PermissionHelper {

    private static final String TAG = "PermissionHelper";

    private static PermissionHelper sPrmissionHelper;
    private Object permissionListenerObject;//权限请求回调管理器
    private List<String> mPermissionsRequest;//请求的权限列表

    /**
     *  请求读写日历权限，默认过渡界面样式
     */
    public static PermissionUtils requestCalendar(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CALENDAR));
    }

    /**
     *  请求读写日历权限
     */
    public static PermissionUtils requestCalendar(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CALENDAR));
    }

    /**
     * 请求拍照权限，默认过渡界面样式
     */
    public static PermissionUtils requestCamera(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CAMERA));
    }

    /**
     * 请求拍照权限
     */
    public static PermissionUtils requestCamera(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CAMERA));
    }

    /**
     * 请求联系人访问与读写权限，默认过渡界面样式
     */
    public static PermissionUtils requestContacts(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CONTACTS));
    }

    /**
     * 请求联系人访问与读写权限
     */
    public static PermissionUtils requestContacts(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.CONTACTS));
    }

    /**
     * 请求位置权限，默认过渡界面样式
     */
    public static PermissionUtils requestLocation(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.LOCATION));
    }

    /**
     * 请求位置权限
     */
    public static PermissionUtils requestLocation(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.LOCATION));
    }

    /**
     * 请求录音权限，默认过渡界面样式
     */
    public static PermissionUtils requestMicroPhone(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.MICROPHONE));
    }

    /**
     * 请求录音权限
     */
    public static PermissionUtils requestMicroPhone(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.MICROPHONE));
    }

    /**
     * 请求手机权限组，包括打电话，访问手机状态、通话记录等，默认过渡界面样式
     */
    public static PermissionUtils requestPhone(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.PHONE));
    }

    /**
     * 请求手机权限组，包括打电话，访问手机状态、通话记录等
     */
    public static PermissionUtils requestPhone(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.PHONE));
    }

    /**
     * 请求传感器权限，默认过渡界面样式
     */
    public static PermissionUtils requestSensors(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.SENSORS));
    }

    /**
     * 请求传感器权限
     */
    public static PermissionUtils requestSensors(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.SENSORS));
    }

    /**
     * 请求短信读写与访问权限，默认过渡界面样式
     */
    public static PermissionUtils requestSms(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.SMS));
    }

    /**
     * 请求短信读写与访问权限
     */
    public static PermissionUtils requestSms(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.SMS));
    }

    /**
     * 请求读写sd卡权限，默认过渡界面样式
     */
    public static PermissionUtils requestStorage(@NotNull final OnWellPermissionListener onWellPermissionListener) {
        return request(onWellPermissionListener, PermissionConstants.getPermissions(PermissionConstants.STORAGE));
    }

    /**
     * 请求读写sd卡权限
     */
    public static PermissionUtils requestStorage(@NotNull final OnFullPermissionListener onFullPermissionListener) {
        return request(onFullPermissionListener, PermissionConstants.getPermissions(PermissionConstants.STORAGE));
    }

    /**
     * 权限请求总入口
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static PermissionUtils request(@NotNull final Object permissionListenerObject,
                                           @NotNull final @PermissionConstants.Permission String... permissions) {
        if (!(permissionListenerObject instanceof OnWellPermissionListener) && !(permissionListenerObject instanceof OnFullPermissionListener)) {
            throw new IllegalArgumentException("error permisstionListenerObject");
        }
        sPrmissionHelper = new PermissionHelper();
        sPrmissionHelper.mPermissionsRequest = Arrays.asList(permissions);
        sPrmissionHelper.permissionListenerObject = permissionListenerObject;
        return PermissionUtils.permission(permissions)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(ShouldRequest shouldRequest) {
                        LogUtils.i(TAG, "需要解释的权限:" + Arrays.toString(permissions));
                        if (permissionListenerObject instanceof OnFullPermissionListener) {
                            ((OnFullPermissionListener)permissionListenerObject).showRationaleDialog(shouldRequest);
                        } else {
                            ((OnWellPermissionListener)permissionListenerObject).showRationaleDialog(shouldRequest);
                        }
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        LogUtils.i(TAG, "已授权的权限:" + permissionsGranted);
                        if (permissionListenerObject instanceof OnFullPermissionListener) {
                            ((OnFullPermissionListener)permissionListenerObject).granted();
                        } else {
                            ((OnWellPermissionListener)permissionListenerObject).granted();
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, final List<String> permissionsDenied) {
                        if (permissionListenerObject instanceof OnFullPermissionListener) {
                            ((OnFullPermissionListener)permissionListenerObject).confirmShowPermissionSettingTransActivity(new OnPermissionConfirmListener() {
                                @Override
                                public void onAgree() {
                                    Intent intent = new Intent(Utils.getApp(), PermissionSettingTransActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Utils.getApp().startActivity(intent);
                                }

                                @Override
                                public void onRefuse() {
                                    ((OnFullPermissionListener)permissionListenerObject).denied();
                                }
                            });
                        } else {
                            ((OnWellPermissionListener)permissionListenerObject).confirmShowPermissionSettingTransActivity(new OnPermissionConfirmListener() {
                                @Override
                                public void onAgree() {
                                    Intent intent = new Intent(Utils.getApp(), PermissionSettingTransActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Utils.getApp().startActivity(intent);
                                }

                                @Override
                                public void onRefuse() {
                                    ((OnWellPermissionListener)permissionListenerObject).denied();
                                }
                            });
                        }
                    }
                }).theme(new PermissionUtils.ThemeCallback() {

                    @Override
                    public Integer getPermissionThemeResId() {
                        if (permissionListenerObject instanceof OnFullPermissionListener) {
                            return ((OnFullPermissionListener)permissionListenerObject).getPermissionThemeResId();
                        } else {
                            return null;
                        }
                    }

                    @Override
                    public Integer getPermissionContentViewResId() {
                        if (permissionListenerObject instanceof OnFullPermissionListener) {
                            return ((OnFullPermissionListener)permissionListenerObject).getPermissionContentViewResId();
                        } else {
                            return null;
                        }
                    }
                })
                .request();
    }

    /**
     * app权限设置过度界面
     */
    public static class PermissionSettingTransActivity extends Activity {

        @Override
        public void setTheme(int resid) {
            if (sPrmissionHelper.permissionListenerObject instanceof OnFullPermissionListener && ((OnFullPermissionListener)sPrmissionHelper.mPermissionsRequest).getPermissionSettingThemeResId() != null) {
                super.setTheme(((OnFullPermissionListener)sPrmissionHelper.permissionListenerObject).getPermissionSettingThemeResId());
            } else {
                super.setTheme(resid);
            }
        }

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (sPrmissionHelper.permissionListenerObject instanceof OnFullPermissionListener && ((OnFullPermissionListener)sPrmissionHelper.permissionListenerObject).getPermissionSettingContentViewResId() != null) {
                setContentView(((OnFullPermissionListener)sPrmissionHelper.permissionListenerObject).getPermissionSettingContentViewResId());
            }
            /*SystemJumpUtils.goToPermissionSetting(this, 2);*/
            SystemJumpUtils.applicationInfo(this, 2);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (PermissionUtils.isGranted(sPrmissionHelper.mPermissionsRequest.toArray(new String[sPrmissionHelper.mPermissionsRequest.size()]))) {
                if (sPrmissionHelper.permissionListenerObject instanceof OnFullPermissionListener) {
                    ((OnFullPermissionListener)sPrmissionHelper.permissionListenerObject).granted();
                } else {
                    ((OnWellPermissionListener)sPrmissionHelper.permissionListenerObject).granted();
                }
            } else {
                if (sPrmissionHelper.permissionListenerObject instanceof OnFullPermissionListener) {
                    ((OnFullPermissionListener)sPrmissionHelper.permissionListenerObject).denied();
                } else {
                    ((OnWellPermissionListener)sPrmissionHelper.permissionListenerObject).denied();
                }
            }
            finish();
        }
    }

    /**
     * 兼容性检查是否有定位使用权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasLocationPermisstion() {
        return PermissionUtils.isGranted(PermissionConstants.getPermissions(PermissionConstants.LOCATION));
    }

    /**
     * 权限请求基础监听器
     */
    public interface OnBasePermissionListener extends PermissionUtils.ThemeCallback {

        /**
         * 确认显示权限设置界面的对话框
         */
        void confirmShowPermissionSettingTransActivity(OnPermissionConfirmListener onPermissionConfirmListener);

        /**
         * 需要显示解释权限请求的对话框
         */
        void showRationaleDialog(PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest);

        /**
         * 获取权限设置过渡界面主题资源id
         */
        Integer getPermissionSettingThemeResId();

        /**
         * 获取权限设置过渡界面布局资源id
         */
        Integer getPermissionSettingContentViewResId();
    }

    /**
     * 权限请求简单监听器
     */
    public interface OnSimplePermissionListener  {

        /**
         * 权限拒绝
         */
        void denied();

        /**
         * 权限允许
         */
        void granted();
    }

    /**
     * 权限请求业务层监听器
     */
    public interface OnWellPermissionListener extends OnSimplePermissionListener {

        /**
         * 确认显示权限设置界面的对话框
         */
        void confirmShowPermissionSettingTransActivity(OnPermissionConfirmListener onPermissionConfirmListener);

        /**
         * 需要显示解释权限请求的对话框
         */
        void showRationaleDialog(PermissionUtils.OnRationaleListener.ShouldRequest shouldRequest);
    }

    /**
     * 权限请求完整监听器
     */
    public interface OnFullPermissionListener extends OnBasePermissionListener {

        /**
         * 权限拒绝
         */
        void denied();

        /**
         * 权限允许
         */
        void granted();
    }

    /**
     * 确认取消监听器
     */
    public interface OnPermissionConfirmListener {

        /**
         * 同意
         */
        void onAgree();

        /**
         * 不同意
         */
        void onRefuse();
    }
}
