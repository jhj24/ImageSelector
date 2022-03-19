package com.jhj.imageselector.utils

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.bean.LocalMediaFolder
import com.jhj.imageselector.config.ImageExtra
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
    private val type = ImageExtra.TYPE_IMAGE

    // 媒体文件数据库字段
    private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            DURATION,
            MediaStore.MediaColumns.DATE_MODIFIED)

    // 图片
    private val SELECTION = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">10*1024")

    private val SELECTION_NOT_GIF = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
            + " AND " + MediaStore.MediaColumns.SIZE + ">10*1024" //去除小图片，防止缓存太多
            + " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF
            + " AND " + MediaStore.MediaColumns.DATA + " NOT LIKE '%/cache/%'" //其他缓存
            + " AND " + MediaStore.MediaColumns.DATA + " NOT LIKE '%/zip_cache/%'" //qq空间缓存
            + " AND " + MediaStore.MediaColumns.DATA + " NOT LIKE '%/score_task/images/%'" //今日头条缓存
            + " AND " + MediaStore.MediaColumns.DATA + " NOT LIKE '%/ArModelFile/%'" //京东缓存
            )//去除缓存目录下的图片


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
                                val time = it.getLong(it.getColumnIndexOrThrow(PROJECTION[6]))
                                val mark = path.hashCode()

                                val image = LocalMedia(path, duration.toLong(), type, pictureType, width, height, mark)
                                imageFolder(image, imageFolders)
                                latelyImages.add(image)
                                allImageFolder.imageNum = allImageFolder.imageNum + 1
                            } while (it.moveToNext())

                            if (latelyImages.size > 0) {
                                sortFolder(imageFolders)
                                allImageFolder.firstImagePath = latelyImages[0].path
                                val title = if (type == MediaMimeType.ofAudio())
                                    "所有音频"
                                else
                                    "相机胶卷"
                                allImageFolder.setName(title)
                                allImageFolder.setImages(latelyImages)
                                imageFolders.add(0, allImageFolder)
                            }
                        }
                        body(imageFolders)
                        activity.supportLoaderManager.destroyLoader(type)
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
            val lsize = lhs.imageNum
            val rsize = rhs.imageNum
            if (lsize == rsize) 0 else if (lsize < rsize) 1 else -1
        })
    }

    /**
     * 创建相应文件夹
     *
     * @param path
     * @param imageFolders
     * @return
     */
    private fun imageFolder(media: LocalMedia, imageFolders: MutableList<LocalMediaFolder>) {
        val imageFile = File(media.path)
        val folderFile = imageFile.parentFile
        for (folder in imageFolders) {
            // 同一个文件夹下，返回自己，否则创建新文件夹
            if (folder.name == folderFile.name) {
                folder.images.add(media)
                folder.imageNum = folder.imageNum + 1
                return
            }
        }
        val newFolder = LocalMediaFolder()
        newFolder.name = folderFile.name
        newFolder.path = folderFile.absolutePath
        newFolder.firstImagePath = media.path
        newFolder.images.add(media)
        newFolder.imageNum = newFolder.imageNum + 1
        imageFolders.add(newFolder)
    }
}