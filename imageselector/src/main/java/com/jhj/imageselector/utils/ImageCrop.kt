package com.jhj.imageselector.utils

import android.app.Activity
import android.net.Uri
import com.yalantis.ucrop.UCrop
import java.io.File

/**
 * Created by jhj on 19-1-21.
 */
object ImageCrop {

    fun startCrop(activity: Activity) {
        /*val option = UCrop.Options()


        UCrop.of(url, Uri.fromFile(File(PictureFileUtils.getDiskCacheDir(activity),
                System.currentTimeMillis() + imgType)))
                .withAspectRatio()
                .withMaxResultSize()
                .withOptions(option)
                .start(activity)*/
    }

    /**
     * 去裁剪
     *
     * @param originalPath
     */
    /* fun startCrop( originalPath: String) {
         val options = UCrop.Options()
         val toolbarColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_toolbar_bg)
         val statusColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_status_color)
         val titleColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_title_color)
         options.setToolbarColor(toolbarColor)
         options.setStatusBarColor(statusColor)
         options.setToolbarWidgetColor(titleColor)
         options.setCircleDimmedLayer(config.circleDimmedLayer)
         options.setShowCropFrame(config.showCropFrame)
         options.setShowCropGrid(config.showCropGrid)
         options.setDragFrameEnabled(config.isDragFrame)
         options.setScaleEnabled(config.scaleEnabled)
         options.setRotateEnabled(config.rotateEnabled)
         options.setCompressionQuality(config.cropCompressQuality)
         options.setHideBottomControls(config.hideBottomControls)
         options.setFreeStyleCropEnabled(config.freeStyleCropEnabled)
         val isHttp = PictureMimeType.isHttp(originalPath)
         val imgType = PictureMimeType.getLastImgType(originalPath)
         val uri = if (isHttp) Uri.parse(originalPath) else Uri.fromFile(File(originalPath))
         UCrop.of(uri, Uri.fromFile(File(PictureFileUtils.getDiskCacheDir(context),
                 System.currentTimeMillis() + imgType)))
                 .withAspectRatio(config.aspect_ratio_x, config.aspect_ratio_y)
                 .withMaxResultSize(config.cropWidth, config.cropHeight)
                 .withOptions(options)
                 .start(this)
     }

     */
    /**
     * 多图去裁剪
     *
     * @param list
     *//*
    fun startCrop(list: ArrayList<String>) {
        val options = UCropMulti.Options()
        val toolbarColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_toolbar_bg)
        val statusColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_status_color)
        val titleColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_title_color)
        options.setToolbarColor(toolbarColor)
        options.setStatusBarColor(statusColor)
        options.setToolbarWidgetColor(titleColor)
        options.setCircleDimmedLayer(config.circleDimmedLayer)
        options.setShowCropFrame(config.showCropFrame)
        options.setDragFrameEnabled(config.isDragFrame)
        options.setShowCropGrid(config.showCropGrid)
        options.setScaleEnabled(config.scaleEnabled)
        options.setRotateEnabled(config.rotateEnabled)
        options.setHideBottomControls(true)
        options.setCompressionQuality(config.cropCompressQuality)
        options.setCutListData(list)
        options.setFreeStyleCropEnabled(config.freeStyleCropEnabled)
        val path = if (list.size > 0) list[0] else ""
        val isHttp = PictureMimeType.isHttp(path)
        val imgType = PictureMimeType.getLastImgType(path)
        val uri = if (isHttp) Uri.parse(path) else Uri.fromFile(File(path))
        UCropMulti.of(uri, Uri.fromFile(File(PictureFileUtils.getDiskCacheDir(this),
                System.currentTimeMillis() + imgType)))
                .withAspectRatio(config.aspect_ratio_x, config.aspect_ratio_y)
                .withMaxResultSize(config.cropWidth, config.cropHeight)
                .withOptions(options)
                .start(this)
    }*/
}