package com.jhj.imageselector

import android.app.Activity
import android.content.Intent
import android.os.Parcelable

import com.jhj.imageselector.activityresult.ActivityResult
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.ui.ImagePreviewActivity
import com.jhj.imageselector.ui.ImageSelectorActivity
import com.jhj.imageselector.utils.*

object ImageSelector {

    @JvmOverloads
    fun singleSelected(mActivity: Activity, localMedia: LocalMedia, body: (List<LocalMedia>) -> Unit = {}) {
        val list = arrayListOf<LocalMedia>(localMedia)
        imageSelected(mActivity, ImageExtra.SINGLE, list, body = body)
    }

    fun multiSelected(mActivity: Activity, imageList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9, selectedMinNum: Int = 1, body: (List<LocalMedia>) -> Unit = {}) {
        imageSelected(
                mActivity = mActivity,
                imageList = imageList,
                selectedMaxNum = selectedMaxNum,
                selectedMinNum = selectedMinNum,
                body = body)
    }

    @JvmOverloads
    fun imageSelected(mActivity: Activity, selectedMode: Int = ImageExtra.MULTI, imageList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9, selectedMinNum: Int = 1, body: (List<LocalMedia>) -> Unit = {}) {
        ActivityResult.with(mActivity)
                .targetActivity(ImageSelectorActivity::class.java)
                .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_SELECTED_LIST, imageList.toArrayList())
                .putInt(ImageExtra.EXTRA_SELECTED_MODE, selectedMode)
                .putInt(ImageExtra.EXTRA_SELECTED_MAX_NUM, selectedMaxNum)
                .putInt(ImageExtra.EXTRA_SELECTED_MIN_NUM, selectedMinNum)
                .onResult { data ->
                    val list = data.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }
        mActivity.overridePendingTransition(R.anim.activity_fade_out, 0)
    }

    @JvmOverloads
    fun imagePreview(mActivity: Activity, imageList: List<LocalMedia>, currentIndex: Int = 0, isDelete: Boolean = false, body: (List<LocalMedia>) -> Unit = {}) {
        val intent = Intent(mActivity, ImagePreviewActivity::class.java)
        intent.putParcelableArrayListExtra(ImageExtra.EXTRA_IMAGE_LIST, imageList.toArrayList<Parcelable>())
        intent.putExtra(ImageExtra.EXTRA_IMAGE_INDEX, currentIndex)
        intent.putExtra(ImageExtra.EXTRA_IMAGE_IS_DELETE, isDelete)
        ActivityResult.with(mActivity)
                .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_LIST, imageList.toArrayList<Parcelable>())
                .putInt(ImageExtra.EXTRA_IMAGE_INDEX, currentIndex)
                .putBoolean(ImageExtra.EXTRA_IMAGE_IS_DELETE, isDelete)
                .targetActivity(ImagePreviewActivity::class.java)
                .onResult { data ->
                    val list = data.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }
        mActivity.overridePendingTransition(R.anim.activity_fade_out, 0)
    }
}