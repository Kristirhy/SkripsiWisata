package com.example.wisatajogja.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wisatajogja.Adapter.AdapterNewsInfo
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import java.util.ArrayList

class ActivityNewsInfo : AppCompatActivity() {

    var actionBar: ActionBar? = null
    private var parent_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterNewsInfo? = null
    private var lyt_progress: View? = null
    private var callbackCall: Call<CallbackListNewsInfo>? = null
//    private var db: DatabaseHandler? = null

    private var post_total = 0
    private var failed_page = 0
    private var snackbar_retry: Snackbar? = null

    // can be, ONLINE or OFFLINE
    private var MODE = "ONLINE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_info)
        parent_view = findViewById(android.R.id.content)

        initToolbar()
        iniComponent()
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!! .setHomeButtonEnabled (true)
        actionBar!!.setTitle(R.string.title_nav_news)
        Tools.systemBarLolipop(this)
    }

    fun iniComponent() {
        lyt_progress = findViewById(R.id.lyt_progress)
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        recyclerView!!.addItemDecoration(SpacingItemDecoration(1, Tools.dpToPx(this, 4), true))


        //set data and list adapter
        mAdapter = AdapterNewsInfo(this, recyclerView!!, ArrayList<NewsInfo>())
        recyclerView!!.setAdapter(mAdapter)

        // on item list clicked
        mAdapter!!.setOnItemClickListener(object : AdapterNewsInfo.OnItemClickListener() {
            override fun onItemClick(v: View, obj: NewsInfo, position: Int) {
                ActivityNewsInfoDetails.navigate(this@ActivityNewsInfo, obj, false)
            }
        })

        // detect when scroll reach bottom
        mAdapter!!.setOnLoadMoreListener(object : AdapterNewsInfo.OnLoadMoreListener {
            override fun onLoadMore(current_page: Int) {
                if (post_total > mAdapter!!.getItemCount() && current_page != 0) {
                    val next_page = current_page + 1
                    requestAction(next_page)
                } else {
                    mAdapter!!.setLoaded()
                }
            }
        })

        // if already have data news at db, use mode OFFLINE
        if (db.getNewsInfoSize() > 0) {
            MODE = "OFFLINE"
        }
        requestAction(1)
    }
}