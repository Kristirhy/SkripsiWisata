package com.example.wisatajogja.Activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Model.Category
import com.example.wisatajogja.Model.Place
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.PermissionUtil
import com.example.wisatajogja.Utils.Tools
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.maps_marker.*
import java.util.HashMap

class ActivityMaps : AppCompatActivity(), OnMapReadyCallback {

    val EXTRA_OBJ = "key.EXTRA_OBJ"

    private var mMap: GoogleMap? = null
    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
//    private var db: DatabaseHandler? = null
    private var mClusterManager: ClusterManager<Place>? = null
    private var parent_view: View? = null
    private var cat: IntArray? = null
    private var placeMarkerRenderer: PlaceMarkerRenderer? = null

    // for single place
    private var ext_place: Place? = null
    private var isSinglePlace: Boolean = false
    internal var hashMapPlaces = HashMap<String, Place>()

    // id category
    private var cat_id = -1

    private var cur_category: Category? = null

    // view for custom marker
    private var icon: ImageView? = null
    private var marker_bg:ImageView? = null
    private var marker_view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        parent_view = findViewById(android.R.id.content)

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        marker_view = inflater.inflate(R.layout.maps_marker, null)
        icon = marker_view!!.findViewById(R.id.marker_icon) as ImageView
        marker_bg = marker_view!!.findViewById(R.id.marker_bg) as ImageView

        ext_place = intent.getSerializableExtra(EXTRA_OBJ) as Place
        isSinglePlace = ext_place != null

//        db = DatabaseHandler(this)
        initMapFragment()
        initToolbar()

        cat = resources.getIntArray(R.array.id_category)

        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = Tools.configActivityMaps(googleMap)
        val location: CameraUpdate
        if (isSinglePlace) {
            marker_bg!!.setColorFilter(resources.getColor(R.color.marker_secondary))
            val markerOptions =
                MarkerOptions().title(ext_place!!.name).position(ext_place!!.getPosition())
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    Tools.createBitmapFromView(
                        this@ActivityMaps,
                        marker_view
                    )
                )
            )
            mMap!!.addMarker(markerOptions)
            location = CameraUpdateFactory.newLatLngZoom(ext_place!!.getPosition(), 12f)
            actionBar!!.setTitle(ext_place!!.name)
        } else {
            location =
                CameraUpdateFactory.newLatLngZoom(LatLng(Constant.city_lat, Constant.city_lng), 9f)
            mClusterManager = ClusterManager(this, mMap)
            placeMarkerRenderer = PlaceMarkerRenderer(this, mMap!!, mClusterManager!!)
            mClusterManager!!.setRenderer(placeMarkerRenderer)
            mMap!!.setOnCameraChangeListener(mClusterManager)
            loadClusterManager(db.getAllPlace())
        }
        mMap!!.animateCamera(location)
        mMap!!.setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener { marker ->
            val place: Place
            if (hashMapPlaces[marker.id] != null) {
                place = hashMapPlaces[marker.id] as Place
            } else {
                place = ext_place!!
            }
            ActivityPlaceDetails.navigate(this@ActivityMaps, parent_view, place)
        })

        showMyLocation()
    }

    private fun showMyLocation() {
        if (PermissionUtil.isLocationGranted(this)) {
            // Enable / Disable my location button
            mMap!!.getUiSettings().isMyLocationButtonEnabled = true
            mMap!!.setMyLocationEnabled(true)
            mMap!!.setOnMyLocationButtonClickListener(GoogleMap.OnMyLocationButtonClickListener {
                try {
                    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showAlertDialogGps()
                    } else {
                        val loc = Tools.getLastKnownLocation(this@ActivityMaps)
                        val myCam = CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                loc.getLatitude(),
                                loc.getLongitude()
                            ), 12f
                        )
                        mMap!!.animateCamera(myCam)
                    }
                } catch (e: Exception) {
                }

                true
            })
        }
    }

    private fun loadClusterManager(places: List<Place>) {
        mClusterManager!!.clearItems()
        mClusterManager!!.addItems(places)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
//                as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        actionBar!!.setTitle(R.string.activity_title_maps)
        Tools.setActionBarColor(this, actionBar)
    }

    private fun initMapFragment() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private inner class PlaceMarkerRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<Place>
    ) :
        DefaultClusterRenderer<Place>(context, map, clusterManager) {

        override fun onBeforeClusterItemRendered(item: Place?, markerOptions: MarkerOptions?) {
            if (cat_id == -1) { // all place
                icon!!.setImageResource(R.drawable.round_shape)
            } else {
                icon!!.setImageResource(cur_category.icon)
            }
            marker_bg!!.setColorFilter(resources.getColor(R.color.marker_primary))
            markerOptions!!.title(item!!.name)
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                    Tools.createBitmapFromView(
                        this@ActivityMaps,
                        marker_view
                    )
                )
            )
            if (ext_place != null && ext_place!!.place_id === item.place_id) {
                markerOptions.visible(false)
            }
        }

        override fun onClusterItemRendered(item: Place?, marker: Marker) {
            hashMapPlaces[marker.id] = item!!
            super.onClusterItemRendered(item, marker)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            super.onBackPressed()
            return true
        } else {
            val category_text: String
            if (item.itemId != R.id.menu_category) {
                category_text = item.title.toString()
                when (item.itemId) {
                    R.id.nav_all -> cat_id = -1
                    R.id.nav_pilihan -> cat_id = cat!![12]
                    R.id.nav_alam -> cat_id = cat!![0]
                    R.id.nav_sejarah -> cat_id = cat!![1]
                    R.id.nav_air -> cat_id = cat!![2]
                    R.id.nav_food -> cat_id = cat!![3]
                    R.id.nav_edukasi -> cat_id = cat!![4]
                    R.id.nav_shop -> cat_id = cat!![5]
                    R.id.nav_religion -> cat_id = cat!![6]
                    R.id.nav_transport -> cat_id = cat!![7]
                    R.id.nav_hotels -> cat_id = cat!![8]
                    R.id.nav_public -> cat_id = cat!![9]
                    R.id.nav_sport -> cat_id = cat!![10]
                    R.id.nav_publik -> cat_id = cat!![11]
                    R.id.nav_oleh -> cat_id = cat!![12]
                }

                // get category object when menu click
                cur_category = db.getCategory(cat_id)

                if (isSinglePlace) {
                    isSinglePlace = false
                    mClusterManager = ClusterManager(this, mMap)
                    mMap!!.setOnCameraChangeListener(mClusterManager)
                }

                val places = db.getAllPlaceByCategory(cat_id)
                loadClusterManager(places)
                if (places.size == 0) {
                    Snackbar.make(
                        parent_view!!,
                        getString(R.string.no_item_at) + " " + item.title.toString(),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                placeMarkerRenderer = PlaceMarkerRenderer(this, mMap!!, mClusterManager!!)
                mClusterManager!!.setRenderer(placeMarkerRenderer)

                actionBar!!.setTitle(category_text)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialogGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.dialog_content_gps)
        builder.setPositiveButton(R.string.YES,
            DialogInterface.OnClickListener { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
        builder.setNegativeButton(R.string.NO,
            DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()
    }
}