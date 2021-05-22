package com.vaultionizer.vaultapp.cryptography

import com.vaultionizer.vaultapp.R
import java.util.regex.Pattern


enum class PasswordValidationResult(val text: Int? = null, val error: Boolean = true) {
    VALID(null, false),
    TOO_SHORT(R.string.pwd_validator_too_short),
    TOO_FEW_DIGITS(R.string.pwd_validator_too_few_digits),
    TOO_FEW_UPPERCASE(R.string.pwd_validator_too_few_uppercase),
    TOO_FEW_SPECIAL_CHARS(R.string.pwd_validator_too_few_special),
    WHITESPACES_INCLUDED(R.string.pwd_validator_no_whitespaces)
}

class PasswordValidator {
    private val lengthPattern: Pattern =
        Pattern.compile(".{10,}")                      // at least 10 characters
    private val digitPattern: Pattern =
        Pattern.compile("(.*[0-9]){3,}.*")              // at least 3 decimal digits
    private val uppercasePattern: Pattern =
        Pattern.compile("^(.*[A-Z]){2,}.*")         // at least 2 uppercase letters
    private val whitespacePattern: Pattern =
        Pattern.compile("^(?=\\S+\$).*")           // no whitespaces
    private val specialCharPattern: Pattern =
        Pattern.compile("(.*[@#\$%^&+=]){3,}.*")  // at least 3 special chars

    fun validatePassword(pwd: String): PasswordValidationResult {
        if (!lengthPattern.matcher(pwd).matches()) return PasswordValidationResult.TOO_SHORT
        if (!digitPattern.matcher(pwd).matches()) return PasswordValidationResult.TOO_FEW_DIGITS
        if (!uppercasePattern.matcher(pwd)
                .matches()
        ) return PasswordValidationResult.TOO_FEW_UPPERCASE
        if (!specialCharPattern.matcher(pwd)
                .matches()
        ) return PasswordValidationResult.TOO_FEW_SPECIAL_CHARS
        if (!whitespacePattern.matcher(pwd)
                .matches()
        ) return PasswordValidationResult.WHITESPACES_INCLUDED
        return PasswordValidationResult.VALID
    }

}