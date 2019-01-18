package com.jhj.imageselector.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
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
import java.io.File
import java.util.*

/**
 * 图片选择
 *
 * Created by jhj on 19-1-16.
 */
open class ImageSelectorActivity : AppCompatActivity() {


    private lateinit var config: PictureSelectionConfig
    private var isAllowTakePhoto = true
    private val DURATION = 450
    private val zoomAnim: Boolean = false
    private var mSelectMode = PictureConfig.SINGLE
    private var foldersList: List<LocalMediaFolder> = ArrayList()
    private var cameraPath: String? = null
    private var outputCameraPath: String? = null

    /**
     * 最近一次选择的imageView
     */
    private var lastTimeSelectedImageView: ImageView? = null

    /**
     * 单选图片
     */
    private var isGo: Boolean = false

    private var selectImages = ArrayList<LocalMedia>()
    private var animation: Animation? = null
    private val maxSelectNum: Int = 9

    private val list = arrayListOf<Any>()


    private lateinit var adapter: SlimAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)
        config = PictureSelectionConfig.getInstance()
        animation = OptAnimationLoader.loadAnimation(this, R.anim.modal_in)

        picture_recycler.setHasFixedSize(true)
        picture_recycler.addItemDecoration(GridSpacingItemDecoration(4,
                (2 * resources.displayMetrics.density).toInt(), false))
        picture_recycler.layoutManager = GridLayoutManager(this, 4)

        MediaLoading.loadMedia(this, false) {
            foldersList = it


            if (isAllowTakePhoto) {
                list.add(Camera())
            }
            list.addAll(it[0].images)
            adapter = SlimAdapter.creator(GridLayoutManager(this, 4))
                    .register<LocalMedia>(R.layout.layout_grid_image) { viewInjector, localMedia, i ->
                        viewInjector
                                .with<ImageView>(R.id.iv_picture) {
                                    Glide.with(this)
                                            .load(localMedia.path)
                                            .into(it)

                                }
                                .with<ImageView>(R.id.iv_image_state) {
                                    it.isSelected = localMedia.isChecked
                                }
                                .clicked {
                                    changeCheckboxState(viewInjector, localMedia, i)
                                }
                    }
                    .register<Camera>(R.layout.layout_item_camera) { viewInjector, localMedia, i ->
                        viewInjector.clicked {
                            if (selectImages.size >= maxSelectNum) {
                                toast("你最多可以选择${maxSelectNum}张图片")
                                return@clicked
                            }
                            startOpenCamera()
                        }
                    }
                    .attachTo(picture_recycler)
                    .setDataList(list)
        }
    }


    private fun startOpenCamera() {
        //Todo 权限请求
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            val type = if (config.mimeType == PictureConfig.TYPE_ALL)
                PictureConfig.TYPE_IMAGE
            else
                config.mimeType
            val cameraFile = PictureFileUtils.createCameraFile(this, type, outputCameraPath, config.suffixType)
            cameraPath = cameraFile.absolutePath
            val imageUri = parUri(cameraFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA)
        }
    }

    /**
     * 生成uri
     *
     * @param cameraFile
     * @return
     */
    private fun parUri(cameraFile: File): Uri {
        val imageUri: Uri
        val authority = packageName + ".provider"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(this, authority, cameraFile)
        } else {
            imageUri = Uri.fromFile(cameraFile)
        }
        return imageUri
    }


    /**
     * 改变图片选中状态
     *
     * @param contentHolder
     * @param localMedia
     */

    private fun changeCheckboxState(injector: ViewInjector, localMedia: LocalMedia, position: Int) {
        val isChecked = localMedia.isChecked
        val imageView = injector.getView<ImageView>(R.id.iv_picture)
        val pictureType = if (selectImages.size > 0) selectImages.get(0).pictureType else ""
        if (!TextUtils.isEmpty(pictureType)) {
            val toEqual = MediaMimeType.mimeToEqual(pictureType, localMedia.pictureType)
            if (!toEqual) {
                toast("不能同时选择图片或视频")
                return
            }
        }
        //达到最大选择数，点击未选中的Image
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
                if (media.path == localMedia.path) {
                    selectImages.remove(media)
                    disZoom(imageView)
                    break
                }
            }
        } else {
            lastTimeSelectedImageView = imageView
            // 如果是单选，则清空已选中的并刷新列表(作单一选择)
            if (mSelectMode == PictureConfig.SINGLE) {
                singleRadioMediaImage()
            }
            selectImages.add(localMedia)
            localMedia.num = selectImages.size
            zoom(imageView)
        }
        localMedia.isChecked = !isChecked
        selectImage(injector, !isChecked, true)

        //changeImageNumber(selectImages)
    }

    /**
     * 单选模式
     */
    private fun singleRadioMediaImage() {
        if (selectImages.size > 0) {
            selectImages.clear()
            list.forEachIndexed { index, it ->
                if (it is LocalMedia && it.isChecked) {
                    it.isChecked = false
                    lastTimeSelectedImageView?.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
                    adapter.notifyItemChanged(index)
                    return
                }
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
    private fun selectImage(injector: ViewInjector, isChecked: Boolean, isAnim: Boolean) {
        val checkStateView = injector.getView<ImageView>(R.id.iv_image_state)
        val photoView = injector.getView<ImageView>(R.id.iv_picture)
        checkStateView.isSelected = isChecked

        if (isChecked) {
            if (isAnim && animation != null) {
                checkStateView.startAnimation(animation)
            }
            photoView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_true), PorterDuff.Mode.SRC_ATOP)
        } else {
            photoView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
        }
    }

    /**
     * 图片放大动画
     */
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

    /**
     * 图片缩小动画
     */
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

    /**
     * 手动添加拍照后的相片到图片列表，并设为选中
     *
     * @param media
     */
    private fun manualSaveFolder(media: LocalMedia) {
        try {
            createNewFolder(foldersList.toMutableList())
            val folder = getImageFolder(media.path, foldersList.toMutableList())
            val cameraFolder = if (foldersList.isNotEmpty()) foldersList[0] else null
            if (cameraFolder != null && folder != null) {
                // 相机胶卷
                cameraFolder.firstImagePath = media.path
                cameraFolder.images = foldersList[0].images
                cameraFolder.imageNum = cameraFolder.imageNum + 1
                // 拍照相册
                val num = folder.imageNum + 1
                folder.imageNum = num
                folder.images.add(0, media)
                folder.firstImagePath = cameraPath
                //folderWindow.bindFolder(foldersList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 将图片插入到相机文件夹中
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private fun getImageFolder(path: String, imageFolders: MutableList<LocalMediaFolder>): LocalMediaFolder {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile

        for (folder in imageFolders) {
            if (folder.name.equals(folderFile.name)) {
                return folder
            }
        }
        val newFolder = LocalMediaFolder()
        newFolder.name = folderFile.name
        newFolder.path = folderFile.absolutePath
        newFolder.firstImagePath = path
        imageFolders.add(newFolder)
        return newFolder
    }

    /**
     * 如果没有任何相册，先创建一个最近相册出来
     *
     * @param folders
     */
    private fun createNewFolder(folders: MutableList<LocalMediaFolder>) {
        if (folders.size == 0) {
            // 没有相册 先创建一个最近相册出来
            val newFolder = LocalMediaFolder()
            val folderName = if (config.mimeType === MediaMimeType.ofAudio())
                "所有音频"
            else
                "相机交卷"
            newFolder.name = folderName
            newFolder.path = ""
            newFolder.firstImagePath = ""
            folders.add(newFolder)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && PictureConfig.REQUEST_CAMERA == requestCode) {
            val file = File(cameraPath)
            //启动MediaScanner服务，扫描媒体文件
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

            // 生成新拍照片或视频对象
            val media = LocalMedia()
            val pictureType = MediaMimeType.createImageType(cameraPath)
            media.path = cameraPath
            media.pictureType = pictureType
            media.duration = 0
            media.isChecked = true
            media.mimeType = config.mimeType
            selectImages.add(media)

            /* if (config.selectionMode == PictureConfig.SINGLE) {
                 singleRadioMediaImage()
             }*/


            adapter.addData(1, media)


            // 解决部分手机拍照完Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,不及时刷新问题手动添加
            //manualSaveFolder(media)

        }

    }


    private class Camera

}