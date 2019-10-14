package com.example.wisatajogja.Activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wisatajogja.Adapter.AdapterImageList
import com.example.wisatajogja.Connection.RestAdapter
import com.example.wisatajogja.Connection.callbacks.CallbackPlaceDetails
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.Data.ThisApplication
import com.example.wisatajogja.Model.Images
import com.example.wisatajogja.Model.Place
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.nostra13.universalimageloader.core.ImageLoader
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

class ActivityPlaceDetails : AppCompatActivity() {

    private val EXTRA_OBJ = "key.EXTRA_OBJ"
    private val EXTRA_NOTIF_FLAG = "key.EXTRA_NOTIF_FLAG"

    // give preparation animation activity transition
    fun navigate(activity: AppCompatActivity, sharedView: View, p: Place) {
        val intent = Intent(activity, ActivityPlaceDetails::class.java)
        intent.putExtra(EXTRA_OBJ, p)
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, EXTRA_OBJ)
        ActivityCompat.startActivity(activity, intent, options.toBundle())
    }

    fun navigateBase(context: Context, obj: Place, from_notif: Boolean?): Intent {
        val i = Intent(context, ActivityPlaceDetails::class.java)
        i.putExtra(EXTRA_OBJ, obj)
        i.putExtra(EXTRA_NOTIF_FLAG, from_notif)
        return i
    }

    private var place: Place? = null
    private val imgloader = ImageLoader.getInstance()
    private var fab: FloatingActionButton? = null
    private var description: WebView? = null
    private var parent_view: View? = null
    private var googleMap: GoogleMap? = null
//    private var db: DatabaseHandler? = null

    private var onProcess = false
    private var isFromNotif = false
    private var callback: Call<CallbackPlaceDetails>? = null
    private var lyt_progress: View? = null
    private var lyt_distance: View? = null
    private var distance = -1f
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)
        parent_view = findViewById(android.R.id.content)

        if (!imgloader.isInited) Tools.initImageLoader(this)

//        db = DatabaseHandler(this)
        // animation transition
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), EXTRA_OBJ)

        place = intent.getSerializableExtra(EXTRA_OBJ) as Place
        isFromNotif = intent.getBooleanExtra(EXTRA_NOTIF_FLAG, false)

        fab = findViewById(R.id.fab)
//                as FloatingActionButton
        lyt_progress = findViewById(R.id.lyt_progress)
        lyt_distance = findViewById(R.id.lyt_distance)
        imgloader.displayImage(
            Constant.getURLimgPlace(place!!.image),
            findViewById(R.id.image)
//                    as ImageView
        )
        distance = place!!.distance

        fabToggle()
        setupToolbar(place!!.name)
        initMap()

        // handle when favorite button clicked
        fab!!.setOnClickListener(View.OnClickListener {
            if (db.isFavoritesExist(place!!.place_id)) {
                db.deleteFavorites(place!!.place_id)
                Snackbar.make(
                    parent_view!!,
                    place!!.name + " " + getString(R.string.remove_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
                // analytics tracking
                ThisApplication.getInstance()
                    .trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", place!!.name)
            } else {
                db.addFavorites(place!!.place_id)
                Snackbar.make(
                    parent_view!!,
                    place!!.name + " " + getString(R.string.add_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
                // analytics tracking
                ThisApplication.getInstance()
                    .trackEvent(Constant.Event.FAVORITES.name(), "ADD", place!!.name)
            }
            fabToggle()
        })

        // for system bar in lollipop
        Tools.systemBarLolipop(this)

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View place : " + place!!.name)
    }

    private fun displayData(p: Place) {
        (findViewById(R.id.address) as TextView).text = p.address
        (findViewById(R.id.phone) as TextView).text =
            if (p.phone.equals("-") || p.phone!!.trim().equals("")) getString(R.string.no_phone_number) else p.phone
        (findViewById(R.id.website) as TextView).text =
            if (p.website.equals("-") || p.website!!.trim().equals("")) getString(R.string.no_website) else p.website

        description = findViewById(R.id.description) as WebView
        var html_data = "<style>img{max-width:100%;height:auto;} iframe{width:100%;}</style> "
        html_data += p.description
        description!!.getSettings().builtInZoomControls = true
        description!!.setBackgroundColor(Color.TRANSPARENT)
        description!!.setWebChromeClient(WebChromeClient())
        description!!.loadData(html_data, "text/html; charset=UTF-8", null)
        description!!.getSettings().javaScriptEnabled = true
        // disable scroll on touch
        description!!.setOnTouchListener(View.OnTouchListener { v, event -> event.action == MotionEvent.ACTION_MOVE })

        if (distance == -1f) {
            lyt_distance!!.setVisibility(View.GONE)
        } else {
            lyt_distance!!.setVisibility(View.VISIBLE)
            (findViewById(R.id.distance) as TextView).setText(Tools.getFormatedDistance(distance))
        }

        setImageGallery(db.getListImageByPlaceId(p.place_id))
    }

    override fun onResume() {
        if (!imgloader.isInited) Tools.initImageLoader(applicationContext)
        loadPlaceData()
        if (description != null) description!!.onResume()
//        prepareAds()
        super.onResume()
    }

    // this method name same with android:onClick="clickLayout" at layout xml
    fun clickLayout(view: View) {
        when (view.id) {
            R.id.lyt_address -> if (!place!!.isDraft()) {
                val uri =
                    Uri.parse("http://maps.google.com/maps?q=loc:" + place!!.lat + "," + place!!.lng)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            R.id.lyt_phone -> if (!place!!.isDraft() && !place!!.phone.equals("-") && !place!!.phone!!.trim().equals(
                    ""
                )
            ) {
                Tools.dialNumber(this, place!!.phone)
            } else {
                Snackbar.make(parent_view!!, R.string.fail_dial_number, Snackbar.LENGTH_SHORT)
                    .show()
            }
            R.id.lyt_website -> if (!place!!.isDraft() && !place!!.website.equals("-") && !place!!.website!!.trim()
                    .equals("")
            ) {
                Tools.directUrl(this, place!!.website)
            } else {
                Snackbar.make(parent_view!!, R.string.fail_open_website, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        fun setImageGallery(images: List<Images>) {
            // add optional image into list
            val new_images = ArrayList<Images>()
            val new_images_str = ArrayList<String>()
            new_images.add(Images(place!!.place_id, place!!.image))
            new_images.addAll(images)
            for (img in new_images) {
                new_images_str.add(Constant.getURLimgPlace(img.name))
            }

            val galleryRecycler = findViewById(R.id.galleryRecycler) as RecyclerView
            galleryRecycler.setLayoutManager(
                LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            )
            val adapter = AdapterImageList(new_images)
            galleryRecycler.setAdapter(adapter)
            adapter.setOnItemClickListener(object : AdapterImageList.OnItemClickListener() {
                override fun onItemClick(view: View, viewModel: String, pos: Int) {
                    val i = Intent(this@ActivityPlaceDetail, ActivityFullScreenImage::class.java)
                    i.putExtra(ActivityFullScreenImage.EXTRA_POS, pos)
                    i.putStringArrayListExtra(ActivityFullScreenImage.EXTRA_IMGS, new_images_str)
                    startActivity(i)
                }
            })
        }

        fun fabToggle() {
            if (db.isFavoritesExist(place!!.place_id)) {
                fab!!.setImageResource(R.drawable.ic_nav_favorites)
            } else {
                fab!!.setImageResource(R.drawable.ic_nav_favorites_outline)
            }
        }

        fun setupToolbar(name: String) {
            val toolbar = findViewById(R.id.toolbar) as Toolbar
            setSupportActionBar(toolbar)
            val actionBar = supportActionBar
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.setTitle("")

            (findViewById(R.id.toolbar_title) as TextView).text = name

            val collapsing_toolbar =
                findViewById(R.id.collapsing_toolbar) as CollapsingToolbarLayout
            collapsing_toolbar.setContentScrimColor(SharedPref(this).getThemeColorInt())
            (findViewById(R.id.app_bar_layout) as AppBarLayout).addOnOffsetChangedListener(object :
                AppBarLayout.OnOffsetChangedListener {
                override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
                    if (collapsing_toolbar.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(
                            collapsing_toolbar
                        )
                    ) {
                        fab!!.show()
                    } else {
                        fab!!.hide()
                    }
                }
            })
        }

        fun onCreateOptionsMenu(menu: Menu): Boolean {
            menuInflater.inflate(R.menu.menu_activity_details, menu)
            return true
        }


        fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                backAction()
                return true
            } else if (id == R.id.action_share) {
                if (!place!!.isDraft()) {
                    Tools.methodShare(this@ActivityPlaceDetails, place)
                }
            }
            return super.onOptionsItemSelected(item)
        }

        fun initMap() {
            if (googleMap == null) {
                val mapFragment1 = fragmentManager.findFragmentById(R.id.mapPlaces) as MapFragment
                mapFragment1.getMapAsync { gMap ->
                    googleMap = gMap
                    if (googleMap == null) {
                        Snackbar.make(
                            parent_view!!,
                            R.string.unable_create_map,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        // config map
                        googleMap =
                            Tools.configStaticMap(this@ActivityPlaceDetails, googleMap, place)
                    }
                }
            }

            (findViewById(R.id.bt_navigate) as Button).setOnClickListener {
                //Toast.makeText(getApplicationContext(),"OPEN", Toast.LENGTH_LONG).show();
                val navigation = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=" + place.lat + "," + place.lng)
                )
                startActivity(navigation)
            }
            (findViewById(R.id.bt_view) as Button).setOnClickListener { openPlaceInMap() }
            (findViewById(R.id.map) as LinearLayout).setOnClickListener { openPlaceInMap() }
        }

        fun openPlaceInMap() {
            val intent = Intent(this@ActivityPlaceDetails, ActivityMaps::class.java)
            intent.putExtra(ActivityMaps.EXTRA_OBJ, place)
            startActivity(intent)
        }

//        fun prepareAds() {
//            if (AppConfig.ADS_PLACE_DETAILS_BANNER && Tools.cekConnection(applicationContext)) {
//                val mAdView = findViewById(R.id.ad_view) as AdView
//                val adRequest =
//                    AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build()
//                // Start loading the ad in the background.
//                mAdView.loadAd(adRequest)
//            } else {
//                (findViewById(R.id.banner_layout) as RelativeLayout).visibility = View.GONE
//            }
//        }

        fun onDestroy() {
            if (callback != null && callback!!.isExecuted()) callback!!.cancel()
            super.onDestroy()
        }

        fun onBackPressed() {
            backAction()
        }

        fun onPause() {
            super.onPause()
            if (description != null) description!!.onPause()
        }

        fun backAction() {
            if (isFromNotif) {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            }
            finish()
        }

        // places detail load with lazy scheme
        fun loadPlaceData() {
            place = db.getPlace(place!!.place_id)
            if (place!!.isDraft()) {
                if (Tools.cekConnection(this)) {
                    requestDetailsPlace(place!!.place_id)
                } else {
                    onFailureRetry(getString(R.string.no_internet))
                }
            } else {
                displayData(place!!)
            }
        }

        fun requestDetailsPlace(place_id: Int) {
            if (onProcess) {
                Snackbar.make(parent_view!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
                return
            }
            onProcess = true
            showProgressbar(true)
            callback = RestAdapter.createAPI().getPlaceDetails(place_id)
            callback!!.enqueue(object : retrofit2.Callback<CallbackPlaceDetails> {
                override fun onResponse(
                    call: Call<CallbackPlaceDetails>,
                    response: Response<CallbackPlaceDetails>
                ) {
                    val resp = response.body()
                    if (resp != null) {
                        place = db.updatePlace(resp.place)
                        displayDataWithDelay(place)
                    } else {
                        onFailureRetry(getString(R.string.failed_load_details))
                    }

                }

                override fun onFailure(call: Call<CallbackPlaceDetails>, t: Throwable) {
                    if (call != null && !call.isCanceled) {
                        val conn = Tools.cekConnection(this@ActivityPlaceDetails)
                        if (conn) {
                            onFailureRetry(getString(R.string.failed_load_details))
                        } else {
                            onFailureRetry(getString(R.string.no_internet))
                        }
                    }
                }
            })
        }

        fun displayDataWithDelay(resp: Place) {
            Handler().postDelayed({
                showProgressbar(false)
                onProcess = false
                displayData(resp)
            }, 1000)
        }

        fun onFailureRetry(msg: String) {
            showProgressbar(false)
            onProcess = false
            snackbar = Snackbar.make(parent_view!!, msg, Snackbar.LENGTH_INDEFINITE)
            snackbar!!.setAction(R.string.RETRY, View.OnClickListener { loadPlaceData() })
            snackbar!!.show()
            retryDisplaySnackbar()
        }

        fun retryDisplaySnackbar() {
            if (snackbar != null && !snackbar!!.isShown()) {
                Handler().postDelayed({ retryDisplaySnackbar() }, 1000)
            }
        }

        fun showProgressbar(show: Boolean) {
            lyt_progress!!.setVisibility(if (show) View.VISIBLE else View.GONE)
        }
    }
}