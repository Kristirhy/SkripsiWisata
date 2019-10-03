package com.example.wisatajogja.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.example.wisatajogja.Connection.callbacks.CallbackPlaceDetails
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.nostra13.universalimageloader.core.ImageLoader
import retrofit2.Call

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

        fab = findViewById(R.id.fab) as FloatingActionButton
        lyt_progress = findViewById(R.id.lyt_progress)
        lyt_distance = findViewById(R.id.lyt_distance)
        imgloader.displayImage(
            Constant.getURLimgPlace(place.image),
            findViewById(R.id.image) as ImageView
        )
        distance = place.distance

        fabToggle()
        setupToolbar(place.name)
        initMap()

        // handle when favorite button clicked
        fab.setOnClickListener(View.OnClickListener {
            if (db.isFavoritesExist(place.place_id)) {
                db.deleteFavorites(place.place_id)
                Snackbar.make(
                    parent_view,
                    place.name + " " + getString(R.string.remove_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
                // analytics tracking
                ThisApplication.getInstance()
                    .trackEvent(Constant.Event.FAVORITES.name(), "REMOVE", place.name)
            } else {
                db.addFavorites(place.place_id)
                Snackbar.make(
                    parent_view,
                    place.name + " " + getString(R.string.add_favorite),
                    Snackbar.LENGTH_SHORT
                ).show()
                // analytics tracking
                ThisApplication.getInstance()
                    .trackEvent(Constant.Event.FAVORITES.name(), "ADD", place.name)
            }
            fabToggle()
        })

        // for system bar in lollipop
        Tools.systemBarLolipop(this)

        // analytics tracking
        ThisApplication.getInstance().trackScreenView("View place : " + place.name)
    }

}