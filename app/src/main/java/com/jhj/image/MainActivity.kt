package com.jhj.image

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.jhj.imageselector.weight.GridSpacingItemDecoration
import com.jhj.imageselector.ImageSelector
import com.jhj.imageselector.bean.LocalMedia
import com.jhj.slimadapter.SlimAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var dataList: List<LocalMedia>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val adapter = SlimAdapter.creator(GridLayoutManager(this, 4))
                .register<LocalMedia>(R.layout.layout_image_selector_grid) { injector, bean, position ->
                    injector
                            .with<ImageView>(R.id.iv_image_selector_picture) {
                                Glide.with(this)
                                        .asBitmap()
                                        .load(bean.path)
                                        .into(it)
                            }
                            .clicked {
                                ImageSelector.with(this)
                                        .imagePreview(dataList, position)
                            }
                            .gone(R.id.layout_image_selector_state)
                }
                .attachTo(recyclerView)
                .addItemDecoration(GridSpacingItemDecoration(4,
                        (2 * resources.displayMetrics.density).toInt(), false))

        btn_selector.setOnClickListener {
            ImageSelector.with(this)
                    .imageSelected {
                        this.dataList = it
                        adapter.setDataList(it)
                    }
        }
    }


}
