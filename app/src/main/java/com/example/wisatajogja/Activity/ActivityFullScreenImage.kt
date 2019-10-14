package com.example.wisatajogja.Activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.wisatajogja.Adapter.AdapterFullScreenImage
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.ArrayList

class ActivityFullScreenImage : AppCompatActivity() {

    val EXTRA_POS = "key.EXTRA_POS"
    val EXTRA_IMGS = "key.EXTRA_IMGS"

    private val imgloader = ImageLoader.getInstance()
    private var adapter: AdapterFullScreenImage? = null
    private var viewPager: ViewPager? = null
    private var text_page: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        if (!imgloader.isInited()) Tools.initImageLoader(this)
        viewPager = findViewById(R.id.pager) as ViewPager
        text_page = findViewById(R.id.text_page) as TextView

        var items: ArrayList<String>? = ArrayList()
        val i = intent
        val position = i.getIntExtra(EXTRA_POS, 0)
        items = i.getStringArrayListExtra(EXTRA_IMGS)
        adapter = AdapterFullScreenImage(this@ActivityFullScreenImage, items)
        val total = adapter!!.getCount()
        viewPager!!.setAdapter(adapter)

        text_page!!.setText(String.format(getString(R.string.image_of), position + 1, total))

        // displaying selected image first
        viewPager!!.setCurrentItem(position)
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                //
            }

            override fun onPageSelected(position: Int) {
                text_page!!.setText(String.format(getString(R.string.image_of), position + 1, total))
            }

            override fun onPageScrollStateChanged(state: Int) {
                //
            }
        })

        (findViewById(R.id.btnClose) as ImageButton).setOnClickListener { finish() }

        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    override fun onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(this)
        super.onResume()
    }

}
