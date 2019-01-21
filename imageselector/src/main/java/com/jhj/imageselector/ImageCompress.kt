package com.jhj.imageselector

import android.content.Context
import com.jhj.imageselector.compress.Luban
import com.jhj.imageselector.compress.OnCompressListener

/**
 * Created by jhj on 19-1-21.
 */
object ImageCompress {

    fun luban(context: Context, path: List<LocalMedia>, onResult: (List<LocalMedia>) -> Unit) {
        Luban.with(context)
                .loadLocalMedia(path)
                .ignoreBy(100)
                .setTargetDir("")
                .setCompressListener(
                        object : OnCompressListener {
                            override fun onStart() {
                            }

                            override fun onSuccess(list: List<LocalMedia>?) {
                                onResult(list.orEmpty())
                            }

                            override fun onError(e: Throwable?) {
                                onResult(path)
                            }

                        }).launch()
    }
}