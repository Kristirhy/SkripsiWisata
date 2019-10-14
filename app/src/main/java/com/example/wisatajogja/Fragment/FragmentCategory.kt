package com.example.wisatajogja.Fragment

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.wisatajogja.Activity.ActivityPlaceDetails
import com.example.wisatajogja.Activity.MainActivity
import com.example.wisatajogja.Adapter.AdapterPlaceGrid
import com.example.wisatajogja.Connection.RestAdapter
import com.example.wisatajogja.Connection.callbacks.CallbackListPlace
import com.example.wisatajogja.Data.AppConfig
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.Data.SharedPref
import com.example.wisatajogja.Data.ThisApplication
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

class FragmentCategory : Fragment() {

    var TAG_CATEGORY = "key.TAG_CATEGORY"

    private var count_total = 0
    private var category_id: Int = 0

    private var root_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var lyt_progress: View? = null
    private var lyt_not_found: View? = null
    private var text_progress: TextView? = null
    private var snackbar_retry: Snackbar? = null

    //    private var db: DatabaseHandler? = null
    private var sharedPref: SharedPref? = null
    private var adapter: AdapterPlaceGrid? = null

    private var callback: Call<CallbackListPlace>? = null

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        root_view = inflater.inflate(R.layout.fragment_category, null)


        // activate fragment menu
        setHasOptionsMenu(true)

//        db = DatabaseHandler(activity)
        sharedPref = SharedPref(activity!!)
        category_id = arguments!!.getInt(TAG_CATEGORY)

        recyclerView = root_view!!.findViewById(R.id.recycler) as RecyclerView
        lyt_progress = root_view!!.findViewById(R.id.lyt_progress)
        lyt_not_found = root_view!!.findViewById(R.id.lyt_not_found)
        text_progress = root_view!!.findViewById(R.id.text_progress) as TextView

        recyclerView!!.setLayoutManager(
            StaggeredGridLayoutManager(
                Tools.getGridSpanCount(activity),
                StaggeredGridLayoutManager.VERTICAL
            )
        )
        recyclerView!!.addItemDecoration(
            SpacingItemDecoration(
                Tools.getGridSpanCount(activity), Tools.dpToPx(
                    activity, 4
                ), true
            )
        )

        //set data and list adapter
        adapter = AdapterPlaceGrid(activity, recyclerView, ArrayList<Place>())
        recyclerView!!.setAdapter(adapter)
        // on item list clicked
        adapter!!.setOnItemClickListener(object : AdapterPlaceGrid.OnItemClickListener {
            override fun onItemClick(v: View, obj: Place) {
                ActivityPlaceDetails.navigate(
                    activity as MainActivity,
                    v.findViewById(R.id.lyt_content),
                    obj
                )
            }
        })

        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(v: RecyclerView, state: Int) {
                super.onScrollStateChanged(v, state)
                if (state == v.SCROLL_STATE_DRAGGING || state == v.SCROLL_STATE_SETTLING) {
                    MainActivity.animateFab(true)
                } else {
                    MainActivity.animateFab(false)
                }
            }
        })
        startLoadMoreAdapter()
        return root_view
    }

    override fun onDestroyView() {
        if (snackbar_retry != null) snackbar_retry!!.dismiss()
        if (callback != null && callback!!.isExecuted()) {
            callback!!.cancel()
        }
        super.onDestroyView()
    }

    override fun onResume() {
        adapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref!!.isRefreshPlaces() || db.getPlacesSize() === 0) {
            actionRefresh(sharedPref!!.getLastPlacePage())
        } else {
            startLoadMoreAdapter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_category, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            ThisApplication.getInstance().setLocation(null)
            sharedPref!!.setLastPlacePage(1)
            sharedPref!!.setRefreshPlaces(true)
            text_progress!!.setText("")
            if (snackbar_retry != null) snackbar_retry!!.dismiss()
            actionRefresh(sharedPref!!.getLastPlacePage())
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startLoadMoreAdapter() {
        adapter!!.resetListData()
        val items = db.getPlacesByPage(category_id, Constant.LIMIT_LOADMORE, 0)
        adapter!!.insertData(items)
        showNoItemView()
        val item_count = db.getPlacesSize(category_id) as Int
        // detect when scroll reach bottom
        adapter!!.setOnLoadMoreListener(object : AdapterPlaceGrid.OnLoadMoreListener {
            override fun onLoadMore(current_page: Int) {
                if (item_count > adapter!!.getItemCount() && current_page != 0) {
                    displayDataByPage(current_page)
                } else {
                    adapter!!.setLoaded()
                }
            }
        })
    }

    private fun displayDataByPage(next_page: Int) {
        adapter!!.setLoading()
        Handler().postDelayed({
            val items = db.getPlacesByPage(
                category_id, Constant.LIMIT_LOADMORE,
                next_page * Constant.LIMIT_LOADMORE
            )
            adapter!!.insertData(items)
            showNoItemView()
        }, 500)
    }

    // checking some condition before perform refresh data
    private fun actionRefresh(page_no: Int) {
        val conn = Tools.cekConnection(activity)
        if (conn) {
            if (!onProcess) {
                onRefresh(page_no)
            } else {
                Snackbar.make(root_view!!, R.string.task_running, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            onFailureRetry(page_no, getString(R.string.no_internet))
        }
    }

    private var onProcess = false

    private void onRefresh(final int page_no) {
        onProcess = true
        showProgress(onProcess)
        callback = RestAdapter.createAPI().getPlacesByPage(
            page_no, Constant.LIMIT_PLACE_REQUEST,
            if (AppConfig.LAZY_LOAD) 1 else 0
        )
        callback!!.enqueue(new retrofit2 . Callback < CallbackListPlace >() {
            fun onResponse(
                call: Call<CallbackListPlace>,
                response: Response<CallbackListPlace>
            ) {
                val resp = response.body()
                if (resp != null) {
                    count_total = resp.count_total
                    if (page_no == 1) db.refreshTablePlace()
                    db.insertListPlace(resp.places)  // save result into database
                    sharedPref!!.setLastPlacePage(page_no + 1)
                    delayNextRequest(page_no)
                    val str_progress = String.format(
                        getString(R.string.load_of),
                        page_no * Constant.LIMIT_PLACE_REQUEST, count_total
                    )
                    text_progress.setText(str_progress)
                } else {
                    onFailureRetry(page_no, getString(R.string.refresh_failed))
                }
            }

            override fun onFailure(call: Call<CallbackListPlace>, t: Throwable) {
                if (call != null && !call.isCanceled) {
                    Log.e("onFailure", t.message)
                    val conn = Tools.cekConnection(activity)
                    if (conn) {
                        onFailureRetry(page_no, getString(R.string.refresh_failed))
                    } else {
                        onFailureRetry(page_no, getString(R.string.no_internet))
                    }
                }
            }
        });
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            lyt_progress!!.setVisibility(View.VISIBLE)
            recyclerView!!.setVisibility(View.GONE)
            lyt_not_found!!.setVisibility(View.GONE)
        } else {
            lyt_progress!!.setVisibility(View.GONE)
            recyclerView!!.setVisibility(View.VISIBLE)
        }
    }

    private fun showNoItemView() {
        if (adapter!!.getItemCount() === 0) {
            lyt_not_found!!.setVisibility(View.VISIBLE)
        } else {
            lyt_not_found!!.setVisibility(View.GONE)
        }
    }

    private fun onFailureRetry(page_no: Int, msg: String) {
        onProcess = false
        showProgress(onProcess)
        showNoItemView()
        startLoadMoreAdapter()
        snackbar_retry = Snackbar.make(root_view!!, msg, Snackbar.LENGTH_INDEFINITE)
        snackbar_retry!!.setAction(R.string.RETRY,
            View.OnClickListener { actionRefresh(page_no) })
        snackbar_retry!!.show()
    }

    private fun delayNextRequest(page_no: Int) {
        if (count_total == 0) {
            onFailureRetry(page_no, getString(R.string.refresh_failed))
            return
        }
        if (page_no * Constant.LIMIT_PLACE_REQUEST > count_total) { // when all data loaded
            onProcess = false
            showProgress(onProcess)
            startLoadMoreAdapter()
            sharedPref!!.setRefreshPlaces(false)
            text_progress!!.setText("")
            Snackbar.make(root_view!!, R.string.load_success, Snackbar.LENGTH_LONG).show()
            return
        }
        Handler().postDelayed({ onRefresh(page_no + 1) }, 500)
    }
}