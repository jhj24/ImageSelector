package com.jhj.imageselector.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PermissionsCheck {

    private static volatile PermissionsCheck singleton;
    private String[] mPermissions;
    private WeakReference<Activity> mActivity;
    private OnPermissionsResultListener mPermissionResultListener;

    private PermissionsCheck(Activity mActivity) {
        this.mActivity = new WeakReference<>(mActivity);
    }

    public static PermissionsCheck getInstance(Activity mActivity) {
        if (singleton == null) {
            synchronized (PermissionsCheck.class) {
                if (singleton == null) {
                    singleton = new PermissionsCheck(mActivity);
                }
            }
        }
        return singleton;
    }


    public PermissionsCheck requestPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    public void onPermissionsResult(OnPermissionsResultListener resultPermissionsListener) {

        this.mPermissionResultListener = resultPermissionsListener;
        Activity activity = mActivity.get();
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0之前不用检测
            List<String> deniedList = new ArrayList<>();
            for (String permission : mPermissions) {
                if (Manifest.permission.CAMERA.equals(permission) && isCameraDenied()) {
                    deniedList.add(permission);
                }
            }
            mPermissionResultListener.onPermissionsResult(deniedList, Arrays.asList(mPermissions));
        } else if (isPermissionsDenied(activity)) { //如果有权限被禁止，进行权限请求
            requestPermissions(activity);
        } else { // 所有权限都被允许，使用原生权限管理检验
            List<String> permissionList = getPermissionDenied(activity, mPermissions);
            mPermissionResultListener.onPermissionsResult(permissionList, Arrays.asList(mPermissions));
        }
    }


    void requestPermissionsResult(Activity activity, String[] permissions) {
        mPermissionResultListener.onPermissionsResult(getPermissionDenied(activity, permissions), Arrays.asList(permissions));
    }



    /**
     * 权限被禁，进行权限请求
     */
    private void requestPermissions(final Activity activity) {
        String TAG = getClass().getName();
        FragmentManager fragmentManager = activity.getFragmentManager();
        PermissionsFragment fragment = (PermissionsFragment) fragmentManager.findFragmentByTag(TAG);

        try {
            if (fragment == null) {
                Bundle bundle = new Bundle();
                bundle.putStringArray(PermissionsFragment.REQUEST_PERMISSIONS, mPermissions);
                fragment = new PermissionsFragment();
                fragment.setArguments(bundle);

                fragmentManager
                        .beginTransaction()
                        .add(fragment, TAG)
                        .commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            }
            fragment.permissionsRequest();
        }catch (Exception e){
            e.printStackTrace();
        }


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
     * 获取被禁止的权限
     *
     * @param permissions 所有权限
     */
    private List<String> getPermissionDenied(Activity activity, String... permissions) {
        List<String> allowPermissionList = new ArrayList<>();
        List<String> deniedPermissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                allowPermissionList.add(permission);
            } else {
                deniedPermissionList.add(permission);
            }
        }

        //对相机进行独立权限鉴定（有些手机能拍照，但不能扫描）
        if (allowPermissionList.contains(Manifest.permission.CAMERA) && isCameraDenied()) {
            deniedPermissionList.add(Manifest.permission.CAMERA);
            allowPermissionList.remove(Manifest.permission.CAMERA);
        }

        //对允许的权限进行底层权限鉴定
        String[] allowArray = new String[allowPermissionList.size()];
        allowPermissionList.toArray(allowArray);
        deniedPermissionList.addAll(bottomLayerPermissionsIdentify(activity, allowArray));


        return deniedPermissionList;

    }


    /**
     * 底层权限管理检验
     *
     * @param context     context
     * @param permissions permissions
     * @return 被禁权限
     */
    private List<String> bottomLayerPermissionsIdentify(Context context, String... permissions) {
        List<String> list = new ArrayList<>();

        for (String permission : permissions) {
            String op = AppOpsManagerCompat.permissionToOp(permission);
            if (op != null) {
                int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
                if (result == AppOpsManagerCompat.MODE_IGNORED) { //忽略
                    list.add(permission);
                } else if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { //禁止
                    list.add(permission);
                }
            }
        }
        return list;
    }


    /**
     * 对于6.0以下以及个别6.0类型手机禁止拍照权限后。能拍照但不能二维码扫描
     *
     * @return boolean true-权限被禁止
     */
    private boolean isCameraDenied() {
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            isCanUse = false;
        }

        if (mCamera != null) {
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                return !isCanUse;
            }

        }
        return !isCanUse;
    }

    /**
     * 请求权限回调。
     */
    public interface OnPermissionsResultListener {

        void onPermissionsResult(List<String> deniedPermissions, List<String> allPermissions);

    }

}