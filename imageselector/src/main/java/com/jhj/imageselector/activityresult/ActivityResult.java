package com.jhj.imageselector.activityresult;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

public final class ActivityResult {

    private Activity mActivity;
    private Class<? extends Activity> targetActivity;
    private Bundle bundle = new Bundle();

    private ActivityResult(Activity activity) {
        this.mActivity = activity;
    }

    public static ActivityResult with(Activity activity) {
        return new ActivityResult(activity);
    }

    public ActivityResult putByte(String key, byte value) {
        bundle.putByte(key, value);
        return this;
    }


    public ActivityResult putChar(String key, char value) {
        bundle.putChar(key, value);
        return this;
    }

    public ActivityResult putShort(String key, short value) {
        bundle.putShort(key, value);
        return this;
    }

    public ActivityResult putInt(String key, int value) {
        bundle.putInt(key, value);
        return this;
    }


    public ActivityResult putFloat(String key, float value) {
        bundle.putFloat(key, value);
        return this;
    }

    public ActivityResult putDouble(String key, Double value) {
        bundle.putDouble(key, value);
        return this;
    }

    public ActivityResult putLong(String key, Long value) {
        bundle.putLong(key, value);
        return this;
    }

    public ActivityResult putString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }

    public ActivityResult putBoolean(String key, boolean value) {
        bundle.putBoolean(key, value);
        return this;
    }

    public ActivityResult putCharSequence(String key, CharSequence value) {
        bundle.putCharSequence(key, value);
        return this;
    }

    public ActivityResult putParcelable(String key, Parcelable value) {
        bundle.putParcelable(key, value);
        return this;
    }

    public ActivityResult putDoubleArray(String key, double[] value) {
        bundle.putDoubleArray(key, value);
        return this;
    }

    public ActivityResult putLongArray(String key, long[] value) {
        bundle.putLongArray(key, value);
        return this;
    }

    public ActivityResult putIntArray(String key, int[] value) {
        bundle.putIntArray(key, value);
        return this;
    }

    public ActivityResult putBooleanArray(String key, boolean[] value) {
        bundle.putBooleanArray(key, value);
        return this;
    }

    public ActivityResult putStringArray(String key, String[] value) {
        bundle.putStringArray(key, value);
        return this;
    }

    public ActivityResult putParcelableArray(String key, Parcelable[] value) {
        bundle.putParcelableArray(key, value);
        return this;
    }

    public ActivityResult putParcelableArrayList(String key,
                                                 ArrayList<? extends Parcelable> value) {
        bundle.putParcelableArrayList(key, value);
        return this;
    }


    public ActivityResult putSparseParcelableArray(String key,
                                                   SparseArray<? extends Parcelable> value) {
        bundle.putSparseParcelableArray(key, value);
        return this;
    }

    public ActivityResult putIntegerArrayList(String key, ArrayList<Integer> value) {
        bundle.putIntegerArrayList(key, value);
        return this;
    }

    public ActivityResult putStringArrayList(String key, ArrayList<String> value) {
        bundle.putStringArrayList(key, value);
        return this;
    }


    public ActivityResult putCharSequenceArrayList(String key,
                                                   ArrayList<CharSequence> value) {
        bundle.putCharSequenceArrayList(key, value);
        return this;
    }

    public ActivityResult putSerializable(String key, Serializable value) {
        bundle.putSerializable(key, value);
        return this;
    }

    public ActivityResult putByteArray(String key, byte[] value) {
        bundle.putByteArray(key, value);
        return this;
    }


    public ActivityResult putShortArray(String key, short[] value) {
        bundle.putShortArray(key, value);
        return this;
    }


    public ActivityResult putCharArray(String key, char[] value) {
        bundle.putCharArray(key, value);
        return this;
    }


    public ActivityResult putFloatArray(String key, float[] value) {
        bundle.putFloatArray(key, value);
        return this;
    }


    public ActivityResult putCharSequenceArray(String key, CharSequence[] value) {
        bundle.putCharSequenceArray(key, value);
        return this;
    }


    public ActivityResult putBundle(String key, Bundle value) {
        bundle.putBundle(key, value);
        return this;
    }

    public ActivityResult putAll(Bundle bundle) {
        this.bundle.putAll(bundle);
        return this;
    }


    public ActivityResult targetActivity(Class<? extends Activity> targetActivity) {
        this.targetActivity = targetActivity;
        return this;
    }


    public void onResult(OnActivityResultListener listener) {
        if (mActivity == null) {
            return;
        }
        String TAG = getClass().getName();
        FragmentManager fragmentManager = mActivity.getFragmentManager();
        OnResultFragment fragment = (OnResultFragment) fragmentManager.findFragmentByTag(TAG);

        if (fragment == null) {
            fragment = new OnResultFragment();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        if (targetActivity != null) {
            fragment.startActivityForResult(targetActivity, bundle, listener);
        }

    }

    public interface OnActivityResultListener {
        void onResult(Intent data);
    }
}