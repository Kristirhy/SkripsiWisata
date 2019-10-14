package com.example.wisatajogja.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wisatajogja.Data.Constant
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.ArrayList

class AdapterNewsInfo : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_ITEM = 1
    private val VIEW_PROG = 0

    private var items: MutableList<NewsInfo> = ArrayList<NewsInfo>()
    private val imgloader = ImageLoader.getInstance()

    private var loading: Boolean = false
    private var onLoadMoreListener: OnLoadMoreListener? = null

    private var ctx: Context? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View, obj: NewsInfo, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mItemClickListener
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    constructor(context: Context, view: RecyclerView, items: List<NewsInfo>) {
        this.items = items
        ctx = context
        lastItemViewDetector(view)
        if (!imgloader.isInited) Tools.initImageLoader(ctx)
    }

    inner class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var title: TextView
        var brief_content: TextView
        var image: ImageView
        var lyt_parent: LinearLayout

        init {
            title = v.findViewById(R.id.title) as TextView
            brief_content = v.findViewById(R.id.brief_content) as TextView
            image = v.findViewById(R.id.image) as ImageView
            lyt_parent = v.findViewById(R.id.lyt_parent) as LinearLayout
        }
    }

    class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var progressBar: ProgressBar

        init {
            progressBar = v.findViewById(R.id.progressBar1) as ProgressBar
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        if (viewType == VIEW_ITEM) {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_news_info, parent, false)
            vh = OriginalViewHolder(v)
        } else {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            vh = ProgressViewHolder(v)
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val o = items[position]
            holder.title.setText(o.title)
            holder.brief_content.setText(o.brief_content)
            imgloader.displayImage(
                Constant.getURLimgNews(o.image),
                holder.image,
                Tools.getGridOption()
            )
            holder.lyt_parent.setOnClickListener {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(it, o, position)
                }
            }
        } else {
            (holder as ProgressViewHolder).progressBar.isIndeterminate = true
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
   override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.items[position] != null) VIEW_ITEM else VIEW_PROG
    }

    fun insertData(items: List<NewsInfo>) {
        setLoaded()
        val positionStart = getItemCount()
        val itemCount = items.size
        this.items.addAll(items)
        notifyItemRangeInserted(positionStart, itemCount)
    }

    fun setLoaded() {
        loading = false
        for (i in 0 until getItemCount()) {
            if (items[i] == null) {
                items.removeAt(i)
                notifyItemRemoved(i)
            }
        }
    }

    fun setLoading() {
        if (getItemCount() != 0) {
            this.items.add(null)
            notifyItemInserted(getItemCount() - 1)
            loading = true
        }
    }


    fun resetListData() {
        this.items = ArrayList<NewsInfo>()
        notifyDataSetChanged()
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    private fun lastItemViewDetector(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is LinearLayoutManager) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastPos = layoutManager.findLastVisibleItemPosition()
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        if (onLoadMoreListener != null) {
                            val current_page = getItemCount() / Constant.LIMIT_NEWS_REQUEST
                            onLoadMoreListener!!.onLoadMore(current_page)
                        }
                        loading = true
                    }
                }
            })
        }
    }

    interface OnLoadMoreListener {
        fun onLoadMore(current_page: Int)
    }

}
