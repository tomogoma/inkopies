package ke.co.definition.inkopies.model.auth

import io.michaelrocks.libphonenumber.android.Phonenumber

sealed class ValidationResult(open val isValid: Boolean) {

    abstract fun getIdentifier(): String

    class Invalid : ValidationResult(false) {
        override fun getIdentifier(): String = ""
    }

    class ValidOnPhone(
            override val isValid: Boolean,
            private val phone: Phonenumber.PhoneNumber
    ) : ValidationResult(isValid) {
        override fun getIdentifier(): String =
                phone.countryCode.toString() + phone.nationalNumber.toString()
    }

    class ValidOnEmail(
            override val isValid: Boolean,
            private val email: String
    ) : ValidationResult(isValid) {
        override fun getIdentifier(): String = email
    }
}