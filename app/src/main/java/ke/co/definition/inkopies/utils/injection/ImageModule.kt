package ke.co.definition.inkopies.utils.injection

import dagger.Module
import dagger.Provides
import ke.co.definition.inkopies.repos.ms.IMAGE_MS_ADDRESS
import ke.co.definition.inkopies.repos.ms.image.ImageClient
import ke.co.definition.inkopies.repos.ms.image.RetrofitImageClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 19/03/18.
 */
@Module
class ImageModule {

    @Provides
    @Named(MS)
    fun provideRetrofit(): Retrofit = retrofitFactory(IMAGE_MS_ADDRESS)

    @Provides
    @Inject
    fun provideImageClient(@Named(MS) rf: Retrofit): ImageClient = RetrofitImageClient(rf)

    companion object {
        const val MS = "imagems"
    }
}