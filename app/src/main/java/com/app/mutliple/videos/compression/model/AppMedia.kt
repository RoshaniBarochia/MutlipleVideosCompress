package com.app.mutliple.videos.compression.model

 open class AppMedia(
     var id : Int = -1,
     var original_path : String = "",
     var absolute_path : String = "",
     var start : Long = 0,
     var end : Long = 0,
     var duration : Long = 0,
     var isComPressed: Boolean=false,
     var isVideo : Boolean =false,
     var thumb : String = "",
     var time : Int = -1,
     var pathUrl : String = "",
     var isEditVoice : Boolean = false
 )