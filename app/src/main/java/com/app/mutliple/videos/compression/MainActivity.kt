package com.app.mutliple.videos.compression

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mutliple.videos.compression.adapter.MediaThumbAdapter
import com.app.mutliple.videos.compression.model.AppMedia
import com.app.mutliple.videos.compression.utils.Constants
import com.app.mutliple.videos.compression.utils.FileUtils
import com.app.mutliple.videos.compression.utils.ImageUtility
import com.app.mutliple.videos.compression.utils.Utility
import com.app.mutliple.videos.compression.utils.alertDialogMediaInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.lang.reflect.Type
import java.net.URLConnection
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        outputDirectory = getOutputDirectory(this)
        findViewById<Button>(R.id.btnImage).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                val arr=arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                var count = 0
                for(i in arr){
                    if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, i))
                        count++
                }
                if(count >= 3){
                    clickToAddMedia()
                }else
                    requestPermission()
            }else requestPermission()
        }
    }
    //ask for permission
    private fun requestPermission() {
        val array=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
            listOf(Manifest.permission.CAMERA,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            listOf(Manifest.permission.CAMERA,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.READ_MEDIA_IMAGES)
        } else{
            listOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        Dexter.withContext(this)
            .withPermissions(
                array
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        clickToAddMedia()
                    }else if (report.grantedPermissionResponses.size == 3 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                        clickToAddMedia()
                    }
                    else if (report.isAnyPermissionPermanentlyDenied) {
                        // check for permanent denial of any permission
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { error ->
                Toast.makeText(this,
                    "Error occurred! $error",Toast.LENGTH_LONG
                ).show()
            }
            .onSameThread()
            .check()
    }

    //open setting dialog
    fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS"
        ) { dialog, which ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.cancel() }
        builder.show()

    }

    //open setting screen
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        resultLauncherSetting.launch(intent)
    }

    private var resultLauncherSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ -> }

    private var mediaArrayList: ArrayList<AppMedia> = ArrayList()
    private var maxMediaSelectionLimitMessage =
        "Can't share more than ${Constants.MULTIPLE_MEDIA_LIMIT}  media items"
    private var tempListMedia: ArrayList<AppMedia> = ArrayList()
    var mediaData = ""
    private lateinit var outputDirectory: File
    private fun clickToAddMedia() {
        Log.d(
            "permissionObserverLvDt",
            "permissionChecking clickToAddMedia - calledFrom="
        )
        if (mediaArrayList.size < Constants.MULTIPLE_MEDIA_LIMIT) {
            val dialog =
                Utility.showDialogWithMedia(
                    this, object : alertDialogMediaInterface {
                        override fun onCapturePhotoClick() {
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            val file = File(
                                outputDirectory,
                                System.currentTimeMillis().toString() + Constants.IMAGE_FORMAT
                            )
                            mediaData = file.absolutePath
                            val fileUri = FileProvider.getUriForFile(
                                this@MainActivity,
                                BuildConfig.APPLICATION_ID + ".fileprovider",
                                file
                            )
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                            startResultForCamera.launch(cameraIntent)
                        }

                        override fun onCaptureVideoClick() {
                             val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                                val file = File(
                                    outputDirectory,
                                    System.currentTimeMillis().toString() + Constants.VIDEO_FORMAT
                                )
                                mediaData = file.absolutePath
                                val fileUri = FileProvider.getUriForFile(
                                    this@MainActivity,
                                    BuildConfig.APPLICATION_ID + ".fileprovider",
                                    file
                                )
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                                startResultForCamera.launch(cameraIntent)

                        }

                        override fun onGalleryClick() {
                                getMediaFromGallery()
                        }

                    }, isCancelable = true)
            dialog.setOnCancelListener {
            }
        } else {
            Utility.msgDialog(this, maxMediaSelectionLimitMessage)
        }
    }

    private fun getMediaFromGallery() {
        try {
            val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE)
            pickIntent.type = "*/*"
            pickIntent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                Constants.MIME_TYPES_IMAGE_VIDEO
            )
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startResultForGallery.launch(pickIntent)
        } catch (ex: Exception) {
            Log.e( "getMediaFromGallery", ex.localizedMessage as String)
        }
    }

    private val startResultForCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && mediaData.isNotEmpty()) {
                try {
                    // image and video from camera
                    val list: ArrayList<AppMedia> = ArrayList()
                    if ((mediaArrayList.size + list.size) < Constants.MULTIPLE_MEDIA_LIMIT) {
                        if (!Utility.isEmpty(mediaData) && isImageFile(mediaData)) {
                            Log.e(
                                "TAG_ADD_MEDIA",
                                "CAMERA_REQUEST Image"
                            )
                            val imagePath = mediaData
                            val compressImageFile =
                                ImageUtility.compressImage(
                                    this,
                                    outputDirectory, imagePath
                                )
                            mediaData = compressImageFile.absolutePath
                            list.add(AppMedia(-1, mediaData, mediaData, 0L, 0L, 0L))
                        } else {
                            Log.e(
                                "TAG_ADD_MEDIA",
                                "CAMERA_REQUEST Video"
                            )
                            val duration = Utility.getDuration(
                                File(mediaData),
                                this
                            )
                            list.add(
                                AppMedia(
                                    -1,
                                    mediaData,
                                    mediaData,
                                    0L,
                                    duration,
                                    duration,
                                    isVideo = true
                                )
                            )
                        }
                        isMediaSelected = true
                        startResultForMediaShow.launch(
                            Intent(
                                this,
                                MediaPagerTrimmingActivity::class.java
                            ).putExtra("extra_list", Gson().toJson(list))
                                .putExtra("media_list", Gson().toJson(mediaArrayList))
                        )

                    } else {
                        Utility.msgDialog(this, maxMediaSelectionLimitMessage)
                    }
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                mediaData = ""
            }
        }

    private val startResultForGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK &&
                result.data != null) {
                try {
                    // image and video from gallery
                    val list: ArrayList<AppMedia> = ArrayList()
                    if (mediaArrayList.size < Constants.MULTIPLE_MEDIA_LIMIT) {
                        val data = result.data
                        if (data?.clipData != null) {
                            val countData: ClipData? = data.clipData
                            for (i in 0 until countData?.itemCount!!) {
                                if ((mediaArrayList.size + list.size) >= Constants.MULTIPLE_MEDIA_LIMIT) {
                                    Toast.makeText(
                                        this,
                                        maxMediaSelectionLimitMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    break
                                }
                                if (!Utility.isEmpty("${data.clipData?.getItemAt(i)?.uri!!}")) {
                                    val imageUrl: Uri = data.clipData?.getItemAt(i)?.uri!!
                                    val path =
                                        FileUtils.getRealPath(this, imageUrl)
                                    if (!Utility.isEmpty(path) && isImageFile(path)) {
                                        val compressImageFile =
                                            ImageUtility.compressImage(
                                                this,
                                                outputDirectory,
                                                path
                                            )
                                        list.add(
                                            AppMedia(
                                                -1,
                                                compressImageFile.absolutePath,
                                                compressImageFile.absolutePath,
                                                0L,
                                                0L,
                                                0L
                                            )
                                        )
                                    } else {
                                        val duration =
                                            Utility.getDuration(
                                                File(path),
                                                this
                                            )
                                        list.add(
                                            AppMedia(
                                                -1,
                                                path,
                                                path,
                                                0L,
                                                duration,
                                                duration,
                                                isVideo = true
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            if (isImageFile(
                                    FileUtils.getRealPath(
                                        this,
                                        Uri.parse(data?.data.toString())
                                    )
                                )
                            ) {
                                Log.e("TAG_ADD_MEDIA", "GALLERY_REQUEST Image")
                                if (!Utility.isEmpty(data?.data?.toString())) {
                                    val imagePath =
                                        FileUtils.getRealPath(
                                            this,
                                            Uri.parse(data?.data?.toString())
                                        )
                                    val compressImageFile =
                                        ImageUtility.compressImage(
                                            this,
                                            outputDirectory,
                                            imagePath
                                        )
                                    list.add(
                                        AppMedia(
                                            -1,
                                            compressImageFile.absolutePath,
                                            compressImageFile.absolutePath,
                                            0L,
                                            0L,
                                            0L
                                        )
                                    )
                                }

                            } else {
                                Log.e(
                                    "TAG_ADD_MEDIA",
                                    "GALLERY_REQUEST Video"
                                )
                                val path =
                                    FileUtils.getRealPath(
                                        this,
                                        Uri.parse(data?.data?.toString())
                                    )
                                val duration =
                                    Utility.getDuration(File(path), this)
                                list.add(
                                    AppMedia(
                                        -1,
                                        path,
                                        path,
                                        0L,
                                        duration,
                                        duration,
                                        isVideo = true
                                    )
                                )
                            }
                        }
                        val intentLunch =
                            Intent(this, MediaPagerTrimmingActivity::class.java)
                                .putExtra("extra_list", Gson().toJson(list))
                                .putExtra("media_list", Gson().toJson(mediaArrayList))
                        isMediaSelected = true
                        startResultForMediaShow.launch(intentLunch)
//                setMediaThumb()
                    } else {
                        Utility.msgDialog(this, maxMediaSelectionLimitMessage)
                    }
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                mediaData = ""
            }
        }

    private var startResultForMediaShow =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Log.d(
                "startResultForMediaShow",
                "addMediaEnable -result.resultCode=${result.resultCode} - tempArrayUri.size=${tempListMedia.size}"
            )
            if (result.resultCode == Activity.RESULT_OK &&
                result.data != null
            ) {
                try {

                    if (result.data?.hasExtra("isDelete") == true) {
                        Log.d(
                            "startResultForMediaShow",
                            "result.data?.hasExtra(isDelete) == true"
                        )
                        if (result.data?.getBooleanExtra("isDelete", false) == true) {
                            Log.d(
                                "startResultForMediaShow",
                                "tempArrayUri.clear() - mediaArrayList.clear() - CALLED"
                            )
                            tempListMedia.clear()
                            //mediaArrayList.clear()
                        }
                    }
                    if (result.data?.hasExtra("media_list") == true) {
                        val type: Type = object : TypeToken<List<AppMedia>>() {}.type
                        Log.d(
                            "startResultForMediaShow",
                            "result.data?.getStringExtra(media_list)=>${
                                result.data?.getStringExtra(
                                    "media_list"
                                )
                            }"
                        )
                        tempListMedia.addAll(
                            Gson().fromJson(
                                result.data?.getStringExtra("media_list"),
                                type
                            )
                        )
                        Log.d(
                            "startResultForMediaShow",
                            "multiMediaList Add media Caption @0 mediaArrayList.size: ${mediaArrayList.size} - tempArrayUri.size: ${tempListMedia.size} - isMediaSelected: $isMediaSelected"
                        )
                        if (isMediaSelected && (mediaArrayList.size != tempListMedia.size)) {
                            Log.d(
                                "startResultForMediaShow",
                                "mediaArrayList.clear() -added All data CALLED ^^^^^^^^^^^^"
                            )
                            mediaArrayList.clear()
                            mediaArrayList.addAll(tempListMedia)
                        } else {
                            Log.d(
                                "startResultForMediaShow",
                                "Gone for set time for loop CALLED ^^^^^^^^^^^^"
                            )
                            for (pnt in 0 until tempListMedia.size) {
                                for (cnt in 0 until mediaArrayList.size) {
                                    if (mediaArrayList[cnt].absolute_path == tempListMedia[pnt].absolute_path)
                                        mediaArrayList[cnt].time = tempListMedia[pnt].time
                                }
                            }
                        }

                        isMediaSelected = false
                        Log.d(
                            "startResultForMediaShow",
                            "multiMediaList Add media Caption @1 mediaArrayList.size: ${mediaArrayList.size}"
                        )
                    } else {
                        Log.d("startResultForMediaShow", "NOT SET MEDIA THUMB")
                    }
                    val recyclerView=findViewById<RecyclerView>(R.id.list_media)
                    val mMediaThumbAdapter = MediaThumbAdapter(
                        mediaArrayList,
                        null,
                        null
                    )
                    findViewById<TextView>(R.id.txtTitle).isVisible = true
                    recyclerView.apply {
                        isVisible = true
                        clipToPadding = true
                        layoutManager= LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter = mMediaThumbAdapter
                    }
                } catch (ex: NullPointerException) {
                    ex.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                Log.d(
                    "startResultForMediaShow",
                    "multiMediaList Add media Caption @2 mediaArrayList.size: ${mediaArrayList.size}"
                )
            } else {
                Log.d("startResultForMediaShow", "GO FOR ELSE PART")
            }

        }

    private var isVideoRecorded = false
    private var isImageCapture = false
    private var isMediaSelected = false

    private fun isImageFile(path: String?): Boolean {
        Log.d("isImageFile", "isImageFile: $path")
        val mimeType: String
        return if (!Utility.isEmpty(path)) {
            isImageCapture = true
            isVideoRecorded = false
            mimeType = URLConnection.guessContentTypeFromName(path)
            mimeType != null && mimeType.startsWith("image")
        } else
            false

    }

    /* get output file path*/
    private fun getOutputDirectory(context: Context): File {

        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, Constants.APP_HIDDEN_FOLDER).absoluteFile.apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir.absoluteFile
        } else context.filesDir.absoluteFile
    }

}