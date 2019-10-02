package com.example.wisatajogja.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.balysv.materialripple.MaterialRippleLayout
import com.example.wisatajogja.R
import com.example.wisatajogja.Utils.Tools
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.ArrayList

class AdapterPlaceGrid : RecyclerView {

    private val VIEW_ITEM = 1
    private val VIEW_PROG = 0
    private var loading: Boolean = false

    private var ctx: Context? = null
    private var items: MutableList<Place> = ArrayList<Place>()
    private val imgloader = ImageLoader.getInstance()

    private var onLoadMoreListener: AdapterNewsInfo.OnLoadMoreListener? = null
    private var onItemClickListener: OnItemClickListener? = null

    private var lastPosition = -1
    private var clicked = false

    interface OnItemClickListener {
        fun onItemClick(view: View, viewModel: Place)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var title: TextView
        var image: ImageView
        var distance: TextView
        var lyt_distance: LinearLayout
        var lyt_parent: MaterialRippleLayout

        init {
            title = v.findViewById(R.id.title) as TextView
            image = v.findViewById(R.id.image) as ImageView
            distance = v.findViewById(R.id.distance) as TextView
            lyt_distance = v.findViewById(R.id.lyt_distance) as LinearLayout
            lyt_parent = v.findViewById(R.id.lyt_parent) as MaterialRippleLayout
        }
    }

    class ProgressViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var progressBar: ProgressBar

        init {
            progressBar = v.findViewById(R.id.progressBar1) as ProgressBar
        }
    }

    fun AdapterPlaceGrid(ctx: Context, view: RecyclerView, items: List<Place>){
        this.ctx = ctx
        this.items = items
        if (!imgloader.isInited) Tools.initImageLoader(ctx)
        lastItemViewDetector(view)
    }

    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        if (viewType == VIEW_ITEM) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
            vh = ViewHolder(v)
        } else {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            vh = ProgressViewHolder(v)
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val p = items[position]
            holder.title.setText(p.name)
            imgloader.displayImage(
                Constant.getURLimgPlace(p.image),
                holder.image,
                Tools.getGridOption()
            )

            if (p.distance === -1) {
                holder.lyt_distance.visibility = View.GONE
            } else {
                holder.lyt_distance.visibility = View.VISIBLE
                holder.distance.setText(Tools.getFormatedDistance(p.distance))
            }

            // Here you apply the animation when the view is bound
            setAnimation(holder.lyt_parent, position)

            holder.lyt_parent.setOnClickListener { v ->
                if (!clicked && onItemClickListener != null) {
                    clicked = true
                    onItemClickListener!!.onItemClick(v, p)
                }
            }
            clicked = false
        } else {
            (holder as ProgressViewHolder).progressBar.isIndeterminate = true
        }
        if (getItemViewType(position) == VIEW_PROG) {
            val layoutParams =
                holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.setFullSpan(true)
        } else {
            val layoutParams =
                holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.setFullSpan(false)
        }
    }

    fun getItemCount(): Int {
        return items.size
    }

    fun getItemViewType(position: Int): Int {
        return if (this.items[position] != null) VIEW_ITEM else VIEW_PROG
    }

    fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Here is the key method to apply the animation
    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(ctx, R.anim.slide_in_bottom)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    fun insertData(items: List<Place>) {
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
        }
    }

    fun resetListData() {
        this.items = ArrayList<Place>()
        notifyDataSetChanged()
    }

    fun setOnLoadMoreListener(onLoadMoreListener: OnLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener
    }

    private fun lastItemViewDetector(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
            val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastPos =
                        getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null))
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        val current_page = getItemCount() / Constant.LIMIT_LOADMORE
                        onLoadMoreListener!!.onLoadMore(current_page)
                        loading = true
                    }
                }
            })
        }
    }

    interface OnLoadMoreListener : AdapterNewsInfo.OnLoadMoreListener {
        override fun onLoadMore(current_page: Int)
    }

    private fun getLastVisibleItem(into: IntArray): Int {
        var last_idx = into[0]
        for (i in into) {
            if (last_idx < i) last_idx = i
        }
        return last_idx
    }

}
