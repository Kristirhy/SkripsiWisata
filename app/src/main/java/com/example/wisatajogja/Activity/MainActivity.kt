package com.example.wisatajogja.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.Fragment.FragmentCategory
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//    private var mInterstitialAd: InterstitialAd? = null

    private val imgloader = ImageLoader.getInstance()

    var actionBar: ActionBar? = null
    var toolbar: Toolbar? = null
    private var cat: IntArray? = null
    private var navigationView: NavigationView? = null
    private var sharedPref: SharedPref? = null
    private var nav_header_lyt: RelativeLayout? = null

    var ctx: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ctx = this

        if (!imgloader.isInited()) Tools.initImageLoader(this)
        sharedPref = SharedPref(this)

        initToolbar()
        initDrawerMenu()
        prepareImageLoader()
        cat = resources.getIntArray(R.array.id_category)

        // first drawer view
        onItemSelected(R.id.nav_all, getString(R.string.title_nav_all))

        fab.setOnClickListener(View.OnClickListener {
            val i = Intent(this@MainActivity, ActivitySearch::class.java)
            startActivity(i)
        })

        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar!!)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        Tools.setActionBarColor(this, actionBar)
    }

    private fun initDrawerMenu() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
               updateFavoritesCounter(navigationView!!,
                   R.id.nav_favorites, db.getFavoritesSize())
                super.onDrawerOpened(drawerView)
            }
        }
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView!!.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener{

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                return onItemSelected(item.itemId, item.title.toString())
            }
        })
        if (!AppConfig.ENABLE_NEWS_INFO) navigationView!!.getMenu().removeItem(R.id.nav_news)

        // navigation header
        var nav_header = navigationView!!.getHeaderView(0)
        nav_header_lyt = nav_header.findViewById(R.id.nav_header_lyt) as RelativeLayout
        nav_header_lyt!!.setBackgroundColor(Tools.colorBrighter(sharedPref!!.getThemeColorInt()))
        nav_header.setOnClickListener {
            when(it.id){
                R.id.menu_nav_setting ->{
                    val i = Intent(applicationContext, ActivitySetting::class.java)
           startActivity(i)
                }
                R.id.menu_nav_map ->{
                    val i = Intent(applicationContext, ActivityMaps::class.java)
            startActivity(i)
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START)
        } else {
            doExitApp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val i = Intent(applicationContext, ActivitySetting::class.java)
        startActivity(i)
        } else if (id == R.id.action_rate) {
            Tools.rateAction(this@MainActivity)
        } else if (id == R.id.action_about) {
            Tools.aboutAction(this@MainActivity)
        }
        return super.onOptionsItemSelected(item)
    }


    fun onItemSelected(id: Int, title: String): Boolean {
        // Handle navigation view item clicks here.
        var fragment: androidx.fragment.app.Fragment? = null
        val bundle = Bundle()
        when (id) {
            //sub menu
            R.id.nav_all -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -1)
                actionBar!!.setTitle(title)
            }
            // favorites
            R.id.nav_favorites -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, -2)
                actionBar!!.setTitle(title)
            }
            // news info
            R.id.nav_news -> {
                val i = Intent(this, ActivityNewsInfo::class.java)
                startActivity(i)
            }

            R.id.nav_featured -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![10])
                actionBar!!.setTitle(title)
            }
            R.id.nav_tour -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![0])
                actionBar!!.setTitle(title)
            }
            R.id.nav_food -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![1])
                actionBar!!.setTitle(title)
            }
            R.id.nav_hotels -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![2])
                actionBar!!.setTitle(title)
            }
            R.id.nav_ent -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![3])
                actionBar!!.setTitle(title)
            }
            R.id.nav_sport -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![4])
                actionBar!!.setTitle(title)
            }
            R.id.nav_shop -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![5])
                actionBar!!.setTitle(title)
            }
            R.id.nav_transport -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![6])
            }
            R.id.nav_religion -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![7])
                actionBar!!.setTitle(title)
            }
            R.id.nav_public -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![8])
                actionBar!!.setTitle(title)
            }
            R.id.nav_money -> {
                fragment = FragmentCategory()
                bundle.putInt(FragmentCategory.TAG_CATEGORY, cat!![9])
                actionBar!!.setTitle(title)
            }
            else -> {
            }
        }/* IMPORTANT : cat[index_array], index is start from 0
             */

        if (fragment != null) {
            fragment!!.setArguments(bundle)
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_content, fragment)
            fragmentTransaction.commit()
        }
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private var exitTime: Long = 0

    fun doExitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, R.string.press_again_exit_app, Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    private fun prepareImageLoader() {
        Tools.initImageLoader(this)
    }

    override fun onResume() {
        if (!imgloader.isInited()) Tools.initImageLoader(this)
        updateFavoritesCounter(navigationView!!, R.id.nav_favorites, db.getFavoritesSize())
        if (actionBar != null) {
            Tools.setActionBarColor(this, actionBar)
            // for system bar in lollipop
            Tools.systemBarLolipop(this)
        }
        if (nav_header_lyt != null) {
            nav_header_lyt!!.setBackgroundColor(Tools.colorBrighter(sharedPref!!.getThemeColorInt()))
        }
        super.onResume()
    }

//    internal var active = false

    var active = false

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
    }

//    fun onStart() {
//        super.onStart()
//        active = true
//    }

//    protected fun onDestroy() {
//        super.onDestroy()
//        active = false
//    }


    private fun updateFavoritesCounter(nav: NavigationView, @IdRes itemId: Int, count: Int) {
        val view = nav.menu.findItem(itemId).actionView.findViewById(R.id.counter) as TextView
        view.text = count.toString()
    }



    fun animateFab(hide: Boolean) {
        val f_ab = this.findViewById(R.id.fab) as FloatingActionButton
        val moveY:Float = (if (hide) 2 * f_ab.height else 0).toFloat()
        f_ab.animate().translationY(moveY).setStartDelay(100).setDuration(400).start()
    }
}
