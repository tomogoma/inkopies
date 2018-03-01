package ke.co.definition.inkopies.views.verification

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import android.support.design.widget.Snackbar
import ke.co.definition.inkopies.model.auth.Authable
import ke.co.definition.inkopies.model.auth.Validatable
import ke.co.definition.inkopies.model.auth.VerifLogin
import ke.co.definition.inkopies.utils.injection.Dagger2Module
import ke.co.definition.inkopies.utils.livedata.SingleLiveEvent
import ke.co.definition.inkopies.views.common.ProgressData
import ke.co.definition.inkopies.views.common.SnackBarData
import ke.co.definition.inkopies.views.common.TextSnackBarData
import rx.Scheduler
import javax.inject.Inject
import javax.inject.Named

/**
 * Created by tomogoma
 * On 02/03/18.
 */
class VerificationViewModel @Inject constructor(
        private val auth: Authable,
        private val validator: Validatable,
        @Named(Dagger2Module.SCHEDULER_IO) val subscribeOnScheduler: Scheduler,
        @Named(Dagger2Module.SCHEDULER_MAIN_THREAD) val observeOnScheduler: Scheduler
) : ViewModel() {

    val openEditDialog: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val verifiedEv: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val finishedEv: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val snackBarData: SingleLiveEvent<SnackBarData> = SingleLiveEvent()

    val progress: ObservableField<ProgressData> = ObservableField()
    val verifLogin: ObservableField<VerifLogin> = ObservableField()
    val otp: ObservableField<String> = ObservableField()

    fun start(vl: VerifLogin) {

        if (vl.verified) {
            verifiedEv.value = true
            return
        }

        if (vl.otpStatus != null && vl.otpStatus.isExpired()) {
            sendOTP(vl)
            return
        }

        this.verifLogin.set(vl)
    }

    fun verify(vl: VerifLogin) {

        val validRes = validator.validateIdentifier(vl.value)
        if (!validRes.isValid) {
            openEditDialog.value = true
            return
        }

        auth.verifyOTP(vl)
                .doOnUnsubscribe { progress.set(ProgressData(true, "Verifying ${vl.value}")) }
                .doAfterTerminate { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(
                        { finishedEv.value = true },
                        { snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG) }
                )
    }

    fun sendOTP(vl: VerifLogin) {

        val validRes = validator.validateIdentifier(vl.value)
        if (!validRes.isValid) {
            openEditDialog.value = true
            return
        }

        val id = validRes.getIdentifier()
        auth.sendVerifyOTP(id)
                .doOnUnsubscribe { progress.set(ProgressData(true, "Sending verification code")) }
                .doAfterTerminate { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(
                        { start(VerifLogin(vl.id, vl.userID, vl.value, vl.verified, it)) },
                        { snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG) }
                )
    }

    fun checkVerified(vl: VerifLogin) {

        val validRes = validator.validateIdentifier(vl.value)
        if (!validRes.isValid) {
            openEditDialog.value = true
            return
        }

        auth.identifierVerified(vl)
                .doOnUnsubscribe { progress.set(ProgressData(true, "Checking ${vl.value} verified")) }
                .doAfterTerminate { progress.set(ProgressData()) }
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .subscribe(
                        { finishedEv.value = true },
                        { snackBarData.value = TextSnackBarData(it.message!!, Snackbar.LENGTH_LONG) }
                )
    }
}