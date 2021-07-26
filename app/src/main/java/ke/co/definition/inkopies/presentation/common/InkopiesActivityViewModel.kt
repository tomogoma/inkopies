package ke.co.definition.inkopies.presentation.common

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 31/03/18.
 */
class InkopiesActivityViewModel @Inject constructor(
        private val auth: Authable,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val toastData = SingleLiveEvent<Pair<String, Int>>()
    val loggedInStatus = SingleLiveEvent<Boolean>()

    private val rxDisposables = CompositeDisposable()
    private var loggedInStatusObserverPos = -1L

    fun start() {
        loggedInStatusObserverPos = auth.registerLoggedInStatusObserver {
            loggedInStatus.postValue(it)
        }
    }

    fun onLogout(onSubscribeFunc: () -> Unit, onUnsubscribeFunc: () -> Unit) {
        rxDisposables.add(auth.logOut()
                .doOnSubscribe { onSubscribeFunc() }
                .doOnTerminate { onUnsubscribeFunc() }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ /*no-op*/ }, this::onError))
    }

    override fun onCleared() {
        auth.unRegisterLoggedInStatusObserver(loggedInStatusObserverPos)
        rxDisposables.clear()
        super.onCleared()
    }

    private fun onError(err: Throwable) {
        toastData.value = Pair(err.message!!, Toast.LENGTH_LONG)
    }

    class Factory @Inject constructor(
            private val auth: Authable,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return InkopiesActivityViewModel(auth, subscribeOnScheduler, observeOnScheduler) as T
        }
    }
}