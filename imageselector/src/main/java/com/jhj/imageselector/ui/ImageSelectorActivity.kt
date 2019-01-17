package com.jhj.imageselector.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.animation.Animation
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jhj.imageselector.*
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.holder.ViewInjector
import kotlinx.android.synthetic.main.activity_image_selector.*
import org.jetbrains.anko.toast
import java.util.*

/**
 * Created by jhj on 19-1-16.
 */
class ImageSelectorActivity : AppCompatActivity() {

    private var isAllowTakePhoto = false
    private val DURATION = 450
    private val zoomAnim: Boolean = false
    private val is_checked_num: Boolean = true
    private val selectMode = PictureConfig.MULTIPLE
    /**
     * 单选图片
     */
    private var isGo: Boolean = false

    private var selectImages = ArrayList<LocalMedia>()
    private var animation: Animation? = null
    private val maxSelectNum: Int = 9


    private var adapter: SlimAdapter? = null

    private var imageSelectChangedListener: OnPhotoSelectChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)

        animation = OptAnimationLoader.loadAnimation(this, R.anim.modal_in)

        picture_recycler.setHasFixedSize(true)
        picture_recycler.addItemDecoration(GridSpacingItemDecoration(4,
                (2 * resources.displayMetrics.density).toInt(), false))
        picture_recycler.layoutManager = GridLayoutManager(this, 4)

        MediaLoading.loadMedia(this, false) {
            adapter = SlimAdapter.creator(GridLayoutManager(this, 4))
                    .register<LocalMedia>(R.layout.layout_grid_image) { viewInjector, localMedia, i ->
                        viewInjector
                                .with<ImageView>(R.id.iv_picture) {
                                    Glide.with(this)
                                            .load(localMedia.path)
                                            .into(it)

                                }
                                .clicked {
                                    changeCheckboxState(viewInjector, localMedia, i)
                                }


                    }
                    .attachTo(picture_recycler)
                    .setDataList(it[0].images)
        }


    }


    /**
     * 改变图片选中状态
     *
     * @param contentHolder
     * @param image
     */

    private fun changeCheckboxState(injector: ViewInjector, image: LocalMedia, position: Int) {
        val isChecked = injector.getView<ImageView>(R.id.iv_image_state).isSelected
        val pictureType = if (selectImages.size > 0) selectImages.get(0).getPictureType() else ""
        if (!TextUtils.isEmpty(pictureType)) {
            val toEqual = MediaMimeType.mimeToEqual(pictureType, image.pictureType)
            if (!toEqual) {
                toast("不能同时选择图片或视频")
                return
            }
        }
        if (selectImages.size >= maxSelectNum && !isChecked) {
            val eqImg = pictureType.startsWith(PictureConfig.IMAGE)
            val str = if (eqImg)
                "你最多可以选择${maxSelectNum}张图片"
            else
                "你最多可以选择${maxSelectNum}个视频"
            toast(str)
            return
        }

        if (isChecked) {
            for (media in selectImages) {
                if (media.path == image.path) {
                    selectImages.remove(media)
                    subSelectPosition()
                    disZoom(injector.getView(R.id.iv_picture))
                    break
                }
            }
        } else {
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
            if (selectMode == PictureConfig.SINGLE) {
                singleRadioMediaImage()
            }
            selectImages.add(image)
            image.num = selectImages.size
            //VoiceUtils.playVoice(context, enableVoice)
            zoom(injector.getView(R.id.iv_picture))
        }
        //通知点击项发生了改变
        adapter?.notifyItemChanged(position)
        selectImage(injector, !isChecked, true)
        if (imageSelectChangedListener != null) {
            imageSelectChangedListener?.onChange(selectImages)
        }
    }

    /**
     * 单选模式
     */
    private fun singleRadioMediaImage() {
        if (selectImages != null && selectImages.size > 0) {
            isGo = true
            val media = selectImages[0]
            adapter?.notifyItemChanged(
                    if (false)
                        media.position
                    else if (isGo)
                        media.position
                    else if (media.position > 0)
                        media.position - 1
                    else 0
            )
            selectImages.clear()
        }
    }


    /**
     * 更新选择的顺序
     */
    private fun subSelectPosition() {
        if (is_checked_num) {
            val size = selectImages.size
            var index = 0
            while (index < size) {
                val media = selectImages[index]
                media.num = index + 1
                adapter?.notifyItemChanged(media.position)
                index++
            }
        }
    }

    /**
     * 选中的图片并执行动画
     *
     * @param holder
     * @param isChecked
     * @param isAnim
     */
    fun selectImage(injector: ViewInjector, isChecked: Boolean, isAnim: Boolean) {
        val checkStateView = injector.getView<ImageView>(R.id.iv_image_state)
        val photoView = injector.getView<ImageView>(R.id.iv_picture)
        checkStateView.setSelected(isChecked)
        if (isChecked) {
            if (isAnim) {
                if (animation != null) {
                    checkStateView.startAnimation(animation)
                }
            }
            photoView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_true), PorterDuff.Mode.SRC_ATOP)
        } else {
            photoView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
        }
    }

    private fun zoom(iv_img: ImageView) {
        if (zoomAnim) {
            val set = AnimatorSet()
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1f, 1.12f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1f, 1.12f)
            )
            set.duration = DURATION.toLong()
            set.start()
        }
    }

    private fun disZoom(iv_img: ImageView) {
        if (zoomAnim) {
            val set = AnimatorSet()
            set.playTogether(
                    ObjectAnimator.ofFloat(iv_img, "scaleX", 1.12f, 1f),
                    ObjectAnimator.ofFloat(iv_img, "scaleY", 1.12f, 1f)
            )
            set.duration = DURATION.toLong()
            set.start()
        }
    }


    interface OnPhotoSelectChangedListener {
        /**
         * 拍照回调
         */
        fun onTakePhoto()

        /**
         * 已选Media回调
         *
         * @param selectImages
         */
        fun onChange(selectImages: List<LocalMedia>)

        /**
         * 图片预览回调
         *
         * @param media
         * @param position
         */
        fun onPictureClick(media: LocalMedia, position: Int)
    }

    fun setOnPhotoSelectChangedListener(imageSelectChangedListener: OnPhotoSelectChangedListener) {
        this.imageSelectChangedListener = imageSelectChangedListener
    }


}