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
    protected static final String REQUEST_PERMISSIONS = "requestPermissions";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    protected void permissionsRequest() {
        String[] permissions = getArguments().getStringArray(REQUEST_PERMISSIONS);
        if (permissions == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, mRequestCode);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String[] permissions, @Nullable int[] grantResults) {
        if (mRequestCode == requestCode) {
            PermissionsCheck.getInstance(getActivity()).requestPermissionsResult(getActivity(),permissions);
        }
    }

}
