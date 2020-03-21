package com.hieupt.ucropviewbox

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Created by HieuPT on 3/21/2020.
 */
object UCropUtil {

    fun saveFileTo(context: Context, inputFileUri: Uri, outputFile: File) {
        if (inputFileUri.scheme == "file") {
            try {
                inputFileUri.path?.let { filePath ->
                    FileInputStream(File(filePath)).use { inStream ->
                        FileOutputStream(outputFile).use { outStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.

                    // Tell the media scanner about the new file so that it is
                    // immediately available to the user.
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(outputFile.toString()),
                        null,
                        null
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Save image failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}