package com.app.mutliple.videos.compression.model

import com.google.gson.annotations.SerializedName

class MultiMedia (
    val media_file_name : String = "",
    @SerializedName("thumbnail")
    val media_thumb : String = "",
    var hieght : Int = 0,
    var width : Int = 0,
    val duration : Any = "",
   // var time : Int = -1,
    var time : String = "",
    var isTime : Boolean = false
)