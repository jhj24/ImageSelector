package com.zgdj.qualitycontrol.utils.permissions


import android.Manifest
import android.app.Activity
import android.os.Build
import java.lang.ref.WeakReference

class PermissionsCheck private constructor(mActivity: Activity) {

    private var mPermissions: Array<String> = arrayOf()
    private val mActivity: WeakReference<Activity>

    init {
        this.mActivity = WeakReference(mActivity)
    }

    fun requestPermissions(vararg permissions: String): PermissionsCheck {
        this.mPermissions = permissions.asList().toTypedArray()
        return this
    }

    fun onPermissionsResult(body: (Array<String>, Array<String>) -> Unit) {
        val activity = mActivity.get()
        if (activity == null) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0之前不用检测
            var deniedArray = arrayOf<String>()
            for (permission in mPermissions) {
                if (Manifest.permission.CAMERA == permission && PermissionsUtil.isCameraDenied) {
                    deniedArray = arrayOf(Manifest.permission.CAMERA)
                }
            }
           body(deniedArray, mPermissions)
        } else if (PermissionsUtil.getDeniedPermissions(activity, mPermissions).isNotEmpty()) { //如果有权限被禁止，进行权限请求
            requestPermissions(activity, body)
        } else { // 所有权限都被允许，使用原生权限管理检验
            val permissionList = PermissionsUtil.getPermissionDenied(activity, mPermissions)
            body(permissionList,mPermissions)
        }
    }


    /**
     * 权限被禁，进行权限请求
     */
    private fun requestPermissions(activity: Activity, body: (Array<String>, Array<String>) -> Unit) {
        val TAG = javaClass.name
        val fragmentManager = activity.fragmentManager
        var fragment: PermissionsFragment? = fragmentManager.findFragmentByTag(TAG) as PermissionsFragment?

        if (fragment == null) {
            fragment = PermissionsFragment()
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        fragment.permissionsRequest(mPermissions, body)

    }

    companion object {

        fun with(activity: Activity): PermissionsCheck {
            return PermissionsCheck(activity)
        }
    }

}