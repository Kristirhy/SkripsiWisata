package com.example.wisatajogja.Widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration : RecyclerView.ItemDecoration() {

    private var spanCount: Int = 0
    private var spacingPx: Int = 0
    private var includeEdge: Boolean = false

    fun SpacingItemDecoration(spanCount: Int, spacingPx: Int, includeEdge: Boolean){
        this.spanCount = spanCount
        this.spacingPx = spacingPx
        this.includeEdge = includeEdge
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        if (includeEdge) {
            outRect.left = spacingPx - column * spacingPx / spanCount
            outRect.right = (column + 1) * spacingPx / spanCount

            if (position < spanCount) { // top edge
                outRect.top = spacingPx
            }
            outRect.bottom = spacingPx // item bottom
        } else {
            outRect.left = column * spacingPx / spanCount
            outRect.right = spacingPx - (column + 1) * spacingPx / spanCount
            if (position >= spanCount) {
                outRect.top = spacingPx // item top
            }
        }
    }
}