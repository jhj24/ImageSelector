package com.zgdj.qualitycontrol.utils.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.support.v4.app.AppOpsManagerCompat
import android.support.v4.content.ContextCompat
import java.util.*

/**
 * Created by jhj on 19-1-21.
 */
object PermissionsUtil {


    val permissionsMap = mapOf(
            Manifest.permission.CAMERA to "相机",
            Manifest.permission.READ_EXTERNAL_STORAGE to "内存",
            Manifest.permission.WRITE_EXTERNAL_STORAGE to "内存",
            Manifest.permission.READ_PHONE_STATE to "获取手机信息",
            Manifest.permission.ACCESS_FINE_LOCATION to "定位",
            Manifest.permission.ACCESS_COARSE_LOCATION to "定位"
    )

    /**
     * 对于6.0以下以及个别6.0类型手机禁止拍照权限后。能拍照但不能二维码扫描
     *
     * @return boolean true-权限被禁止
     */
    val isCameraDenied: Boolean
        get() {
            var isCanUse = true
            var mCamera: Camera? = null
            try {
                mCamera = Camera.open()
                val mParameters = mCamera?.parameters
                mCamera.parameters = mParameters
            } catch (e: Exception) {
                isCanUse = false
            }

            if (mCamera != null) {
                try {
                    mCamera.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return !isCanUse
                }

            }
            return !isCanUse
        }

    /**
     * 获取被禁止的权限,利用 Android6.0 的权限检查方法
     *
     * @param activity Activity
     * @return array
     */
    fun getDeniedPermissions(activity: Activity, mPermissions: Array<String>?): Array<String> {
        val deniedPermissions = ArrayList<String>()
        if (mPermissions == null) {
            return arrayOf()
        }
        for (mPermission in mPermissions) {
            if (ContextCompat.checkSelfPermission(activity, mPermission) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(mPermission)
            }
        }
        return deniedPermissions.toTypedArray()
    }

    /**
     * 获取被禁止的权限(彻底的)
     *
     * @param permissions 所有权限
     */
    fun getPermissionDenied(activity: Activity, permissions: Array<String>): Array<String> {
        val allowPermissionList = ArrayList<String>()
        val deniedPermissionList = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                allowPermissionList.add(permission)
            } else {
                deniedPermissionList.add(permission)
            }
        }

        //对相机进行独立权限鉴定（有些手机能拍照，但不能扫描）
        if (allowPermissionList.contains(Manifest.permission.CAMERA) && isCameraDenied) {
            deniedPermissionList.add(Manifest.permission.CAMERA)
            allowPermissionList.remove(Manifest.permission.CAMERA)
        }

        //对允许的权限进行底层权限鉴定
        deniedPermissionList.addAll(bottomLayerPermissionsIdentify(activity, allowPermissionList))

        return deniedPermissionList.toTypedArray()

    }

    /**
     * 底层权限管理检验
     *
     * @param context     context
     * @param permissions permissions
     * @return 被禁权限
     */
    private fun bottomLayerPermissionsIdentify(context: Context, permissions: List<String>): List<String> {
        val list = ArrayList<String>()

        for (permission in permissions) {
            val op = AppOpsManagerCompat.permissionToOp(permission)
            if (op != null) {
                val result = AppOpsManagerCompat.noteProxyOp(context, op, context.packageName)
                if (result == AppOpsManagerCompat.MODE_IGNORED) { //忽略
                    list.add(permission)
                } else if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { //禁止
                    list.add(permission)
                }
            }
        }
        return list
    }


}
