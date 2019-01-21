package com.jhj.imageselector.permissions;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 请求权限。
 * Created by jianhaojie on 2017/5/24.
 */

public final class PermissionsFragment extends Fragment {

    private int mRequestCode = 0x10000000;
    private PermissionsCheck.OnPermissionsResultListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    protected void permissionsRequest(String[] mPermissions, PermissionsCheck.OnPermissionsResultListener listener) {
        this.listener = listener;
        if (mPermissions == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissions, mRequestCode);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        if (mRequestCode == requestCode) {
            List<String> deniedPermissions = PermissionsUtil.getPermissionDenied(getActivity(), permissions);
            listener.onPermissionsResult(deniedPermissions, Arrays.asList(permissions));
        }
    }
}
