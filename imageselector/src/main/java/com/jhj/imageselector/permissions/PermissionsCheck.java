package com.jhj.imageselector.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;

import java.lang.ref.WeakReference;

public final class PermissionsCheck {

    private String[] mPermissions;
    private WeakReference<Activity> mActivity;

    private PermissionsCheck(Activity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    public static PermissionsCheck with(Activity activity) {
        return new PermissionsCheck(activity);
    }

    public PermissionsCheck requestPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public void onPermissionsResult(OnPermissionsResultListener resultPermissionsListener) {
        Activity activity = mActivity.get();
        if (activity == null || mPermissions == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0之前不用检测
            String[] deniedArray = new String[]{};
            for (String permission : mPermissions) {
                if (Manifest.permission.CAMERA.equals(permission) && PermissionsUtil.isCameraDenied()) {
                    deniedArray = new String[]{Manifest.permission.CAMERA};
                }
            }
            resultPermissionsListener.onPermissionsResult(deniedArray, mPermissions);
        } else if (PermissionsUtil.getDeniedPermissions(activity, mPermissions).length > 0) { //如果有权限被禁止，进行权限请求
            requestPermissions(activity, resultPermissionsListener);
        } else { // 所有权限都被允许，使用原生权限管理检验
            String[] permissionList = PermissionsUtil.getPermissionDenied(activity, mPermissions);
            resultPermissionsListener.onPermissionsResult(permissionList, mPermissions);
        }
    }


    /**
     * 权限被禁，进行权限请求
     */
    private void requestPermissions(final Activity activity, OnPermissionsResultListener resultPermissionsListener) {
        String TAG = getClass().getName();
        FragmentManager fragmentManager = activity.getFragmentManager();
        PermissionsFragment fragment = (PermissionsFragment) fragmentManager.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = new PermissionsFragment();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        fragment.permissionsRequest(mPermissions, resultPermissionsListener);

    }


    /**
     * 请求权限回调。
     */
    public interface OnPermissionsResultListener {

        void onPermissionsResult(String[] deniedPermissions, String[] allPermissions);

    }

}