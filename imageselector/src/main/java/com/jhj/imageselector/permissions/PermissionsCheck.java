package com.jhj.imageselector.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PermissionsCheck {

    private static volatile PermissionsCheck singleton;
    private String[] mPermissions;
    private Activity mActivity;
    private OnPermissionsResultListener mPermissionResultListener;

    public PermissionsCheck(Activity mActivity) {
        this.mActivity = mActivity;
    }


    public PermissionsCheck requestPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public void onPermissionsResult(OnPermissionsResultListener resultPermissionsListener) {

        this.mPermissionResultListener = resultPermissionsListener;
        if (mActivity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0之前不用检测
            List<String> deniedList = new ArrayList<>();
            for (String permission : mPermissions) {
                if (Manifest.permission.CAMERA.equals(permission) && PermissionsUtil.isCameraDenied()) {
                    deniedList.add(permission);
                }
            }
            mPermissionResultListener.onPermissionsResult(deniedList, Arrays.asList(mPermissions));
        } else if (isPermissionsDenied(mActivity)) { //如果有权限被禁止，进行权限请求
            requestPermissions(mActivity, resultPermissionsListener);
        } else { // 所有权限都被允许，使用原生权限管理检验
            List<String> permissionList = PermissionsUtil.getPermissionDenied(mActivity, mPermissions);
            mPermissionResultListener.onPermissionsResult(permissionList, Arrays.asList(mPermissions));
        }
    }

    /**
     * 权限被禁，进行权限请求
     */
    private void requestPermissions(final Activity activity, OnPermissionsResultListener listener) {
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
        String[] array = new String[]{};
        List<String> list = PermissionsUtil.getPermissionDenied(mActivity, mPermissions);
        fragment.permissionsRequest(list.toArray(array), listener);

    }

    /**
     * 检测权限是否被禁止
     *
     * @param activity Activity
     * @return true-禁止
     */
    private boolean isPermissionsDenied(Activity activity) {
        for (String permission : mPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }


    /**
     * 请求权限回调。
     */
    public interface OnPermissionsResultListener {

        void onPermissionsResult(List<String> deniedPermissions, List<String> allPermissions);

    }

}