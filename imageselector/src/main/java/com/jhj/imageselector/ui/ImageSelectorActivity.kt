package com.jhj.imageselector.ui

import android.Manifest
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
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.jhj.imageselector.ImageSelector
import com.jhj.imageselector.R
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.bean.LocalMediaFolder
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.utils.*
import com.jhj.imageselector.weight.FolderPopWindow
import com.jhj.imageselector.weight.GridSpacingItemDecoration
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.holder.ViewInjector
import com.yalantis.ucrop.UCrop
import com.zgdj.qualitycontrol.utils.activityresult.ActivityResult
import com.zgdj.qualitycontrol.utils.permissions.PermissionsCheck
import kotlinx.android.synthetic.main.activity_image_selector.*
import kotlinx.android.synthetic.main.layout_image_selector_bottom.*
import kotlinx.android.synthetic.main.layout_image_selector_topbar.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.toast
import java.io.File

/**
 * 图片选择
 *
 * Created by jhj on 19-1-16.
 */
open class ImageSelectorActivity : BaseImageActivity() {

    private lateinit var adapter: SlimAdapter
    private var previewList: ArrayList<LocalMedia> = arrayListOf()

    //上一次选中图片的信息
    private var lastTimeSelectedInjector: ViewInjector? = null
    private var lastTimeSelectedLocalMedia: LocalMedia? = null

    //动画
    private var isSelectedImageAnim = config.isImageAnim
    private val originalSize = 1.0f
    private val zoomSize = 1.10f
    private val DURATION = 450

    //模式
    private var isOnlyCamera = config.isOnlyCamera //是否只拍照
    private var isCompress = config.isCompress //是否压缩
    private var ignoreCompressSize: Int = config.compressSize  //忽略压缩的最小限制
    private var isCrop = config.isCrop //是否剪切
    private var isAllowTakePhoto = config.isAllowTakePhoto //选择照片是否有相机

    //文件夹选择PopWindow
    private lateinit var folderWindow: FolderPopWindow
    private var foldersList: List<LocalMediaFolder> = arrayListOf()

    //拍照图片路径
    private var cameraPath: String? = null
    private var outputCameraPath: String? = null

    //标题右侧展开收起图标
    private var titleArrowDownImage = config.titleArrowDownImage
    private var titleArrowUpImage = config.titleArrowUpImage

    //剪切
    private var uCropOptions = config.uCropOptions
    private var uCropCompressQuality = config.uCropPressQuality
    private var uCropScaleX = config.uCropScaleX
    private var uCropScaleY = config.uCropScaleY
    private var uCropWidth = config.uCropWidth
    private var uCropHeight = config.uCropHeight
    private var isHideUCropBottomControl = config.isHideUCropBottomControl
    private var isFreeStyleCropEnabled = config.isFreeStyleCropEnabled


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)
        isOnlyCamera = intent.getBooleanExtra(ImageExtra.EXTRA_IMAGE_IS_ONLY_CAMERA, config.isOnlyCamera)
        if (isOnlyCamera) {
            startOpenCamera()
        }

        mediaLoading()

        //预览
        layout_bottom_preview.backgroundColor = getResColor(bottomBackgroundColor)
        layout_image_preview.setOnClickListener {
            if (imageSelectedList.size > 0) {
                ImageSelector.preview(this, imageSelectedList)
            } else {
                toast("请选择要预览的图片")
            }
        }
        initTopBar()
    }

    private fun initTopBar() {
        folderWindow = FolderPopWindow(this)
        tv_image_selector_title.compoundDrawablePadding = 10
        tv_image_selector_title.text = "相机胶卷"
        tv_image_selector_title.setCompoundDrawablesWithIntrinsicBounds(null, null, getResDrawable(titleArrowDownImage), null)
        tv_image_selector_title.setOnClickListener {
            tv_image_selector_title.setCompoundDrawablesWithIntrinsicBounds(null, null, getResDrawable(titleArrowUpImage), null)
            folderWindow.showAsDropDown(it)
        }
        folderWindow.setOnDismissListener {
            tv_image_selector_title.setCompoundDrawablesWithIntrinsicBounds(null, null, getResDrawable(titleArrowDownImage), null)
        }
        folderWindow.setItemClickListener(object : FolderPopWindow.OnItemClickListener {
            override fun onItemClicked(bean: LocalMediaFolder) {
                previewList = bean.images.toArrayList()
                val list = arrayListOf<Any>()
                if (bean.name == "相机胶卷" && isAllowTakePhoto) {
                    list.add(Camera())
                }
                list.addAll(bean.images)
                adapter.setDataList(list)
                tv_image_selector_title.text = bean.name
            }
        })

        tv_image_selector_right.visibility = View.VISIBLE
        tv_image_selector_right.setOnClickListener {
            val pictureType = if (imageSelectedList.size > 0)
                imageSelectedList[0].pictureType
            else
                ""
            // 如果设置了图片最小选择数量，则判断是否满足条件
            val isImage = pictureType.startsWith(ImageExtra.IMAGE) || pictureType == ""
            if (imageSelectedList.size == 0) {
                toast("请选择图片")
                return@setOnClickListener
            } else if (selectedMode == ImageExtra.MULTI && imageSelectedList.size < selectedMinNum) {
                val str = if (isImage)
                    "图片最低选择不能少于${selectedMinNum}张"
                else
                    "视频最低选择不能少于${selectedMinNum}个"
                toast(str)
                return@setOnClickListener
            }
            if (isCrop && isImage && selectedMode == ImageExtra.SINGLE) {
                startCrop(imageSelectedList[0].path)
            } else if (isImage && isCompress) {
                // 图片才压缩，视频不管
                ImageCompress.luban(this, ignoreCompressSize, imageSelectedList) {
                    onResult(it.toArrayList())
                }
            } else {
                onResult(imageSelectedList)
            }
        }
        layout_image_selector_title.backgroundColor = getResColor(toolbarColor)
    }


    private fun mediaLoading() {
        PermissionsCheck.with(this)
                .requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .onPermissionsResult { deniedPermissions, allPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        MediaLoading.loadMedia(this, false) { it ->
                            foldersList = it
                            folderWindow.bindFolder(foldersList)
                            val imageDataList = arrayListOf<Any>()
                            if (isAllowTakePhoto) {
                                imageDataList.add(Camera())
                            }
                            imageDataList.addAll(it[0].images)
                            previewList = it[0].images.toArrayList()
                            initAdapter(imageDataList)
                        }
                    } else {
                        toast("内存权限请求失败")
                        closeActivity()
                    }
                }
    }

    private fun initAdapter(imageDataList: ArrayList<Any>) {
        updateSelectedNum(imageSelectedList.size, "预览")
        picture_recycler.setHasFixedSize(true)
        picture_recycler.addItemDecoration(GridSpacingItemDecoration(4,
                (2 * resources.displayMetrics.density).toInt(), false))
        picture_recycler.layoutManager = GridLayoutManager(this, 4)
        adapter = SlimAdapter.creator()
                .register<LocalMedia>(R.layout.layout_image_selector_grid) { injector, bean, position ->
                    injector
                            .with<ImageView>(R.id.iv_image_selector_picture) {
                                imageNoCache(bean.path, it)
                            }
                            .with<ImageView>(R.id.iv_image_selector_state) {
                                val selectedList = imageSelectedList.filter { image -> image.path == bean.path }
                                if (selectedList.isNotEmpty()) {
                                    bean.isChecked = true
                                    it.isSelected = true
                                }
                                it.isSelected = bean.isChecked
                                val drawable = selected(selectedStateImage, unSelectedStateImage)
                                it.setImageDrawable(drawable)
                                if (selectedMode == ImageExtra.SINGLE && bean.isChecked) {
                                    lastTimeSelectedInjector = injector
                                    lastTimeSelectedLocalMedia = bean
                                }
                            }
                            .clicked(R.id.layout_image_selector_state) {
                                val pictureType = if (imageSelectedList.size > 0) imageSelectedList.get(0).pictureType else ""
                                val stateImageView = injector.getView<ImageView>(R.id.iv_image_selector_state)
                                val imageView = injector.getView<ImageView>(R.id.iv_image_selector_picture)
                                val isChecked = stateImageView.isSelected
                                if (!TextUtils.isEmpty(pictureType)) {
                                    val toEqual = MediaMimeType.mimeToEqual(pictureType, bean.pictureType)
                                    if (!toEqual) {
                                        toast("不能同时选择图片或视频")
                                        return@clicked
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
                                    return@clicked
                                }

                                stateImageView.isSelected = !isChecked
                                bean.isChecked = !isChecked

                                if (stateImageView.isSelected) {
                                    // 如果是单选，则清空已选中的并刷新列表(作单一选择)
                                    if (selectedMode == ImageExtra.SINGLE) {
                                        singleMediaImage()
                                        lastTimeSelectedInjector = injector
                                        lastTimeSelectedLocalMedia = bean
                                    }
                                    stateImageView.startAnimation(selectedAnim)
                                    imageSelectedList.add(bean)
                                    bean.num = imageSelectedList.size
                                    if (isSelectedImageAnim) {
                                        scaleAnim(imageView, originalSize, zoomSize)
                                        imageView.setColorFilter(ContextCompat.getColor(this@ImageSelectorActivity, R.color.image_overlay_true), PorterDuff.Mode.SRC_ATOP)
                                    }
                                } else {
                                    val selectedList = imageSelectedList.filter { image -> image.path == bean.path }
                                    imageSelectedList.removeAll(selectedList)
                                    if (isSelectedImageAnim) {
                                        scaleAnim(imageView, zoomSize, originalSize)
                                        imageView.setColorFilter(ContextCompat.getColor(this@ImageSelectorActivity, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
                                    }
                                }
                                updateSelectedNum(imageSelectedList.size, "预览")
                            }
                            .clicked {
                                var index = position
                                if (isAllowTakePhoto && adapter.getDataList<Camera>().get(0) is Camera) {
                                    index = position - 1
                                }
                                ActivityResult.with(this@ImageSelectorActivity)
                                        .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_LIST, previewList.toArrayList())
                                        .putParcelableArrayList(ImageExtra.EXTRA_IMAGE_SELECTED_LIST, imageSelectedList)
                                        .putBoolean(ImageExtra.EXTRA_IMAGE_IS_SELECTED, true)
                                        .putInt(ImageExtra.EXTRA_IMAGE_INDEX, index)
                                        .targetActivity(ImagePreviewActivity::class.java)
                                        .onResult { intent ->
                                            imageSelectedList = intent?.getParcelableArrayListExtra<LocalMedia>(ImageExtra.EXTRA_IMAGE_SELECTED_LIST).orEmpty().toArrayList()
                                            imageSelectedList.forEach { selectImage ->
                                                previewList.forEach { image ->
                                                    if (selectImage.path == image.path) {
                                                        image.isChecked = true
                                                    }
                                                }
                                            }
                                            val list = arrayListOf<Any>()
                                            if (tv_image_selector_title.text.toString() == "相机胶卷" && isAllowTakePhoto) {
                                                list.add(Camera())
                                            }
                                            list.addAll(previewList)
                                            adapter.setDataList(list)
                                            updateSelectedNum(imageSelectedList.size, "预览")
                                        }
                                overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
                            }
                }
                .register<Camera>(R.layout.layout_image_selector_grid_camera) { injector, bean, position ->
                    injector.clicked {
                        if (imageSelectedList.size >= selectedMaxNum) {
                            toast("你最多可以选择${selectedMaxNum}张图片")
                            return@clicked
                        }
                        startOpenCamera()
                    }
                }
                .attachTo(picture_recycler)
                .setDataList(imageDataList)
    }

    private fun startOpenCamera() {
        PermissionsCheck.with(this)
                .requestPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .onPermissionsResult { deniedPermissions, allPermissions ->
                    if (deniedPermissions.isEmpty()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (cameraIntent.resolveActivity(packageManager) != null) {
                            val cameraFile = PictureFileUtils.createCameraFile(this, ImageExtra.TYPE_IMAGE, outputCameraPath, PictureFileUtils.POSTFIX)
                            cameraPath = cameraFile.absolutePath
                            val imageUri = parUri(cameraFile)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            startActivityForResult(cameraIntent, ImageExtra.EXTRA_CAMERA)
                        }
                    } else {
                        deniedPermissions.forEach {
                            if (it == Manifest.permission.CAMERA) {
                                toast("相机权限请求失败")
                                closeActivity()
                            } else if (it == Manifest.permission.WRITE_EXTERNAL_STORAGE || it == Manifest.permission.READ_EXTERNAL_STORAGE) {
                                toast("内存权限请求失败")
                                closeActivity()
                            }
                        }
                    }
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
        val authority = "$packageName.provider"
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            imageUri = FileProvider.getUriForFile(this, authority, cameraFile)
        } else {
            imageUri = Uri.fromFile(cameraFile)
        }
        return imageUri
    }


    /**
     * 单选模式
     */
    private fun singleMediaImage() {
        if (imageSelectedList.size > 0) {
            imageSelectedList.clear()
            val lastTimeSelectedImageView = lastTimeSelectedInjector?.getView<ImageView>(R.id.iv_image_selector_picture)
            val lastTimeSelectedStateImageView = lastTimeSelectedInjector?.getView<ImageView>(R.id.iv_image_selector_state)
            if (isSelectedImageAnim && lastTimeSelectedImageView != null) {
                scaleAnim(lastTimeSelectedImageView, zoomSize, originalSize)
                lastTimeSelectedImageView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
            }
            lastTimeSelectedStateImageView?.isSelected = false
            lastTimeSelectedLocalMedia?.isChecked = false
        }
    }


    /**
     * 用于ImageView选中、取消图片放大、缩小
     */
    private fun scaleAnim(imageView: ImageView, startSize: Float, endSize: Float) {
        val set = AnimatorSet()
        set.playTogether(
                ObjectAnimator.ofFloat(imageView, "scaleX", startSize, endSize),
                ObjectAnimator.ofFloat(imageView, "scaleY", startSize, endSize)
        )
        set.duration = DURATION.toLong()
        set.start()
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
            if (cameraFolder != null) {
                // 相机胶卷
                cameraFolder.firstImagePath = media.path
                cameraFolder.images = foldersList[0].images
                cameraFolder.imageNum = cameraFolder.imageNum + 1
                // 拍照相册
                val num = folder.imageNum + 1
                folder.imageNum = num
                folder.images.add(0, media)
                folder.firstImagePath = cameraPath
                folderWindow.bindFolder(foldersList)
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
            newFolder.name = "相机胶卷"
            newFolder.path = ""
            newFolder.firstImagePath = ""
            folders.add(newFolder)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImageExtra.EXTRA_CAMERA == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    val file = File(cameraPath)
                    //启动MediaScanner服务，扫描媒体文件
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                //单选
                if (selectedMode == ImageExtra.SINGLE) {
                    singleMediaImage()
                }

                // 生成新拍照片或视频对象
                val media = LocalMedia()
                val pictureType = MediaMimeType.createImageType(cameraPath)
                media.path = cameraPath
                media.pictureType = pictureType
                media.duration = 0
                media.isChecked = true
                media.mimeType = ImageExtra.TYPE_IMAGE
                imageSelectedList.add(media)
                previewList.add(0, media)
                adapter.addData(1, media)

                // 解决部分手机拍照完Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,不及时刷新问题手动添加
                manualSaveFolder(media)
                updateSelectedNum(imageSelectedList.size, "预览")
                if (isOnlyCamera) {
                    ActivityResult.with(this)
                            .putParcelableArrayList(ImageExtra.EXTRA_SELECTED_RESULT, arrayListOf(media))
                            .finish()
                    overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
                }
            }
        }

    }

    private fun startCrop(url: String) {
        if (uCropOptions == null) {
            uCropOptions = UCrop.Options()
            uCropOptions.setStatusBarColor(getResColor(statusColor))
            uCropOptions.setToolbarColor(getResColor(toolbarColor))
            uCropOptions.setToolbarWidgetColor(getResColor(titleTextColor))
            uCropOptions.setHideBottomControls(isHideUCropBottomControl) // 是否隐藏底部容器
            uCropOptions.setFreeStyleCropEnabled(isFreeStyleCropEnabled) //是否能调整裁剪框
            uCropOptions.setCompressionQuality(uCropCompressQuality) ////设置裁剪的图片质量，取值0-100
        }
        val imageType = MediaMimeType.getLastImgType(url)
        val target = File(PictureFileUtils.getDiskCacheDir(this), System.currentTimeMillis().toString() + imageType)
        val intent = UCrop.of(Uri.fromFile(File(url)), Uri.fromFile(target))
                .withAspectRatio(uCropScaleX, uCropScaleY)
                .withMaxResultSize(uCropWidth, uCropHeight)
                .withOptions(uCropOptions)
                .getIntent(this)
        ActivityResult.with(this)
                .targentIntent(intent)
                .onResult { intent ->
                    intent?.let {
                        val uri = UCrop.getOutput(it)
                        val cutPath = uri?.path
                        imageSelectedList[0].cutPath = cutPath
                        imageSelectedList[0].isCut = true
                        onResult(imageSelectedList)
                    }
                }
    }


    /**
     * return image result
     *
     * @param images
     */
    private fun onResult(images: ArrayList<LocalMedia>) {
        ActivityResult.with(this)
                .putParcelableArrayList(ImageExtra.EXTRA_SELECTED_RESULT, images.toArrayList())
                .finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private class Camera
}