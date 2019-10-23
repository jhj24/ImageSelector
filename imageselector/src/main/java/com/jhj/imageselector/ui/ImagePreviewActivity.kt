package com.jhj.imageselector.ui

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.jhj.imageselector.R
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.utils.MediaMimeType
import com.jhj.imageselector.utils.selected
import com.jhj.imageselector.utils.toArrayList
import com.zgdj.qualitycontrol.utils.activityresult.ActivityResult
import kotlinx.android.synthetic.main.activity_image_preview.*
import kotlinx.android.synthetic.main.layout_image_selector_bottom.*
import kotlinx.android.synthetic.main.layout_image_selector_topbar.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.toast
import uk.co.senab.photoview.PhotoView

/**
 * 图片预览
 *
 * Created by jhj on 19-1-15.
 *
 * 删除和编辑不可同时存在
 */
class ImagePreviewActivity : BaseImageActivity() {

    private lateinit var imageList: MutableList<LocalMedia>
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        layout_image_selector_title.backgroundColor = (255 * 0.1).toInt() shl 24 or toolbarColor
        layout_bottom_preview.backgroundColor = (255 * 0.1).toInt() shl 24 or toolbarColor

        imageList = intent.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_IMAGE_LIST).toArrayList()
        currentIndex = intent.getIntExtra(ImageExtra.EXTRA_IMAGE_INDEX, 0)
        //图片是否可选择
        val isImageSelector = intent.getBooleanExtra(ImageExtra.EXTRA_IMAGE_IS_SELECTED, false)

        tv_image_selector_title.text = "${currentIndex + 1}/${imageList.size}"

        if (isImageSelector) {
            updateSelectedNum(imageSelectedList.size, "完成")
        }
        initViewPager(currentIndex, isImageSelector)
        imageHandler()
    }


    /**
     * 初始化 ImagePager
     */
    private fun initViewPager(imageIndex: Int, isImageSelector: Boolean) {
        currentIndex = imageIndex
        imageViewPager.adapter = pageAdapter
        imageViewPager.currentItem = currentIndex
        imageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                if (ViewPager.SCROLL_STATE_IDLE == state) {
                    onPageSelected(imageViewPager.currentItem)
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val currentItem = imageViewPager.currentItem
                if (isImageSelector) {
                    setImageSelector(currentItem)
                }
                if (position == currentItem) {//向右
                    if (currentItem == imageList.size - 1)
                        return
                } else {//向左
                    if (currentItem == 0)
                        return
                }
            }

            override fun onPageSelected(position: Int) {
                currentIndex = position
                tv_image_selector_title.text = "${position + 1}/${imageList.size}"
                if (isImageSelector) {
                    setImageSelector(position)
                }
            }
        })
    }


    /**
     * 图片处理，删除或编辑，二者只能选其一
     */
    private fun imageHandler() {
        val isImageDelete = intent.getBooleanExtra(ImageExtra.EXTRA_IMAGE_IS_DELETE, false)
        if (isImageDelete) {
            //预览时，是否可删除
            tv_image_selector_right.visibility = View.VISIBLE
            tv_image_selector_right.text = "删除"
            tv_image_selector_right.setOnClickListener {

                val isLeftSweep = currentIndex < imageList.size - 1
                imageList.removeAt(currentIndex)
                if (imageList.size <= 0) {
                    ActivityResult.with(this)
                            .putParcelableArrayList(ImageExtra.EXTRA_SELECTED_RESULT, imageList.toArrayList())
                            .finish()
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
                    return@setOnClickListener
                }

                val index = if (currentIndex < imageList.size) currentIndex else imageList.size - 1
                imageViewPager.adapter?.notifyDataSetChanged()
                imageViewPager.currentItem = index

                val animInRes = if (isLeftSweep) {
                    R.anim.anim_image_delete_left
                } else {
                    R.anim.anim_image_delete_right
                }
                val animIn = AnimationUtils.loadAnimation(this@ImagePreviewActivity, animInRes)
                animIn.fillAfter = true
                imageViewPager.startAnimation(animIn)
                tv_image_selector_title.text = "${index + 1}/${imageList.size}"
            }
        }
    }

    /**
     * 图片是否选中，从 imageSelector 界面点击图片进入，图片可放大选中
     */
    private fun setImageSelector(position: Int) {
        layout_image_selector_state.visibility = View.VISIBLE
        iv_image_selector_state.isSelected = imageList[position].isChecked
        val drawable = selected(selectedStateImage, unSelectedStateImage)
        iv_image_selector_state.setImageDrawable(drawable)
        layout_image_selector_state.setOnClickListener {
            val localMedia = imageList[position]
            val pictureType = if (imageSelectedList.size > 0) imageSelectedList[0].pictureType else ""
            val isChecked = iv_image_selector_state.isSelected
            if (!TextUtils.isEmpty(pictureType)) {
                val toEqual = MediaMimeType.mimeToEqual(pictureType, localMedia.pictureType)
                if (!toEqual) {
                    toast("不能同时选择图片或视频")
                    return@setOnClickListener
                }
            }
            //达到最大选择数，点击未选中的ImageView
            if (imageSelectedList.size >= selectedMaxNum && !isChecked) {
                val eqImg = pictureType.startsWith(ImageExtra.IMAGE)
                val str = if (eqImg)
                    "你最多可以选择${selectedMaxNum}张图片"
                else
                    "你最多可以选择${selectedMaxNum}个视频"
                toast(str)
                return@setOnClickListener
            }

            iv_image_selector_state.isSelected = !isChecked
            localMedia.isChecked = !isChecked

            if (iv_image_selector_state.isSelected) {
                // 如果是单选，则清空已选中的并刷新列表(作单一选择)
                if (selectedMode == ImageExtra.SINGLE) {
                    if (imageSelectedList.size > 0) {
                        imageList.filter { image -> image.path == imageSelectedList[0].path }[0].isChecked = false
                        imageSelectedList.clear()
                    }
                }
                iv_image_selector_state.startAnimation(selectedAnim)
                imageSelectedList.add(localMedia)
                localMedia.num = imageSelectedList.size
            } else {
                val selectedList = imageSelectedList.filter { image -> image.path == localMedia.path }
                imageSelectedList.removeAll(selectedList)
            }
            updateSelectedNum(imageSelectedList.size, "完成")
        }

        //点击完成，返回选中的图片
        layout_image_preview.setOnClickListener {
            ActivityResult.with(this)
                    .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_SELECTED_LIST, imageSelectedList.toArrayList())
                    .finish()
            overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        }
    }


    private val pageAdapter = object : PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return imageList.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(container.context)
            imageNoCache(imageList[position].path, photoView)
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            photoView.setOnViewTapListener { view, x, y ->
                closeActivity()
            }
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

}