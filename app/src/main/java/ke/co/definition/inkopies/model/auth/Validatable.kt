package ke.co.definition.inkopies.model.auth

import android.content.Context

interface Validatable {
    fun validateIdentifier(c: Context, id: String?): ValidationResult
    fun isValidPassword(pass: String?): Boolean
}