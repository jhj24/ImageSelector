package com.jhj.imageselector.ui

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.jhj.imageselector.ImageExtra
import com.jhj.imageselector.LocalMedia
import com.jhj.imageselector.R
import kotlinx.android.synthetic.main.activity_image_view_pager.*
import uk.co.senab.photoview.PhotoView

/**
 * 图片预览
 *
 * Created by jhj on 19-1-15.
 */
class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var imageList: MutableList<LocalMedia>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view_pager)

        imageList = (intent.getSerializableExtra(ImageExtra.IMAGE_LIST) as List<LocalMedia>?).orEmpty().toMutableList()
        var imageIndex = intent.getIntExtra(ImageExtra.IMAGE_INDEX, 0)
        val imageIsEditable = intent.getBooleanExtra(ImageExtra.IMAGE_IS_EDITABLE, false)

        imageViewPager.offscreenPageLimit = imageList.size
        imageViewPager.adapter = pageAdapter
        imageViewPager.currentItem = imageIndex
        imageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                imageIndex = position
                tv_image_index.text = "${position + 1}/${imageList.size}"
            }

        })

        tv_image_index.text = "${imageIndex + 1}/${imageList.size}"
        tv_image_delete.visibility = if (imageIsEditable) View.VISIBLE else View.GONE
        tv_image_delete.setOnClickListener {

            val isLeftSweep = imageIndex < imageList.size - 1
            imageList.removeAt(imageIndex)
            if (imageList.size <= 0) {
                finish()
                return@setOnClickListener
            }

            val currentIndex = if (imageIndex < imageList.size) imageIndex else imageList.size - 1
            imageViewPager.adapter?.notifyDataSetChanged()
            imageViewPager.currentItem = currentIndex
            tv_image_index.text = "${currentIndex + 1}/${imageList.size}"

            val animInRes = if (isLeftSweep) {
                R.anim.anim_image_in_left
            } else {
                R.anim.anim_image_in_right
            }
            val animIn = AnimationUtils.loadAnimation(this@ImagePreviewActivity, animInRes)
            animIn.fillAfter = true
            imageViewPager.startAnimation(animIn)
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
                finish()
                overridePendingTransition(0, R.anim.a3)
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
}