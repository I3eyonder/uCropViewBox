package com.hieupt.ucropviewbox.sample

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hieupt.ucropviewbox.UCropUtil
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yalantis.ucrop.callback.BitmapCropCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), MultiplePermissionsListener, BitmapCropCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(this)
            .check()
        initViews()
        initActions()
    }

    private fun initViews() {
        uCropViewBox.apply {
            bindToLifecycle(lifecycle)
            options.withAspectRatio(1f, 1f)
        }
    }

    private fun initActions() {
        btnCropRandom.setOnClickListener {
            val random = Random()
            val minSizePixels = 800
            val maxSizePixels = 2400
            val uri = Uri.parse(
                String.format(
                    Locale.getDefault(), "https://unsplash.it/%d/%d/?random",
                    minSizePixels + random.nextInt(maxSizePixels - minSizePixels),
                    minSizePixels + random.nextInt(maxSizePixels - minSizePixels)
                )
            )
            startCrop(uri)
        }
        btnPickImage.setOnClickListener {
            pickFromGallery()
        }
        btnSaveImage.setOnClickListener {
            uCropViewBox.cropAndSaveImage(callback = this)
        }
        btnRatio11.setOnClickListener {
            uCropViewBox.setTargetAspectRatio(1f)
        }
        btnRatio169.setOnClickListener {
            uCropViewBox.setTargetAspectRatio(16f, 9f)
        }
        btnRatio32.setOnClickListener {
            uCropViewBox.setTargetAspectRatio(3f, 2f)
        }
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("image/*")
            .addCategory(Intent.CATEGORY_OPENABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select image"
            ), REQUEST_PICK_IMAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    startCrop(selectedUri)
                } else {
                    Toast.makeText(
                        this,
                        "Cannot retrieve selected image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startCrop(uri: Uri) {
        uCropViewBox.setImageUri(uri)
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

    }

    override fun onPermissionRationaleShouldBeShown(
        permissions: MutableList<PermissionRequest>?,
        token: PermissionToken?
    ) {
    }

    companion object {
        private const val REQUEST_PICK_IMAGE = 199
    }

    override fun onBitmapCropped(
        resultUri: Uri,
        offsetX: Int,
        offsetY: Int,
        imageWidth: Int,
        imageHeight: Int
    ) {
        Toast.makeText(this, resultUri.toString(), Toast.LENGTH_SHORT).show()
        val downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val filename = String.format(
            "%d_%s",
            System.currentTimeMillis(),
            resultUri.lastPathSegment
        )

        val saveFile = File(downloadPath, filename)
        UCropUtil.saveFileTo(this, resultUri, saveFile)
    }

    override fun onCropFailure(t: Throwable) {
    }
}
