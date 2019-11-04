package com.jhj.imageselector.activityresult

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable
import java.util.*

class ActivityResult private constructor(private val mActivity: Activity?) {
    private var mTargetActivity: Class<out Activity>? = null
    private val mBundle = Bundle()
    private var mIntent: Intent? = null

    fun putByte(key: String, value: Byte): ActivityResult {
        mBundle.putByte(key, value)
        return this
    }

    fun putChar(key: String, value: Char): ActivityResult {
        mBundle.putChar(key, value)
        return this
    }

    fun putShort(key: String, value: Short): ActivityResult {
        mBundle.putShort(key, value)
        return this
    }

    fun putInt(key: String, value: Int): ActivityResult {
        mBundle.putInt(key, value)
        return this
    }


    fun putFloat(key: String, value: Float): ActivityResult {
        mBundle.putFloat(key, value)
        return this
    }

    fun putDouble(key: String, value: Double): ActivityResult {
        mBundle.putDouble(key, value)
        return this
    }

    fun putLong(key: String, value: Long): ActivityResult {
        mBundle.putLong(key, value)
        return this
    }

    fun putString(key: String, value: String): ActivityResult {
        mBundle.putString(key, value)
        return this
    }

    fun putBoolean(key: String, value: Boolean): ActivityResult {
        mBundle.putBoolean(key, value)
        return this
    }

    fun putCharSequence(key: String, value: CharSequence): ActivityResult {
        mBundle.putCharSequence(key, value)
        return this
    }

    fun putParcelable(key: String, value: Parcelable): ActivityResult {
        mBundle.putParcelable(key, value)
        return this
    }

    fun putDoubleArray(key: String, value: DoubleArray): ActivityResult {
        mBundle.putDoubleArray(key, value)
        return this
    }

    fun putLongArray(key: String, value: LongArray): ActivityResult {
        mBundle.putLongArray(key, value)
        return this
    }

    fun putIntArray(key: String, value: IntArray): ActivityResult {
        mBundle.putIntArray(key, value)
        return this
    }

    fun putBooleanArray(key: String, value: BooleanArray): ActivityResult {
        mBundle.putBooleanArray(key, value)
        return this
    }

    fun putStringArray(key: String, value: Array<String>): ActivityResult {
        mBundle.putStringArray(key, value)
        return this
    }

    fun putParcelableArray(key: String, value: Array<Parcelable>): ActivityResult {
        mBundle.putParcelableArray(key, value)
        return this
    }

    fun putParcelableArrayList(key: String,
                               value: ArrayList<out Parcelable>): ActivityResult {
        mBundle.putParcelableArrayList(key, value)
        return this
    }


    fun putSparseParcelableArray(key: String,
                                 value: SparseArray<out Parcelable>): ActivityResult {
        mBundle.putSparseParcelableArray(key, value)
        return this
    }

    fun putIntegerArrayList(key: String, value: ArrayList<Int>): ActivityResult {
        mBundle.putIntegerArrayList(key, value)
        return this
    }

    fun putStringArrayList(key: String, value: ArrayList<String>): ActivityResult {
        mBundle.putStringArrayList(key, value)
        return this
    }


    fun putCharSequenceArrayList(key: String,
                                 value: ArrayList<CharSequence>): ActivityResult {
        mBundle.putCharSequenceArrayList(key, value)
        return this
    }

    fun putSerializable(key: String, value: Serializable): ActivityResult {
        mBundle.putSerializable(key, value)
        return this
    }

    fun putByteArray(key: String, value: ByteArray): ActivityResult {
        mBundle.putByteArray(key, value)
        return this
    }


    fun putShortArray(key: String, value: ShortArray): ActivityResult {
        mBundle.putShortArray(key, value)
        return this
    }


    fun putCharArray(key: String, value: CharArray): ActivityResult {
        mBundle.putCharArray(key, value)
        return this
    }


    fun putFloatArray(key: String, value: FloatArray): ActivityResult {
        mBundle.putFloatArray(key, value)
        return this
    }


    fun putCharSequenceArray(key: String, value: Array<CharSequence>): ActivityResult {
        mBundle.putCharSequenceArray(key, value)
        return this
    }


    fun putBundle(key: String, value: Bundle): ActivityResult {
        mBundle.putBundle(key, value)
        return this
    }

    fun putAll(bundle: Bundle): ActivityResult {
        this.mBundle.putAll(bundle)
        return this
    }

    fun targentIntent(intent: Intent): ActivityResult {
        this.mIntent = intent
        return this
    }


    fun targetActivity(targetActivity: Class<out Activity>): ActivityResult {
        this.mTargetActivity = targetActivity
        return this
    }

    fun onResult(body: (Intent?) -> Unit) {
        if (mActivity == null) {
            return
        }
        val TAG = javaClass.name
        val fragmentManager = mActivity.fragmentManager
        var fragment: OnResultFragment? = fragmentManager.findFragmentByTag(TAG) as OnResultFragment?

        try {
            if (fragment == null) {
                fragment = OnResultFragment()
                fragmentManager
                        .beginTransaction()
                        .add(fragment, TAG)
                        .commitAllowingStateLoss()
                fragmentManager.executePendingTransactions()
            }

            if (mTargetActivity != null) {
                mTargetActivity?.let {
                    fragment.startActivityForResult(it, mBundle, body)
                }
            } else if (mIntent != null) {
                mIntent?.let {
                    fragment.startActivityForResult(it, body)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun finish() {
        val intent = Intent()
        intent.putExtras(mBundle)
        mActivity?.setResult(Activity.RESULT_OK, intent)
        mActivity?.finish()
    }

    companion object {

        fun with(activity: Activity): ActivityResult {
            return ActivityResult(activity)
        }
    }
}