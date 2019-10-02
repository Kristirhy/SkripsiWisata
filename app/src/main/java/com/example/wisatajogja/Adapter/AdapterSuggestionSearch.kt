package com.example.wisatajogja.Adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wisatajogja.R
import com.google.gson.Gson
import java.io.Serializable
import java.util.*

class AdapterSuggestionSearch : RecyclerView.Adapter<AdapterSuggestionSearch.ViewHolder>() {

    private val SEARCH_HISTORY_KEY = "_SEARCH_HISTORY_KEY"
    private val MAX_HISTORY_ITEMS = 5

    private var items: List<String> = ArrayList()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null
    lateinit var prefs: SharedPreferences

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var title: TextView
        var lyt_parent: LinearLayout

        init {
            title = v.findViewById(R.id.title) as TextView
            lyt_parent = v.findViewById(R.id.lyt_parent) as LinearLayout
        }
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    fun AdapterSuggestionSearch(context: Context){
        prefs = context.getSharedPreferences("PREF_RECENT_SEARCH", Context.MODE_PRIVATE)
        this.items = getSearchHistory()
        Collections.reverse(this.items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = items[position]
        holder.title.text = p
        holder.lyt_parent.setOnClickListener { v ->
            onItemClickListener!!.onItemClick(
                v, p, position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, viewModel: String, pos: Int)
    }

    fun refreshItems() {
        this.items = getSearchHistory()
        Collections.reverse(this.items)
        notifyDataSetChanged()
    }

    private inner class SearchObject(items: MutableList<String>) : Serializable {

        var items: MutableList<String> = ArrayList()

        init {
            this.items = items
        }
    }

    /**
     * To save last state request
     */
    fun addSearchHistory(s: String) {
        val searchObject = SearchObject(getSearchHistory())
        if (searchObject.items.contains(s)) searchObject.items.remove(s)
        searchObject.items.add(s)
        if (searchObject.items.size > MAX_HISTORY_ITEMS) searchObject.items.removeAt(0)
        val json = Gson().toJson(searchObject, SearchObject::class.java)
        prefs.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    private fun getSearchHistory(): List<String> {
        val json = prefs.getString(SEARCH_HISTORY_KEY, "")
        if (json == "") return ArrayList()
        val searchObject = Gson().fromJson(json, SearchObject::class.java)
        return searchObject.items
    }
}
