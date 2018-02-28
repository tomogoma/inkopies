package ke.co.definition.inkopies.model.auth

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

object Validator : Validatable {

    override fun validateIdentifier(c: Context, id: String?) = when {
        id == null || id.isEmpty() -> {
            ValidationResult.Invalid()
        }
        isValidEmail(id) -> {
            ValidationResult.ValidOnEmail(true, id)
        }
        else -> {
            val pnu = PhoneNumberUtil.createInstance(c)
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