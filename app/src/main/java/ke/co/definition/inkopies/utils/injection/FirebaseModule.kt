package ke.co.definition.inkopies.utils.injection

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by tomogoma
 * On 03/04/18.
 */
@Module
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()
}