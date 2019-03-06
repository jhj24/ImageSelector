package com.jhj.imageselector

import android.app.Activity
import android.content.Intent
import android.media.Image
import android.os.Parcelable

import com.jhj.imageselector.activityresult.ActivityResult
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.ui.ImagePreviewActivity
import com.jhj.imageselector.ui.ImageSelectorActivity
import com.jhj.imageselector.utils.*

class ImageSelector private constructor(private val mActivity: Activity) {

    @JvmOverloads
    fun singleSelected(localMedia: LocalMedia, body: (List<LocalMedia>) -> Unit = {}) {
        val list = arrayListOf<LocalMedia>(localMedia)
        imageSelected(ImageExtra.SINGLE, list, body = body)
    }

    fun multiSelected(imageList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9, selectedMinNum: Int = 1, body: (List<LocalMedia>) -> Unit = {}) {
        imageSelected(
                imageList = imageList,
                selectedMaxNum = selectedMaxNum,
                selectedMinNum = selectedMinNum,
                body = body)
    }

    @JvmOverloads
    fun imageSelected(selectedMode: Int = ImageExtra.MULTI, imageList: List<LocalMedia> = arrayListOf(), selectedMaxNum: Int = 9, selectedMinNum: Int = 1, body: (List<LocalMedia>) -> Unit = {}) {
        ActivityResult.with(mActivity)
                .targetActivity(ImageSelectorActivity::class.java)
                .putParcelableArrayList(ImageExtra.IMAGE_SELECTED_LIST, imageList.toArrayList())
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
    fun imagePreview(imageList: List<LocalMedia>, currentIndex: Int = 0, isDelete: Boolean = false, body: (List<LocalMedia>) -> Unit = {}) {
        val intent = Intent(mActivity, ImagePreviewActivity::class.java)
        intent.putParcelableArrayListExtra(ImageExtra.IMAGE_LIST, imageList.toArrayList<Parcelable>())
        intent.putExtra(ImageExtra.IMAGE_INDEX, currentIndex)
        intent.putExtra(ImageExtra.IMAGE_IS_DELETE, isDelete)
        ActivityResult.with(mActivity)
                .putParcelableArrayList(ImageExtra.IMAGE_LIST, imageList.toArrayList<Parcelable>())
                .putInt(ImageExtra.IMAGE_INDEX, currentIndex)
                .putBoolean(ImageExtra.IMAGE_IS_DELETE, isDelete)
                .targetActivity(ImagePreviewActivity::class.java)
                .onResult { data ->
                    val list = data.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_SELECTED_RESULT).orEmpty()
                    body(list)
                }
        mActivity.overridePendingTransition(R.anim.activity_fade_out, 0)
    }

    companion object {
        fun with(activity: Activity): ImageSelector {
            return ImageSelector(activity)
        }
    }
}