package com.example.wisatajogja.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.balysv.materialripple.MaterialRippleLayout
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Data.ThisApplication
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.item_place.view.*
import java.util.ArrayList

class ActivityNewsInfoDetails : AppCompatActivity() {

    private val EXTRA_OBJECT = "key.EXTRA_OBJECT"
    private val EXTRA_FROM_NOTIF = "key.EXTRA_FROM_NOTIF"

    // activity transition
    fun navigate(activity: Activity, obj: NewsInfo, from_notif: Boolean?) {
        val int = navigateBase(activity, obj, from_notif)
        activity.startActivity(int)
    }

    fun navigateBase(context: Context, obj: NewsInfo, from_notif: Boolean?): Intent {
        val int = Intent(context, ActivityNewsInfoDetails::class.java)
        int.putExtra(EXTRA_OBJECT, obj)
        int.putExtra(EXTRA_FROM_NOTIF, from_notif)
        return int
    }

    private var from_notif: Boolean? = null

    // extra obj
    private var newsInfo: NewsInfo? = null

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var parent_view: View? = null
    private var webview: WebView? = null
    private val imgloader = ImageLoader.getInstance()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_news_info_details)

        newsInfo = intent.getSerializableExtra(EXTRA_OBJECT) as NewsInfo
        from_notif = intent.getBooleanExtra(EXTRA_FROM_NOTIF, false)

        if (!imgloader.isInited) Tools.initImageLoader(this)

        initComponent()
        initToolbar()
        displayData()

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View News Info : " + newsInfo.title)
    }

    private fun initComponent() {
        parent_view = findViewById(android.R.id.content)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.setTitle("")
    }

//    private fun prepareAds() {
//        if (AppConfig.ADS_NEWS_DETAILS_BANNER && Tools.cekConnection(applicationContext)) {
//            val mAdView = findViewById(R.id.ad_view) as AdView
//            val adRequest = AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
//            // Start loading the ad in the background.
//            mAdView.loadAd(adRequest)
//        } else {
//            (findViewById(R.id.banner_layout) as RelativeLayout).visibility = View.GONE
//        }
//    }

    private fun displayData() {
        (findViewById(R.id.title) as TextView).text = Html.fromHtml(newsInfo.title)

        webview = findViewById(R.id.content)
//                as WebView
        var html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> "
        html_data += newsInfo.full_content
        webview!!.getSettings().javaScriptEnabled = true
        webview!!.getSettings()
        webview!!.getSettings().builtInZoomControls = true
        webview!!.setBackgroundColor(Color.TRANSPARENT)
        webview!!.setWebChromeClient(WebChromeClient())
        webview!!.loadData(html_data, "text/html; charset=UTF-8", null)

        // disable scroll on touch
        webview!!.setOnTouchListener(View.OnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE })

        (findViewById(R.id.date) as TextView).setText(Tools.getFormattedDate(newsInfo.last_update))
        imgloader.displayImage(
            Constant.getURLimgNews(newsInfo.image),
            findViewById(R.id.image)
//                    as ImageView
        )

        (findViewById(R.id.lyt_image) as MaterialRippleLayout).setOnClickListener {
            val images_list = ArrayList<String>()
            images_list.add(Constant.getURLimgNews(newsInfo.image))
            val i = Intent(this@ActivityNewsInfoDetails, ActivityFullScreenImage::class.java)
            i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, images_list)
            startActivity(i)
        }
    }

    override fun onPause() {
        super.onPause()
        if (webview != null) webview!!.onPause()
    }

    override fun onResume() {
        if (!imgloader.isInited) Tools.initImageLoader(applicationContext)
        if (webview != null) webview!!.onResume()
//        prepareAds()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_details, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackAction()
            return true
        } else if (id == R.id.action_share) {
            Tools.methodShareNews(this, newsInfo)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackAction()
    }

    private fun onBackAction() {
        if (from_notif!!) {
            if (MainActivity.active) {
                finish()
            } else {
                val intent = Intent(applicationContext, ActivitySplashScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }
}
