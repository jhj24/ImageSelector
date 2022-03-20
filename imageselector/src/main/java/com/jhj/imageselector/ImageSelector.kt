package com.jhj.imageselector

import android.app.Activity
import com.google.gson.Gson
import com.jhj.imageselector.activityresult.ActivityResult
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.ui.ImagePreviewActivity
import com.jhj.imageselector.ui.ImageSelectorActivity
import com.jhj.imageselector.utils.LiveDataBus
import com.jhj.imageselector.utils.toArrayList

object ImageSelector {
    fun camera(mActivity: Activity, body: (List<LocalMedia>) -> Unit) {
        ActivityResult.with(mActivity)
                .targetActivity(ImageSelectorActivity::class.java)
                .putBoolean(ImageExtra.EXTRA_IMAGE_IS_ONLY_CAMERA, true)
                .onResult { data ->
                    val list = data?.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }
        mActivity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    @JvmOverloads
    fun singleSelected(mActivity: Activity, localMedia: LocalMedia?, isImageEditable: Boolean = false, body: (List<LocalMedia>) -> Unit) {
        val list = if (localMedia == null) arrayListOf() else arrayListOf(localMedia)
        selected(mActivity, ImageExtra.SINGLE, list, isImageEditable = isImageEditable, body = body)
    }

    @JvmOverloads
    fun multiSelected(mActivity: Activity, imageSelectedList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9, selectedMinNum: Int = 1, isImageEditable: Boolean = false, body: (List<LocalMedia>) -> Unit) {
        selected(
                mActivity = mActivity,
                imageSelectedList = imageSelectedList,
                selectedMaxNum = selectedMaxNum,
                selectedMinNum = selectedMinNum,
                isImageEditable = isImageEditable,
                body = body)
    }

    @JvmOverloads
    fun selected(mActivity: Activity, selectedMode: Int = ImageExtra.MULTI, imageSelectedList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9,
                 selectedMinNum: Int = 1, isImageEditable: Boolean = false, body: (List<LocalMedia>) -> Unit = {}) {
        ActivityResult.with(mActivity)
                .targetActivity(ImageSelectorActivity::class.java)
                .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_SELECTED_LIST, imageSelectedList.toArrayList())
                .putInt(ImageExtra.EXTRA_SELECTED_MODE, selectedMode)
                .putInt(ImageExtra.EXTRA_SELECTED_MAX_NUM, selectedMaxNum)
                .putInt(ImageExtra.EXTRA_SELECTED_MIN_NUM, selectedMinNum)
                .onResult { data ->
                    val list = data?.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }
        mActivity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }


    fun preview(mActivity: Activity, imageList: List<LocalMedia>, currentIndex: Int = 0) {
        preview(mActivity, imageList, currentIndex, false, {})
    }


    fun deletePreview(mActivity: Activity, imageList: List<LocalMedia>, currentIndex: Int = 0, body: (List<LocalMedia>) -> Unit) {
        preview(mActivity, imageList, currentIndex, true, body)
    }

    fun preview(mActivity: Activity, imageList: List<LocalMedia>, currentIndex: Int = 0, isDelete: Boolean = false,
                body: (List<LocalMedia>) -> Unit = {}) {
        LiveDataBus.get().with(ImageExtra.EXTRA_IMAGE_LIST).value = Gson().toJson(imageList)
        ActivityResult.with(mActivity)
                .putInt(ImageExtra.EXTRA_IMAGE_INDEX, currentIndex)
                .putBoolean(ImageExtra.EXTRA_IMAGE_IS_DELETE, isDelete)
                .targetActivity(ImagePreviewActivity::class.java)
                .onResult { data ->
                    val list = data?.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }

        mActivity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }


}