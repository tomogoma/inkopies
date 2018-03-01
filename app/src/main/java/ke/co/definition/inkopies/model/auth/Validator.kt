package ke.co.definition.inkopies.model.auth

import android.text.TextUtils
import android.util.Patterns
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import javax.inject.Inject

class Validator @Inject constructor(private val pnu: PhoneNumberUtil) : Validatable {

    override fun validateIdentifier(id: String?) = when {
        id == null || id.isEmpty() -> {
            ValidationResult.Invalid()
        }
        isValidEmail(id) -> {
            ValidationResult.ValidOnEmail(true, id)
        }
        else -> {
            try {
                val phoneNumber = pnu.parse(id, "KE")
                if (pnu.isValidNumber(phoneNumber)) {
                    ValidationResult.ValidOnPhone(true, phoneNumber)
                } else {
                    ValidationResult.Invalid()
                }
            } catch (e: NumberParseException) {
                ValidationResult.Invalid()
            }
        }
    }

    override fun isValidPassword(pass: String?) = pass != null && pass.length >= 8

    private fun isValidEmail(target: String) =
            !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}