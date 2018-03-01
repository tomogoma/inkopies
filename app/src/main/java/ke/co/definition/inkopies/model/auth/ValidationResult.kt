package ke.co.definition.inkopies.model.auth

import io.michaelrocks.libphonenumber.android.Phonenumber

sealed class ValidationResult(open val isValid: Boolean) {

    abstract fun getIdentifier(): Identifier

    class Invalid : ValidationResult(false) {
        override fun getIdentifier() = Identifier.Email("")
    }

    class ValidOnPhone(
            override val isValid: Boolean,
            private val phone: Phonenumber.PhoneNumber
    ) : ValidationResult(isValid) {
        override fun getIdentifier() = Identifier.Phone(phone)
    }

    class ValidOnEmail(
            override val isValid: Boolean,
            private val email: String
    ) : ValidationResult(isValid) {
        override fun getIdentifier() = Identifier.Email(email)
    }
}