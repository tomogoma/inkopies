package ke.co.definition.inkopies.repos.ms.image

import android.net.Uri
import com.google.gson.annotations.SerializedName
import ke.co.definition.inkopies.BuildConfig
import ke.co.definition.inkopies.repos.ms.bearerToken
import ke.co.definition.inkopies.utils.injection.ImageModule
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.*
import rx.Single
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by tomogoma
 * On 19/03/18.
 */
class RetrofitImageClient @Inject constructor(@Named(ImageModule.MS) private val retrofit: Retrofit) : ImageClient {

    private val imageMSAPI by lazy { retrofit.create(ImageMSAPI::class.java) }

    override fun uploadProfilePic(token: String, folder: String, uri: Uri): Single<String> {


        val file = File(uri.path)
        val reqFile = RequestBody.create(MediaType.parse("image/*"), file)
        val reqFilePart = MultipartBody.Part.createFormData("image", file.name, reqFile)

        val reqFolder = RequestBody.create(MediaType.parse("text/plain"), folder)

        return imageMSAPI.uploadImage(bearerToken(token), reqFolder, reqFilePart)
                .flatMap { Single.just(it.url) }
    }
}

interface ImageMSAPI {

    @Multipart
    @PUT("upload")
    @Headers("x-api-key: ${BuildConfig.IMAGE_MS_API_KEY}")
    fun uploadImage(
            @Header("Authorization") bearerToken: String,
            @Part("folder") folder: RequestBody,
            @Part image: MultipartBody.Part
    ): Single<UploadImageResult>
}

data class UploadImageRequest(
        val folder: String,
        val image: String
)

data class UploadImageResult(
        val time: Date,
        @SerializedName("URL") val url: String
)