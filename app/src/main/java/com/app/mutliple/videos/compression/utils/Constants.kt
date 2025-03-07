package com.app.mutliple.videos.compression.utils



object Constants{

    const val VIDEO_FORMAT = ".mp4"
    const val IMAGE_FORMAT = ".jpg"
    const val NONE: String = "NONE"
    const val APP_HIDDEN_FOLDER = ".imageVideoApp"
    var VideoTrimmingLimit = 15000 //15*1000
    var mediaLimits = 10
    const val MULTIPLE_MEDIA_LIMIT = 10
    const val IMAGE_VIDEO_CACHE = ".videoCache"
    var MIME_TYPES_IMAGE_VIDEO = arrayOf(
        "video/*",
        "image/*"
    )
    const val ERROR = "ERROR"
    const val SUCCESS = "SUCCESS"
    const val PLAY = "PLAY"
    const val ACTION_CLICK = "ACTION_CLICK"




}
