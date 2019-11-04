package com.jhj.imageselector.permissions

import android.app.Fragment
import android.os.Build
import android.os.Bundle


/**
 * 请求权限。
 * Created by jianhaojie on 2017/5/24.
 */

class PermissionsFragment : Fragment() {

    private val mRequestCode = 0x10000000
    private var allPermissions: Array<String> = arrayOf()
    private var body: ((Array<String>, Array<String>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }


    fun permissionsRequest(allPermissions: Array<String>, body: (Array<String>, Array<String>) -> Unit) {
        this.body = body
        this.allPermissions = allPermissions
        val deniedPermissions = PermissionsUtil.getDeniedPermissions(activity, allPermissions)
        if (deniedPermissions.isEmpty()) {
            return
        }
        //对被禁止的权限进行请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(deniedPermissions, mRequestCode)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>?, grantResults: IntArray?) {
        if (mRequestCode == requestCode) {
            val deniedPermissions = PermissionsUtil.getPermissionDenied(activity, allPermissions)
            body?.let { it(deniedPermissions, allPermissions) }
        }
    }
}
