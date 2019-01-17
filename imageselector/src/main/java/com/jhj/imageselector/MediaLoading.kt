package com.jhj.imageselector

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import java.io.File
import java.util.*

/**
 * 图片加载处理类
 *
 * Created by jhj on 19-1-17.
 */
object MediaLoading {
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"
    private val NOT_GIF = "!='image/gif'"
    private val DURATION = "duration"
    private val type = PictureConfig.TYPE_IMAGE

    // 媒体文件数据库字段
    private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION)

    // 图片
    private val SELECTION = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0")

    private val SELECTION_NOT_GIF = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">0"
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)


    fun loadMedia(activity: FragmentActivity, isGif: Boolean, body: (List<LocalMediaFolder>) -> Unit) {
        activity.supportLoaderManager.initLoader(type, null, object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                val MEDIA_TYPE_IMAGE = arrayOf<String>(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString());
                return CursorLoader(
                        activity, QUERY_URI,
                        PROJECTION, if (isGif) SELECTION else SELECTION_NOT_GIF, MEDIA_TYPE_IMAGE, ORDER_BY)
            }

            override fun onLoaderReset(loader: Loader<Cursor>) {

            }

            override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                try {
                    val imageFolders = ArrayList<LocalMediaFolder>()
                    val allImageFolder = LocalMediaFolder()
                    val latelyImages = ArrayList<LocalMedia>()
                    data?.let {
                        val count = it.count
                        if (count > 0) {
                            it.moveToFirst()
                            do {
                                val path = it.getString(it.getColumnIndexOrThrow(PROJECTION[1]))
                                val pictureType = it.getString(it.getColumnIndexOrThrow(PROJECTION[2]))
                                val width = it.getInt(it.getColumnIndexOrThrow(PROJECTION[3]))
                                val height = it.getInt(it.getColumnIndexOrThrow(PROJECTION[4]))
                                val duration = it.getInt(it.getColumnIndexOrThrow(PROJECTION[5]))

                                val image = LocalMedia(path, duration.toLong(), type, pictureType, width, height)
                                val folder = getImageFolder(path, imageFolders)
                                folder.images.add(image)
                                folder.imageNum = folder.imageNum + 1

                                latelyImages.add(image)
                                allImageFolder.imageNum = allImageFolder.imageNum + 1
                            } while (it.moveToNext())

                            if (latelyImages.size > 0) {
                                sortFolder(imageFolders)
                                imageFolders.add(0, allImageFolder)
                                allImageFolder.firstImagePath = latelyImages[0].path
                                val title = if (type == MediaMimeType.ofAudio())
                                    "所有音频"
                                else
                                    "相机胶卷"
                                allImageFolder.name = title
                                allImageFolder.images = latelyImages
                            }
                        }
                        body(imageFolders)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    /**
     * 文件夹数量进行排序
     *
     * @param imageFolders
     */
    private fun sortFolder(imageFolders: List<LocalMediaFolder>) {
        // 文件夹按图片数量排序
        Collections.sort(imageFolders, Comparator { lhs, rhs ->
            if (lhs.images == null || rhs.images == null) {
                return@Comparator 0
            }
            val lSize = lhs.imageNum
            val rSize = rhs.imageNum
            if (lSize == rSize) 0 else if (lSize < rSize) 1 else -1
        })
    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private fun getImageFolder(path: String, imageFolders: MutableList<LocalMediaFolder>): LocalMediaFolder {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile
        for (folder in imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
            if (folder.name == folderFile.name) {
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


}