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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jhj.imageselector.*
import com.jhj.imageselector.activityresult.ActivityResult
import com.jhj.imageselector.permissions.PermissionsCheck
import com.jhj.imageselector.weight.FolderPopWindow
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.holder.ViewInjector
import kotlinx.android.synthetic.main.activity_image_selector.*
import kotlinx.android.synthetic.main.layout_image_selector_topbar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import java.io.File

/**
 * 图片选择
 *
 * Created by jhj on 19-1-16.
 */
open class ImageSelectorActivity : AppCompatActivity() {

    private var config = ImageConfig.getInstance()
    private lateinit var adapter: SlimAdapter
    private lateinit var previewList: List<LocalMedia>
    private lateinit var selectedAnim: Animation

    //上一次选中图片的信息
    private lateinit var lastTimeSelectedInjector: ViewInjector
    private lateinit var lastTimeSelectedLocalMedia: LocalMedia

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
    private var selectedMode: Int = config.selectMode //图片是单选、多选
    private var selectedMaxNum: Int = config.maxSelectNum //最大选择数量
    private var selectedMinNum: Int = config.minSelectNum //最小选择数量
    private var selectImages = ArrayList<LocalMedia>() //被选中的图片

    //文件夹选择PopWindow
    private lateinit var folderWindow: FolderPopWindow
    private var foldersList: List<LocalMediaFolder> = ArrayList()

    //拍照图片路径
    private var cameraPath: String? = null
    private var outputCameraPath: String? = null

    //基础设置
    private var previewTextColor = config.previewTextColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selector)
        folderWindow = FolderPopWindow(this)
        selectedAnim = AnimationUtils.loadAnimation(this, R.anim.selected_image_num_in)
        if (isOnlyCamera) {
             startOpenCamera()
             return
        } else {
            PermissionsCheck.with(this)
                    .requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .onPermissionsResult { deniedPermissions, allPermissions ->
                        if (deniedPermissions.isEmpty()) {
                            initAdapter()
                        } else {
                            toast("内存权限请求失败")
                            closeActivity()
                        }
                    }
        }
        initTopBar()
    }


    private fun initTopBar() {
        tv_image_selector_right.setOnClickListener {

            val pictureType = if (selectImages.size > 0)
                selectImages[0].pictureType
            else
                ""
            // 如果设置了图片最小选择数量，则判断是否满足条件
            val isImage = pictureType.startsWith(ImageExtra.IMAGE) || pictureType == ""
            if (selectedMinNum > 0 && selectedMode == ImageExtra.MULTI) {
                if (selectImages.size < selectedMinNum) {
                    val str = if (isImage)
                        "图片最低选择不能少于${selectedMinNum}张"
                    else
                        "视频最低选择不能少于${selectedMinNum}个"
                    toast(str)
                    return@setOnClickListener
                }
            }
            /* if (config.isCrop && isImage) {
                 if (config.selectMode == ImageConfig.SelectedMode.MULTI) {
                     image?.path?.let { it1 -> ImageCrop.startCrop(it1) }
                 } else {
                     // 是图片和选择压缩并且是多张，调用批量压缩
                     val medias = ArrayList<String>()
                     for (media in selectImages) {
                         medias.add(media.getPath())
                     }
                     ImageCrop.startCrop(medias)
                 }
             } else*/
            if (isImage && isCompress) {
                // 图片才压缩，视频不管
                ImageCompress.luban(this, ignoreCompressSize, selectImages) {
                    onResult(it.toArrayList())
                }
            } else {
                onResult(selectImages)
            }
        }


        folderArrow(R.drawable.arrow_down)
        tv_image_selector_title.setOnClickListener {
            folderArrow(R.drawable.arrow_up)
            folderWindow.showAsDropDown(it)
        }
        folderWindow.setOnDismissListener {
            folderArrow(R.drawable.arrow_down)
        }
        folderWindow.setItemClickListener(object : FolderPopWindow.OnItemClickListener {
            override fun onItemClicked(bean: LocalMediaFolder) {
                previewList = bean.images
                val list = arrayListOf<Any>()
                if (bean.name == "相机胶卷" && isAllowTakePhoto) {
                    list.add(Camera())
                }
                list.addAll(bean.images)
                adapter.dataList = list
                tv_image_selector_title.text = bean.name

            }
        })


    }


    private fun initAdapter() {
        picture_recycler.setHasFixedSize(true)
        picture_recycler.addItemDecoration(GridSpacingItemDecoration(4,
                (2 * resources.displayMetrics.density).toInt(), false))
        picture_recycler.layoutManager = GridLayoutManager(this, 4)

        MediaLoading.loadMedia(this, false) {
            foldersList = it
            folderWindow.bindFolder(foldersList)
            val list = arrayListOf<Any>()
            if (isAllowTakePhoto) {
                list.add(Camera())
            }
            list.addAll(it[0].images)
            previewList = it[0].images
            adapter = SlimAdapter.creator(GridLayoutManager(this, 4))
                    .register<LocalMedia>(R.layout.layout_grid_image) { viewInjector, localMedia, i ->
                        viewInjector
                                .with<ImageView>(R.id.iv_image_selector_picture) {
                                    Glide.with(this)
                                            .load(localMedia.path)
                                            .into(it)

                                }
                                .with<ImageView>(R.id.iv_image_selector_state) {
                                    it.isSelected = localMedia.isChecked
                                    if (selectedMode == ImageExtra.SINGLE && localMedia.isChecked) {
                                        lastTimeSelectedInjector = viewInjector
                                        lastTimeSelectedLocalMedia = localMedia
                                    }
                                }
                                .clicked(R.id.layout_image_selector_state) {
                                    val pictureType = if (selectImages.size > 0) selectImages.get(0).pictureType else ""
                                    val stateImageView = viewInjector.getView<ImageView>(R.id.iv_image_selector_state)
                                    val imageView = viewInjector.getView<ImageView>(R.id.iv_image_selector_picture)
                                    val isChecked = stateImageView.isSelected
                                    if (!TextUtils.isEmpty(pictureType)) {
                                        val toEqual = MediaMimeType.mimeToEqual(pictureType, localMedia.pictureType)
                                        if (!toEqual) {
                                            toast("不能同时选择图片或视频")
                                            return@clicked
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
                                        return@clicked
                                    }

                                    stateImageView.isSelected = !isChecked
                                    localMedia.isChecked = !isChecked

                                    if (stateImageView.isSelected) {
                                        // 如果是单选，则清空已选中的并刷新列表(作单一选择)
                                        if (selectedMode == ImageExtra.SINGLE) {
                                            singleMediaImage()
                                            lastTimeSelectedInjector = viewInjector
                                            lastTimeSelectedLocalMedia = localMedia
                                        }
                                        stateImageView.startAnimation(selectedAnim)
                                        selectImages.add(localMedia)
                                        localMedia.num = selectImages.size
                                        if (isSelectedImageAnim) {
                                            scaleAnim(imageView, originalSize, zoomSize)
                                            imageView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_true), PorterDuff.Mode.SRC_ATOP)
                                        }
                                    } else {
                                        selectImages.remove(localMedia)
                                        if (isSelectedImageAnim) {
                                            scaleAnim(imageView, zoomSize, originalSize)
                                            imageView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
                                        }
                                    }

                                    //预览
                                    id_ll_ok.setOnClickListener {
                                        if (selectImages.size > 0) {
                                            startActivity<ImagePreviewActivity>(ImageExtra.IMAGE_LIST to selectImages)
                                        } else {
                                            toast("请选择要预览的图片")
                                        }

                                    }
                                    if (selectImages.size > 0) {
                                        tv_img_num.visibility = View.VISIBLE
                                        tv_img_num.text = selectImages.size.toString()
                                        tv_img_num.startAnimation(selectedAnim)
                                        tv_ok.text = "预览"
                                        tv_ok.textColor = ContextCompat.getColor(this, previewTextColor)
                                    } else {
                                        tv_img_num.text = "0"
                                        tv_img_num.visibility = View.GONE
                                        tv_ok.text = "请选择"
                                        tv_ok.textColor = 0xff999999.toInt()
                                    }
                                }
                                .clicked {
                                    var index = i
                                    if (isAllowTakePhoto && adapter.dataList[0] is Camera) {
                                        index = i - 1
                                    }
                                    ActivityResult.with(this)
                                            .putSerializable(ImageExtra.IMAGE_LIST, previewList.toArrayList())
                                            .putInt(ImageExtra.IMAGE_INDEX, index)
                                            .targetActivity(ImagePreviewActivity::class.java)
                                            .onResult {

                                            }
                                }
                    }
                    .register<Camera>(R.layout.layout_item_camera) { viewInjector, localMedia, i ->
                        viewInjector.clicked {
                            if (selectImages.size >= selectedMaxNum) {
                                toast("你最多可以选择${selectedMaxNum}张图片")
                                return@clicked
                            }
                            startOpenCamera()
                        }
                    }
                    .attachTo(picture_recycler)
                    .setDataList(list)
        }
    }

    //popWindow
    private fun folderArrow(drawableRes: Int) {
        val drawable = ContextCompat.getDrawable(this, drawableRes)
        tv_image_selector_title.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        tv_image_selector_title.compoundDrawablePadding = 10
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
                            startActivityForResult(cameraIntent, ImageExtra.REQUEST_CAMERA)
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
     * 单选模式
     */
    private fun singleMediaImage() {
        if (selectImages.size > 0) {
            selectImages.clear()
            val lastTimeSelectedImageView = lastTimeSelectedInjector.getView<ImageView>(R.id.iv_image_selector_picture)
            val lastTimeSelectedStateImageView = lastTimeSelectedInjector.getView<ImageView>(R.id.iv_image_selector_state)
            if (isSelectedImageAnim) {
                scaleAnim(lastTimeSelectedImageView, zoomSize, originalSize)
                lastTimeSelectedImageView.setColorFilter(ContextCompat.getColor(this, R.color.image_overlay_false), PorterDuff.Mode.SRC_ATOP)
            }
            lastTimeSelectedStateImageView.isSelected = false
            lastTimeSelectedLocalMedia.isChecked = false
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
        if (resultCode == Activity.RESULT_OK && ImageExtra.REQUEST_CAMERA == requestCode) {
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
            selectImages.add(media)

            adapter.addData(1, media)

            // 解决部分手机拍照完Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,不及时刷新问题手动添加
            manualSaveFolder(media)

        }

    }

    /**
     * return image result
     *
     * @param images
     */
    private fun onResult(images: ArrayList<LocalMedia>) {
        /*dismissCompressDialog()
        if (config.camera && config.selectionMode == PictureConfig.MULTIPLE && selectionMedias != null) {
            images.addAll(if (images.size > 0) images.size - 1 else 0, selectionMedias)
        }*/
        val intent = Intent()
        intent.putExtra(ImageExtra.EXTRA_SELECTED_RESULT, images)
        setResult(Activity.RESULT_OK, intent)
        closeActivity()
    }

    /**
     * Close Activity
     */
    private fun closeActivity() {
        finish()
        if (isOnlyCamera) {
            overridePendingTransition(0, R.anim.fade_out)
        } else {
            overridePendingTransition(0, R.anim.a3)
        }
    }

    private class Camera

}