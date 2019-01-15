package com.jhj.imageselector

import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image_view_pager.*
import uk.co.senab.photoview.PhotoView

/**
 * 图片预览
 *
 * Created by jhj on 19-1-15.
 */
class ImageViewPagerActivity : AppCompatActivity() {

    private lateinit var imageList: MutableList<ImageModel>
    private var currentImage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view_pager)

        val imageIndex = intent.getIntExtra(ImageConfig.IMAGE_INDEX, 0)
        val imageIsEditable = intent.getBooleanExtra(ImageConfig.IMAGE_IS_EDITABLE, false)
        imageList = (intent.getSerializableExtra(ImageConfig.IMAGE_LIST) as List<ImageModel>?).orEmpty().toMutableList()
        currentImage = imageIndex

        tv_image_index.text = "${imageIndex + 1}/${imageList.size}"
        tv_image_delete.visibility = if (imageIsEditable) View.VISIBLE else View.GONE
        tv_image_delete.setOnClickListener {
            imageList.removeAt(currentImage)
            imageViewPager.adapter = pageAdapter
            val currentIndex = if (imageIndex < imageList.size) imageIndex else imageList.size - 1
            imageViewPager.currentItem = currentIndex
            tv_image_index.text = "${currentIndex + 1}/${imageList.size}"
        }
        imageViewPager.adapter = pageAdapter
        imageViewPager.currentItem = imageIndex
        imageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                currentImage = position
                tv_image_index.text = "${position + 1}/${imageList.size}"
            }

        })


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
            Glide
                    .with(this@ImageViewPagerActivity)
                    .load(imageList[position].imagePath)
                    .into(photoView)
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            return photoView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            if (position < imageList.size) {
                container.removeView(`object` as View?)
            }
        }
    }
}