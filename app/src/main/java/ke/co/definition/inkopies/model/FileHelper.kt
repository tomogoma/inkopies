package ke.co.definition.inkopies.model

import android.annotation.SuppressLint
import android.app.Application
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


/**
 * Created by tomogoma
 * On 10/03/18.
 */
interface FileHelper {
    fun createFile(): File
}

class FileHelperImpl @Inject constructor(val app: Application) : FileHelper {

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    override fun createFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
}