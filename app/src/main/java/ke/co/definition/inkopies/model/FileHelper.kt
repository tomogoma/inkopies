package ke.co.definition.inkopies.model

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.os.Environment
import ke.co.definition.inkopies.R
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * Created by tomogoma
 * On 10/03/18.
 */
interface FileHelper {
    fun createTempFile(ext: String): File
    fun newExternalPublicFile(name: String, ext: String): File
    fun getInputStream(uri: Uri): InputStream
}

class FileHelperImpl @Inject constructor(val app: Application) : FileHelper {

    @Throws(IOException::class)
    override fun createTempFile(ext: String): File {
        val storageDir = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(genTimestampedFileName("Inkopies_temp_", ext), "", storageDir)
    }

    override fun newExternalPublicFile(name: String, ext: String): File {
        if (!isExternalStorageWritable()) {
            throw ExternalStorageUnavailableException()
        }
        val appName = app.getString(R.string.app_name)
        val dir = File(Environment.getExternalStoragePublicDirectory(appName).absolutePath)
        dir.mkdirs()
        return File(dir, genTimestampedFileName(name, ext))
    }

    override fun getInputStream(uri: Uri): InputStream {
        return app.contentResolver.openInputStream(uri)!!
    }

    private fun genTimestampedFileName(name: String, ext: String): String {
        @SuppressLint("SimpleDateFormat")
        val date = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return "${name}_$date.$ext"
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}

class ExternalStorageUnavailableException : Exception("external storage unavailable")