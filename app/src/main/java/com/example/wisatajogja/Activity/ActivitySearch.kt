package com.example.wisatajogja.Activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import java.util.ArrayList

class ActivitySearch : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var actionBar: ActionBar? = null
    private var et_search: EditText? = null
    private var bt_clear: ImageButton? = null
    private var parent_view: View? = null

    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterPlaceGrid? = null

    private var recyclerSuggestion: RecyclerView? = null
    private var mAdapterSuggestion: AdapterSuggestionSearch? = null
    private var lyt_suggestion: LinearLayout? = null

//    private var db: DatabaseHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        parent_view = findViewById(android.R.id.content)

        initComponent()
        setupToolbar()
    }

    private fun initComponent() {
        lyt_suggestion = findViewById(R.id.lyt_suggestion) as LinearLayout
        et_search = findViewById(R.id.et_search) as EditText
        et_search!!.addTextChangedListener(textWatcher)

        bt_clear = findViewById(R.id.bt_clear) as ImageButton
        bt_clear!!.setVisibility(View.GONE)
        recyclerView = findViewById(R.id.recyclerView) as RecyclerView
        recyclerSuggestion = findViewById(R.id.recyclerSuggestion) as RecyclerView

        recyclerView!!.setLayoutManager(
            StaggeredGridLayoutManager(
                Tools.getGridSpanCount(this),
                StaggeredGridLayoutManager.VERTICAL
            )
        )
        recyclerView!!.addItemDecoration(
            SpacingItemDecoration(
                Tools.getGridSpanCount(this),
                Tools.dpToPx(this, 4),
                true
            )
        )


        recyclerSuggestion!!.setLayoutManager(LinearLayoutManager(this))
        recyclerSuggestion!!.setHasFixedSize(true)

        //set data and list adapter
        mAdapter = AdapterPlaceGrid(this, recyclerView, ArrayList<Place>())
        recyclerView.setAdapter(mAdapter)
        mAdapter.setOnItemClickListener(object : AdapterPlaceGrid.OnItemClickListener() {
            fun onItemClick(v: View, obj: Place) {
                ActivityPlaceDetail.navigate(
                    this@ActivitySearch,
                    v.findViewById(R.id.lyt_content),
                    obj
                )
            }
        })

        //set data and list adapter suggestion
        mAdapterSuggestion = AdapterSuggestionSearch(this)
        recyclerSuggestion!!.setAdapter(mAdapterSuggestion)
        showSuggestionSearch()
        mAdapterSuggestion.setOnItemClickListener(object :
            AdapterSuggestionSearch.OnItemClickListener() {
            fun onItemClick(view: View, viewModel: String, pos: Int) {
                et_search!!.setText(viewModel)
                lyt_suggestion!!.setVisibility(View.GONE)
                hideKeyboard()
                searchAction()
            }
        })

        bt_clear!!.setOnClickListener(View.OnClickListener {
            et_search!!.setText("")
            mAdapter.resetListData()
            showNotFoundView()
        })

        et_search!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                searchAction()
                return@OnEditorActionListener true
            }
            false
        })

        et_search!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            showSuggestionSearch()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            false
        })

        showNotFoundView()
    }

    private fun showSuggestionSearch() {
        mAdapterSuggestion.refreshItems()
        lyt_suggestion!!.setVisibility(View.VISIBLE)
    }

    override fun onResume() {
        mAdapter.notifyDataSetChanged()
        super.onResume()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        // for system bar in lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.grey_medium)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    internal var textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {
            if (c.toString().trim { it <= ' ' }.length == 0) {
                bt_clear!!.setVisibility(View.GONE)
            } else {
                bt_clear!!.setVisibility(View.VISIBLE)
            }
        }

        override fun beforeTextChanged(c: CharSequence, i: Int, i1: Int, i2: Int) {}

        override fun afterTextChanged(editable: Editable) {}
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun searchAction() {
        lyt_suggestion!!.setVisibility(View.GONE)
        showNotFoundView()
        val query = et_search!!.getText().toString().trim { it <= ' ' }
        if (query != "") {
            mAdapterSuggestion.addSearchHistory(query)
            mAdapter.resetListData()
            mAdapter.insertData(Tools.filterItemsWithDistance(this, db.searchAllPlace(query)))
            showNotFoundView()
        } else {
            Toast.makeText(this, R.string.please_fill, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotFoundView() {
        val lyt_no_item = findViewById(R.id.lyt_no_item) as View
        if (mAdapter.getItemCount() === 0) {
            recyclerView!!.setVisibility(View.GONE)
            lyt_no_item.visibility = View.VISIBLE
        } else {
            recyclerView!!.setVisibility(View.VISIBLE)
            lyt_no_item.visibility = View.GONE
        }
    }
}
