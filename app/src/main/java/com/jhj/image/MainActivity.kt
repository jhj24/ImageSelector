package com.jhj.image

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jhj.imageselector.ImageSelector
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.imageselector.config.ImageExtra
import com.jhj.imageselector.weight.GridSpacingItemDecoration
import com.jhj.slimadapter.SlimAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var dataList: List<LocalMedia>

    private lateinit var adapter: SlimAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataList = arrayListOf()

        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(4, (2 * resources.displayMetrics.density).toInt(), false))
        adapter = SlimAdapter.creator()
                .register<LocalMedia>(R.layout.layout_image_selector_grid) { injector, bean, position ->
                    val path = when {
                        bean.isCut -> bean.cutPath
                        bean.isCompressed -> bean.compressPath
                        else -> bean.path
                    }
                    injector
                            .with<ImageView>(R.id.iv_image_selector_picture) {
                                Glide.with(this@MainActivity)
                                        .load(path)
                                        .into(it)
                            }
                            .clicked {
                                ImageSelector.deletePreview(this@MainActivity, dataList, position) {
                                    adapter.setDataList(it)
                                }
                            }
                            .gone(R.id.layout_image_selector_state)
                }
                .attachTo(recyclerView)

        btn_selector.setOnClickListener {
            ImageSelector.selected(this@MainActivity, ImageExtra.MULTI, dataList, isImageEditable = true) {
                this.dataList = it
                adapter.setDataList(it)
            }

        }
    }


}
