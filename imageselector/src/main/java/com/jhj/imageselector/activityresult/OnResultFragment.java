package com.jhj.imageselector.activityresult;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public final class OnResultFragment extends Fragment {

    private int ACTIVITY_CODE = 0x11000000;
    private ActivityResult.OnActivityResultListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    void startActivityForResult(Class<? extends Activity> activity, ActivityResult.OnActivityResultListener listener) {
        this.listener = listener;
        Intent intent = new Intent(getActivity(), activity);
        intent.putExtras(getArguments());
        startActivityForResult(intent, ACTIVITY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_CODE) {
            listener.onResult(data);
        }
    }
}
