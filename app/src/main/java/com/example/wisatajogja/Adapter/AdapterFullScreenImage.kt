package com.example.wisatajogja.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.wisatajogja.R
import com.example.wisatajogja.Widget.TouchImageView
import com.nostra13.universalimageloader.core.ImageLoader


class AdapterFullScreenImage : PagerAdapter() {

    private var act: Activity? = null
    private var imagePaths: List<String>? = null
    private var inflater: LayoutInflater? = null
    private val imgloader = ImageLoader.getInstance()

    // constructor
    fun AdapterFullScreenImage(activity: Activity, imagePaths: List<String>) {
        this.act = activity
        this.imagePaths = imagePaths
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    override fun getCount(): Int {
        return this.imagePaths!!.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imgDisplay: TouchImageView
        inflater = act!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewLayout = inflater!!.inflate(R.layout.item_fullscreen_image, container, false)

        imgDisplay = viewLayout.findViewById(R.id.imgDisplay) as TouchImageView

        imgloader.displayImage(imagePaths!!.get(position), imgDisplay)
        (container as ViewPager).addView(viewLayout)

        return viewLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as RelativeLayout)

    }

}
