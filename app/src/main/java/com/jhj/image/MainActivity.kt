package com.jhj.image

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.jhj.imageselector.ImageConfig
import com.jhj.imageselector.ImageModel

import com.jhj.imageselector.ImageViewPagerActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = arrayListOf<ImageModel>(
                ImageModel("http://47.94.173.253:8008/image/com/46/1804085749logo.png"),
                ImageModel("http://47.94.173.253:8008/image/com/59/1804113524logo.png"),
                ImageModel("http://47.94.173.253:8008/image/com/60/1808172559logo.png")
        )

        textView.setOnClickListener {
            val intent = Intent(this, ImageViewPagerActivity::class.java)
            intent.putExtra(ImageConfig.IMAGE_IS_EDITABLE, true)
            intent.putExtra(ImageConfig.IMAGE_LIST, list)
            startActivity(intent)
        }
    }
}
