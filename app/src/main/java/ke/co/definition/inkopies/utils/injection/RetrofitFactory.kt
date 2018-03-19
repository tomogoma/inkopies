package ke.co.definition.inkopies.utils.injection

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by tomogoma
 * On 19/03/18.
 */
fun retrofitFactory(baseURL: String): Retrofit {
    val gson = GsonBuilder()
            .setLenient()
            .create()
    return Retrofit.Builder()
            .baseUrl(baseURL)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}