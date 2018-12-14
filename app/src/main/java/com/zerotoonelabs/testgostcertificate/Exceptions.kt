package com.zerotoonelabs.testgostcertificate

import java.lang.RuntimeException

class IncorrectPasswordException(errorMessage: String? = null): RuntimeException(errorMessage), TranslatableException{
    override fun getDefaultResourceId(): Int = R.string.login_error_incorrect_password
}

class BinNotFoundException(errorMessage: String? = null): RuntimeException(errorMessage), TranslatableException{
    override fun getDefaultResourceId(): Int = R.string.login_error_bin_not_found
}

class SomeTransletableException(
    private val errorMessageId: Int = -1
) : RuntimeException(), TranslatableException {
    override fun getDefaultResourceId(): Int = if (errorMessageId != -1) errorMessageId else R.string.error_unknown
}

class IncorrectCeritificateFileException(errorMessage: String? = null) : RuntimeException(errorMessage), TranslatableException {
    override fun getDefaultResourceId(): Int = R.string.login_error_incorrect_certificate_file_format
}

class EmptyUriException(errorMessage: String? = null) : RuntimeException(errorMessage), TranslatableException {
    override fun getDefaultResourceId(): Int = R.string.login_error_empty_uri
}

class NoDataTagException(errorMessage: String? = null) : RuntimeException(errorMessage), TranslatableException {
    override fun getDefaultResourceId(): Int = R.string.talon_error_no_data_tag
}

