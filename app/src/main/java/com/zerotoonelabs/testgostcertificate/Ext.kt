package com.zerotoonelabs.testgostcertificate

import android.content.ContentResolver
import android.net.Uri
import java.io.File

const val P12_MIME_TYPE = "application/x-pkcs12"

@Throws(EmptyUriException::class, IncorrectCeritificateFileException::class)
fun checkCertificateFileType(fileUri: Uri?, mimeType: String?){
    if (fileUri == null || mimeType == null) {
        throw EmptyUriException()
    }
    if (fileUri.scheme != ContentResolver.SCHEME_CONTENT || mimeType != P12_MIME_TYPE) {
        throw IncorrectCeritificateFileException()
    }
}



/**
 * Creates a Uri from the given encoded URI string.
 *
 * @see Uri.parse
 */
inline fun String.toUri(): Uri = Uri.parse(this)

/**
 * Creates a Uri from the given file.
 *
 * @see Uri.fromFile
 */
inline fun File.toUri(): Uri = Uri.fromFile(this)

/** Creates a [File] from the given [Uri]. */
fun Uri.toFile(): File {
    require(scheme == "file") { "Uri lacks 'file' scheme: $this" }
    return File(path)
}
