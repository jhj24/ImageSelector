package com.jhj.imageselector.permissions;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 请求权限。
 * Created by jianhaojie on 2017/5/24.
 */

public final class PermissionsFragment extends Fragment {

    private int mRequestCode = 0x10000000;
    private PermissionsCheck.OnPermissionsResultListener listener;
    private String[] allPermissions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    protected void permissionsRequest(String[] allPermissions, PermissionsCheck.OnPermissionsResultListener resultPermissionsListener) {
        this.allPermissions = allPermissions;
        String[] deniedPermissions = PermissionsUtil.getDeniedPermissions(getActivity(), allPermissions);
        this.listener = resultPermissionsListener;
        if (deniedPermissions.length == 0 || allPermissions == null) {
            return;
        }
        //对被禁止的权限进行请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(deniedPermissions, mRequestCode);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        if (mRequestCode == requestCode) {
            String[] deniedPermissions = PermissionsUtil.getPermissionDenied(getActivity(), allPermissions);
            listener.onPermissionsResult(deniedPermissions, allPermissions);
        }
    }
}
