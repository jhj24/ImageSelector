package com.jhj.imageselector.ui

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.jhj.imageselector.R
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageConfig
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.utils.getResColor
import com.jhj.imageselector.utils.getResDrawable
import com.jhj.imageselector.utils.toArrayList
import kotlinx.android.synthetic.main.layout_image_selector_bottom.*
import kotlinx.android.synthetic.main.layout_image_selector_topbar.*
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.textColor

open class BaseImageActivity : AppCompatActivity() {
    protected var config = ImageConfig.getInstance()

    protected lateinit var selectedAnim: Animation
    protected var previewTextColor = config.previewTextColor
    protected var previewNumBackground = config.previewNumBackground
    protected var statusColor = config.statusBarDark
    protected var toolbarColor = config.topbarPrimary
    protected var topBarBackImage = config.titleLeftBackImage
    protected var titleTextColor = config.titleTextColor
    protected var rightTextColor = config.rightTextColor
    protected var selectedStateImage = config.selectedStateImage
    protected var unSelectedStateImage = config.unSelectedStateImage

    protected var bottomBackgroundColor = config.bottomBackgroundColor

    protected var isImageEditable = false
    protected var selectedMaxNum: Int = config.maxSelectNum //最大选择数量
    protected var selectedMinNum: Int = config.minSelectNum //最小选择数量
    protected var selectedMode: Int = config.selectMode //图片是单选、多选
    protected var imageSelectedList = ArrayList<LocalMedia>() //被选中的图片


    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        selectedMode = intent.getIntExtra(ImageExtra.EXTRA_SELECTED_MODE, config.selectMode)
        selectedMaxNum = intent.getIntExtra(ImageExtra.EXTRA_SELECTED_MAX_NUM, config.maxSelectNum)
        selectedMinNum = intent.getIntExtra(ImageExtra.EXTRA_SELECTED_MIN_NUM, config.minSelectNum)
        imageSelectedList = intent.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_IMAGE_SELECTED_LIST).toArrayList()

        iv_image_selector_back.setImageDrawable(getResDrawable(topBarBackImage))
        iv_image_selector_back.setOnClickListener {
            closeActivity()
        }

        tv_image_selector_title.textColor = getResColor(titleTextColor)
        tv_image_selector_right.textColor = getResColor(rightTextColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(getResColor(statusColor))
        selectedAnim = AnimationUtils.loadAnimation(this, R.anim.selected_image_num_in)
    }

    protected fun updateSelectedNum(selectImageSize: Int, previewText: String) {
        layout_bottom_preview.visibility = View.VISIBLE
        if (selectImageSize > 0) {
            tv_image_num.visibility = View.VISIBLE
            tv_image_num.text = selectImageSize.toString()
            tv_image_num.startAnimation(selectedAnim)
            tv_image_num.backgroundDrawable = getResDrawable(previewNumBackground)
            tv_image_preview.text = previewText
            tv_image_preview.textColor = getResColor(previewTextColor)
        } else {
            tv_image_num.text = "0"
            tv_image_num.visibility = View.GONE
            tv_image_preview.text = "请选择"
            tv_image_preview.textColor = 0xff999999.toInt()
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(@ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = color
            }
        }
    }

    fun imageNoCache(path: Any, imageView: ImageView) {
        val noCacheOptions = RequestOptions()
                .placeholder(R.mipmap.ic_image_selector_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
        Glide.with(this)
                .asBitmap()
                .load(path)
                .apply(noCacheOptions)
                .into(imageView)
    }

    override fun onBackPressed() {
        closeActivity()
    }

    fun closeActivity() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

}