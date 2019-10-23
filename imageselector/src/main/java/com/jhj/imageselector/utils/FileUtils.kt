package com.jhj.imageselector.utils

import android.os.Environment
import java.io.File

object FileUtils {
    val PROJECT_DIR = "iamgeSelector"
    val IMAGE = "image"

    fun getSDPath(subDir: String?): String? {

        if (Environment.MEDIA_MOUNTED == Environment
                        .getExternalStorageState()) {

            var path = Environment.getExternalStorageDirectory()
                    .absolutePath

            if (!path.endsWith("/"))
                path += "/"

            path += PROJECT_DIR + "/"

            if (subDir != null && subDir.trim { it <= ' ' }.length > 0)
                path += "$subDir/"

            val f = File(path)

            return if (!f.exists()) {
                if (f.mkdirs())
                    path
                else
                    null
            } else {
                if (f.isFile) {
                    if (f.delete()) {
                        if (f.mkdir())
                            path
                        else
                            null
                    } else
                        null
                } else
                    path
            }
        }
        return null
    }

}