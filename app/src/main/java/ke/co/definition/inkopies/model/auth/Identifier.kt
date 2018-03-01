package ke.co.definition.inkopies.model.auth

import io.michaelrocks.libphonenumber.android.Phonenumber

/**
 * Created by tomogoma
 * On 01/03/18.
 */

const val ID_TYPE_PHONE = "phones"
const val ID_TYPE_EMAIL = "emails"

sealed class Identifier {

    abstract fun value(): String
    abstract fun type(): String

    class Phone(private val phone: Phonenumber.PhoneNumber) : Identifier() {
        override fun type() = ID_TYPE_PHONE
        override fun value() = phone.countryCode.toString() + phone.nationalNumber.toString()
    }

    class Email(private val email: String) : Identifier() {
        override fun type() = ID_TYPE_EMAIL
        override fun value() = email
    }
}