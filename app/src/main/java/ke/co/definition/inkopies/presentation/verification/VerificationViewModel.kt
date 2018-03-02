package ke.co.definition.inkopies.presentation.verification

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.presentation.common.ProgressData
import ke.co.definition.inkopies.presentation.common.SnackBarData
import ke.co.definition.inkopies.presentation.common.TextSnackBarData
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named


/**
 * Created by tomogoma
 * On 02/03/18.
 */
class VerificationViewModel @Inject constructor(
        private val auth: Authable,
        @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
) : ViewModel() {

    val openEditDialog: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val finishedEv: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val snackBarData: SingleLiveEvent<SnackBarData> = SingleLiveEvent()

    val resetCDTimer: ObservableField<String> = ObservableField()
    val progress: ObservableField<ProgressData> = ObservableField()
    val verifLogin: ObservableField<VerifLogin> = ObservableField()
    val otp: ObservableField<String> = ObservableField()

    private lateinit var vl: VerifLogin

    fun start(vl: VerifLogin) {

        this.vl = vl
        verifLogin.set(vl)

        if (vl.verified) {
            onClaimVerified()
            return
        }

        if (vl.otpStatus != null && vl.otpStatus.isExpired()) {
            onRequestResendOTP()
            return
        }

        countDownResetVisibility()
    }

    fun onSubmit() {

        auth.verifyOTP(vl, otp.get())
                .doOnSubscribe { progress.set(ProgressData(true, "Verifying ${vl.value}")) }
                .doOnUnsubscribe { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({
                    finishedEv.value = true
                }, {
                    snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG)
                })
    }

    fun onRequestResendOTP() {

        auth.sendVerifyOTP(vl)
                .doOnSubscribe { progress.set(ProgressData(true, "Sending verification code")) }
                .doOnUnsubscribe { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({
                    start(VerifLogin(vl.id, vl.userID, vl.value, vl.verified, it))
                }, {
                    snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG)
                })
    }

    fun onClaimVerified() {

        auth.checkIdentifierVerified(vl)
                .doOnSubscribe { progress.set(ProgressData(true, "Checking ${vl.value} verified")) }
                .doOnUnsubscribe { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({
                    finishedEv.value = true
                }, {
                    snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG)
                })
    }

    private fun countDownResetVisibility() {
        auth.resendInterval(vl.otpStatus ?: return, 1)
                .doOnUnsubscribe({ resetCDTimer.set("") })
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe({ resetCDTimer.set(it) })
    }

    class Factory @Inject constructor(
            private val auth: Authable,
            @Named(Dagger2Module.SCHEDULER_IO) private val subscribeOnScheduler: Scheduler,
            @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) private val observeOnScheduler: Scheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return VerificationViewModel(auth, subscribeOnScheduler, observeOnScheduler) as T
        }

    }
}