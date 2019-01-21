package com.jhj.imageselector.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jhj on 19-1-21.
 */
class PermissionsUtil {

    /**
     * 对于6.0以下以及个别6.0类型手机禁止拍照权限后。能拍照但不能二维码扫描
     *
     * @return boolean true-权限被禁止
     */
    static boolean isCameraDenied() {
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
     * 获取被禁止的权限
     *
     * @param permissions 所有权限
     */
    static List<String> getPermissionDenied(Activity activity, String... permissions) {
        List<String> allowPermissionList = new ArrayList<>();
        List<String> deniedPermissionList = new ArrayList<>();

        if (permissions == null) {
            return deniedPermissionList;
        }

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
    private static List<String> bottomLayerPermissionsIdentify(Context context, String... permissions) {
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


}
