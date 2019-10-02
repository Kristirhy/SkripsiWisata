package com.example.wisatajogja.Adapter

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.balysv.materialripple.MaterialRippleLayout
import com.example.wisatajogja.R
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.ArrayList

class AdapterImageList : RecyclerView.Adapter<AdapterImageList.ViewHolder>() {

    private var items = ArrayList<MediaStore.Images>()
    private val imgloader = ImageLoader.getInstance()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null

    private val lastPosition = -1

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var image: ImageView
        var lyt_parent: MaterialRippleLayout

        init {
            image = v.findViewById(R.id.image) as ImageView
            lyt_parent = v.findViewById(R.id.lyt_parent) as MaterialRippleLayout
        }
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    fun AdapterImageList(items: List<MediaStore.Images>){
        this.items = items as ArrayList<MediaStore.Images>
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(v)
    }

    // ReString the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = items[position].getImageUrl()
        imgloader.displayImage(Constant.getURLimgPlace(p), holder.image)
        holder.lyt_parent.setOnClickListener { v ->
            // Give some delay to the ripple to finish the effect
            onItemClickListener!!.onItemClick(v, p, position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, viewModel: String, pos: Int)
    }
}
