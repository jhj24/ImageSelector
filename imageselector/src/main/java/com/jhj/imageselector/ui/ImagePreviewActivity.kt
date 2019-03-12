package com.jhj.imageselector.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.jhj.imageselector.R
import com.jhj.imageselector.activityresult.ActivityResult
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.utils.*
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
 */
class ImagePreviewActivity : BaseImageActivity() {

    private lateinit var imageList: MutableList<LocalMedia>
    private var selectImages: ArrayList<LocalMedia> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        selectImages = intent.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_IMAGE_SELECTED_LIST).orEmpty().toArrayList()
        imageList = intent.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_IMAGE_LIST).orEmpty().toArrayList()
        var imageIndex = intent.getIntExtra(ImageExtra.EXTRA_IMAGE_INDEX, 0)
        val isImageDelete = intent.getBooleanExtra(ImageExtra.EXTRA_IMAGE_IS_DELETE, false)
        val isImageSelector = intent.getBooleanExtra(ImageExtra.EXTRA_IMAGE_IS_SELECTED, false)

        layout_image_selector_title.backgroundColor = (255 * 0.1).toInt() shl 24 or toolbarColor
        layout_bottom_preview.backgroundColor = (255 * 0.1).toInt() shl 24 or toolbarColor

        if (isImageSelector) {
            updateSelectedNum(selectImages.size, "完成")
        }

        tv_image_selector_title.text = "${imageIndex + 1}/${imageList.size}"

        tv_image_selector_right.visibility = if (isImageDelete) View.VISIBLE else View.GONE
        tv_image_selector_right.text = "删除"
        tv_image_selector_right.setOnClickListener {

            val isLeftSweep = imageIndex < imageList.size - 1
            imageList.removeAt(imageIndex)
            if (imageList.size <= 0) {
                val intent = Intent()
                intent.putParcelableArrayListExtra(ImageExtra.EXTRA_SELECTED_RESULT,selectImages);
                setResult(Activity.RESULT_OK,intent)
                closeActivity()
                return@setOnClickListener
            }

            val currentIndex = if (imageIndex < imageList.size) imageIndex else imageList.size - 1
            imageViewPager.adapter?.notifyDataSetChanged()
            imageViewPager.currentItem = currentIndex

            val animInRes = if (isLeftSweep) {
                R.anim.anim_image_delete_left
            } else {
                R.anim.anim_image_delete_right
            }
            val animIn = AnimationUtils.loadAnimation(this@ImagePreviewActivity, animInRes)
            animIn.fillAfter = true
            imageViewPager.startAnimation(animIn)
        }

        imageViewPager.adapter = pageAdapter
        imageViewPager.currentItem = imageIndex
        imageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                if (ViewPager.SCROLL_STATE_IDLE == state) {
                    onPageSelected(imageViewPager.currentItem)
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val currentItem = imageViewPager.currentItem
                if (position == currentItem) {//向右
                    if (currentItem == imageList.size - 1)
                        return
                } else {//向左
                    if (currentItem == 0)
                        return
                }
                setImageSelector(isImageSelector, currentItem)
            }

            override fun onPageSelected(position: Int) {
                imageIndex = position
                tv_image_selector_title.text = "${position + 1}/${imageList.size}"
                setImageSelector(isImageSelector, position)
            }
        })

        layout_image_preview.setOnClickListener {
            ActivityResult.with(this)
                    .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_SELECTED_LIST, selectImages.toArrayList())
                    .finish()
            overridePendingTransition(0, R.anim.activity_fade_out)
        }

    }

    private fun setImageSelector(isImageSelector: Boolean, position: Int) {
        if (isImageSelector) {
            layout_image_selector_state.visibility = View.VISIBLE
            iv_image_selector_state.isSelected = imageList[position].isChecked
            val drawable = selected(selectedStateImage, unSelectedStateImage)
            iv_image_selector_state.setImageDrawable(drawable)
            layout_image_selector_state.setOnClickListener {
                val localMedia = imageList[position]
                val pictureType = if (selectImages.size > 0) selectImages[0].pictureType else ""
                val isChecked = iv_image_selector_state.isSelected
                if (!TextUtils.isEmpty(pictureType)) {
                    val toEqual = MediaMimeType.mimeToEqual(pictureType, localMedia.pictureType)
                    if (!toEqual) {
                        toast("不能同时选择图片或视频")
                        return@setOnClickListener
                    }
                }
                //达到最大选择数，点击未选中的ImageView
                if (selectImages.size >= selectedMaxNum && !isChecked) {
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
                        if (selectImages.size > 0) {
                            imageList.filter { image -> image.path == selectImages[0].path }[0].isChecked = false
                            selectImages.clear()
                        }
                    }
                    iv_image_selector_state.startAnimation(selectedAnim)
                    selectImages.add(localMedia)
                    localMedia.num = selectImages.size
                } else {
                    val selectedList = selectImages.filter { image -> image.path == localMedia.path }
                    selectImages.removeAll(selectedList)
                }
                updateSelectedNum(selectImages.size, "完成")
            }
        }
    }


    private val pageAdapter = object : PagerAdapter() {

        private var mPrimaryItem: View? = null

        val options = RequestOptions()
                .placeholder(R.drawable.bg_image_selector_placeholder)
                .sizeMultiplier(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return imageList.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val photoView = PhotoView(container.context)
            Glide
                    .with(this@ImagePreviewActivity)
                    .asBitmap()
                    .load(imageList[position].path)
                    .apply(options)
                    .into(photoView)
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            photoView.setOnViewTapListener { view, x, y ->
                closeActivity()
            }
            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View?)
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            super.setPrimaryItem(container, position, `object`)
            this.mPrimaryItem = `object` as View
        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putParcelableArrayListExtra(ImageExtra.EXTRA_SELECTED_RESULT,selectImages)
        setResult(Activity.RESULT_OK,intent)
        super.onBackPressed()
    }
}