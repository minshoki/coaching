package com.coaching.coaching

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.os.Message
import android.os.Parcelable
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CustomWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    /**
     * File upload callback for platform versions prior to Android 5.0
     */
    protected var mFileUploadCallbackFirst: ValueCallback<Uri>? = null

    /**
     * File upload callback for Android 5.0+
     */
    protected var mFileUploadCallbackSecond: ValueCallback<Array<Uri>>? = null
    private val mRequestCodeFilePicker: Int = 51426
    var cameraUri: Uri? = null

    var tempFilePathCallback: ValueCallback<Array<Uri>>? = null
    var tempAllowMultiple: Boolean = false
    init {
        webChromeClient = object: WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val allowMultiple = fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE
                tempFilePathCallback = filePathCallback
                tempAllowMultiple = allowMultiple
                requestPermission(null, filePathCallback, allowMultiple)
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                Log.e("shokitest", "onCreateWindow")
                return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
            }
        }
    }


    fun onRequestPermissionResult() {
        openFileInput(null, tempFilePathCallback, tempAllowMultiple)
        tempFilePathCallback = null
        tempAllowMultiple = false
    }

    private fun requestPermission(
        fileUploadCallbackFirst: ValueCallback<Uri>?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        allowMultiple: Boolean
    ) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED -> {
                if(context is MainActivity) {
                    (context as MainActivity).requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 100
                    )
                }
                //request permission
                context
            }

            ActivityCompat.shouldShowRequestPermissionRationale(context as MainActivity, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) -> {
                if(context is MainActivity) {
                    (context as MainActivity).requestPermissions(
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 100
                    )
                }
            }

            else -> {
                openFileInput(null, filePathCallback, allowMultiple)
            }
        }
    }


    private fun openFileInput(
        fileUploadCallbackFirst: ValueCallback<Uri>?,
        fileUploadCallbackSecond: ValueCallback<Array<Uri>>?,
        allowMultiple: Boolean
    ) {
        if (mFileUploadCallbackFirst != null) {
            mFileUploadCallbackFirst?.onReceiveValue(null)
        }
        mFileUploadCallbackFirst = fileUploadCallbackFirst
        if (mFileUploadCallbackSecond != null) {
            mFileUploadCallbackSecond?.onReceiveValue(null)
        }
        mFileUploadCallbackSecond = fileUploadCallbackSecond
        val captureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        try {
            cameraUri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                createImageFile()
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        //i.setType("image/*");
        i.putExtra(
            Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent)
        )
        //i.addCategory(Intent.CATEGORY_BROWSABLE);
        if (allowMultiple) {
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        i.type = "*/*"
        val chooserIntent = Intent.createChooser(i, "Image Chooser")

        // Set camera intent to file chooser
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent)
        )
        if(context is MainActivity) {
            (context as MainActivity).startActivityForResult(chooserIntent, mRequestCodeFilePicker)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == mRequestCodeFilePicker) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent != null) {
                    if (mFileUploadCallbackFirst != null) {
                        val pathOfFile = Utils.getPath(context, intent.data)
                        if (pathOfFile == null || pathOfFile.isEmpty()) {
                            mFileUploadCallbackFirst?.onReceiveValue(null)
                            mFileUploadCallbackFirst = null
                            return
                        }
                        val uri = Uri.parse("file:///$pathOfFile")
                        mFileUploadCallbackFirst?.onReceiveValue(uri)
                        mFileUploadCallbackFirst = null
                    } else if (mFileUploadCallbackSecond != null) {
                        var dataUris: Array<Uri>? = null
                        try {
                            if (intent.dataString != null) {
                                dataUris = arrayOf(Uri.parse(intent.dataString))
                            } else {
                                if (intent.clipData != null) {
                                    val numSelectedFiles = intent.clipData!!.itemCount
                                    for (i in 0 until numSelectedFiles) {
                                        dataUris?.set(i, intent.clipData!!.getItemAt(i).uri)
                                    }
                                } else {
                                    if (cameraUri != null) {
                                        if (mFileUploadCallbackFirst != null) {
                                            mFileUploadCallbackFirst?.onReceiveValue(cameraUri)
                                            mFileUploadCallbackFirst = null
                                        } else if (mFileUploadCallbackSecond != null) {
                                            try {
                                                dataUris = arrayOf(cameraUri!!)
                                            } catch (ignored: Exception) {
                                            }

//												mFileUploadCallbackSecond.onReceiveValue(dataUris);
//												mFileUploadCallbackSecond = null;
                                        }
                                    }
                                }
                            }
                        } catch (ignored: Exception) {
                        }
                        mFileUploadCallbackSecond?.onReceiveValue(dataUris)
                        mFileUploadCallbackSecond = null
                    }
                } else { //Frome Camera intent
                    if (cameraUri != null) {
                        if (mFileUploadCallbackFirst != null) {
                            mFileUploadCallbackFirst?.onReceiveValue(cameraUri)
                            mFileUploadCallbackFirst = null
                        } else if (mFileUploadCallbackSecond != null) {
                            var dataUris: Array<Uri>? = null
                            try {
                                dataUris = arrayOf(cameraUri!!)
                            } catch (ignored: Exception) {
                            }
                            mFileUploadCallbackSecond?.onReceiveValue(dataUris)
                            mFileUploadCallbackSecond = null
                        }
                    }
                }
            } else {
                if (mFileUploadCallbackFirst != null) {
                    mFileUploadCallbackFirst?.onReceiveValue(null)
                    mFileUploadCallbackFirst = null
                } else if (mFileUploadCallbackSecond != null) {
                    mFileUploadCallbackSecond?.onReceiveValue(null)
                    mFileUploadCallbackSecond = null
                }
            }
        }
    }
}