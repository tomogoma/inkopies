package ke.co.definition.inkopies.presentation.shopping.checkout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import ke.co.definition.inkopies.R
import ke.co.definition.inkopies.model.ResourceManager
import ke.co.definition.inkopies.model.shopping.CheckoutManager
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackbarData
import ke.co.definition.inkopies.presentation.common.TextSnackbarData
import ke.co.definition.inkopies.presentation.format.DateFormatter
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import rx.Subscription
import java.util.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 28/05/18.
 */
class CheckoutVM @Inject constructor(
        private val resMan: ResourceManager,
        private val model: CheckoutManager,
        private val dateFormatter: DateFormatter,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val storeName = MutableLiveData<String>()
    val storeNameError = MutableLiveData<String>()
    val branchName = MutableLiveData<String>()
    val branchNameError = MutableLiveData<String>()
    val checkoutDate = MutableLiveData<String>()
    val checkoutDateError = MutableLiveData<String>()
    val progressData = MutableLiveData<ProgressData>()

    val snackbarData = SingleLiveEvent<SnackbarData>()
    val onCompleteEvent = SingleLiveEvent<Unit>()

    private lateinit var slID: String
    internal lateinit var date: Date

    internal fun onStart(slID: String) {
        this.slID = slID
        onCheckoutDateSet(Date())
    }

    internal fun onCheckoutDateSet(date: Date) {
        this.date = date
        this.checkoutDate.postValue(dateFormatter.formatDate(date))
    }

    internal fun onSubmit() {
        model.checkout(slID, branchName.value, storeName.value, date)
                .observeOn(observeOnScheduler)
                .doOnSubscribe(this::showCheckoutProgress)
                .doOnUnsubscribe(this::hideProgress)
                .subscribeOn(subscribeOnScheduler)
                .subscribe(this::onCheckoutComplete, this::onError)
    }

    private fun showCheckoutProgress(@Suppress("UNUSED_PARAMETER") subscription: Subscription) {
        progressData.postValue(ProgressData(resMan.getString(R.string.checking_out)))
    }

    private fun hideProgress() {
        progressData.postValue(ProgressData())
    }

    private fun onCheckoutComplete() {
        onCompleteEvent.call()
    }

    private fun onError(e: Throwable) {
        snackbarData.value = TextSnackbarData(e.message ?: return, Snackbar.LENGTH_LONG)
    }

    class Factory @Inject constructor(
            private val resMan: ResourceManager,
            private val model: CheckoutManager,
            private val dateFormatter: DateFormatter,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CheckoutVM(resMan, model, dateFormatter, subscribeOnScheduler, observeOnScheduler) as T
        }
    }
}