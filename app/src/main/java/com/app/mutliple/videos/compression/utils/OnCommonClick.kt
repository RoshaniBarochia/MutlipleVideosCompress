package com.app.mutliple.videos.compression.utils

import android.view.View

interface OnCommonClick {
    fun onViewClick(
        pos: Int,
        action: String,
        view: View,
        tag : String = ""
    )
}