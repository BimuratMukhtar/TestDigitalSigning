package com.zerotoonelabs.testgostcertificate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE)
        ti_edit_talon_file_path_file_path.setOnClickListener {
            onSelectFile()
        }
        ti_edit_talon_file_path_file_path.setText(getSavedFilePath())
    }

    fun authorize(view: View) {
        try {
            signEcp()
        } catch (e: Exception) {
            if (e is TranslatableException) {
                showText(e.getTranslableMessage(this))
            } else {
                e.printStackTrace()
                showText(e.localizedMessage)
            }
        }
    }

    fun signEcp() {
        val filepath = ti_edit_talon_file_path_file_path.text.toString()
        Log.d("tag", filepath)

        if (filepath.isEmpty())
            throw SomeTransletableException(R.string.error_empty_path)
        val fileUri = filepath.toUri()
        checkCertificateFileType(fileUri, getMimeType(fileUri))

//        val signedXml = signer.signXmlFile(xmlTestFile!!.readBytes())
        val inputStream = assets.open("chk_ru_123.xml")
        val size = inputStream.available()
        val buffer = ByteArray(size) //declare the size of the byte array with size of the file
        inputStream.read(buffer) //read file
        inputStream.close() //close file

// Store text file data in the string variable
        val str_data = String(buffer)

        RequestSigner.destroyInstance()
        val signer = RequestSigner(readContentFromUri(fileUri)!!, "Qwerty12")
        val signedXml = signer.signXmlFile(buffer)
        showText(signedXml!!)
    }

    @Throws(FileNotFoundException::class)
    private fun readContentFromUri(uri: Uri): InputStream? {
        return contentResolver?.openInputStream(uri)
    }

    private fun showText(text: String) {
//        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show()
        textView.text = text
    }

    private fun handleActivityResult(fileUri: Uri?) {
        checkCertificateFileType(fileUri, getMimeType(fileUri))
        saveFilePath(fileUri.toString())
        ti_edit_talon_file_path_file_path.setText(fileUri.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            try {
                val fileUri = data?.data ?: throw FileNotFoundException()
                contentResolver?.takePersistableUriPermission(
                    fileUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                handleActivityResult(fileUri)
            } catch (e: RuntimeException) {
                handleRuntimeException(e)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @AfterPermissionGranted(RC_STORAGE_PERM)
    private fun onSelectFile() {
        if (hasReadWriteStoragePermissions() && isExternalStorageWritable()) {
            openFileManager()
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.login_perm_rationable_external_storage),
                RC_STORAGE_PERM, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private fun openFileManager() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            intent.type = P12_MIME_TYPE
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(intent, RC_SELECT_FILE)
        } catch (e: SecurityException) {
            showText(getString(R.string.login_perm_rationable_external_storage))
        }
    }

    private fun hasReadWriteStoragePermissions(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun handleRuntimeException(e: Exception) {
        when (e) {
            is EmptyUriException -> {
                e.printStackTrace()
                showText(e.getTranslableMessage(this))
            }
            is IncorrectCeritificateFileException -> {
                showText(e.getTranslableMessage(this))
            }
            is FileNotFoundException -> {
                showText(getString(R.string.login_error_not_file_found))
            }
            is IncorrectPasswordException -> {
                showText(e.getTranslableMessage(this))
            }
            is BinNotFoundException -> {
                showText(e.getTranslableMessage(this))
            }
            is IOException -> {
                showText(getString(R.string.login_error_corrupted_certificate_file_or_password))
            }
            is StringIndexOutOfBoundsException -> {
                showText(getString(R.string.login_error_bin_not_found))
            }
            else -> {
                throw e
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    private fun getMimeType(fileUri: Uri?): String? {
        return if (fileUri == null) null else contentResolver?.getType(fileUri)
    }

    private fun getSavedFilePath(): String{
        return sharedPreferences.getString(KEY_PATH, "")!!
    }

    private fun saveFilePath(filePath: String){
        sharedPreferences.edit().putString(KEY_PATH, filePath).commit()
    }

    companion object {
        private const val RC_STORAGE_PERM = 4
        private const val RC_SELECT_FILE = 3
        private const val KEY_PATH = "path"
    }
}
