package com.vaultionizer.vaultapp.ui.main.space

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.ui.main.file.FileAlertDialogType
import com.vaultionizer.vaultapp.ui.main.file.showDialog
import com.vaultionizer.vaultapp.util.qr.CRC32Handler
import com.vaultionizer.vaultapp.util.qr.QRCodeGen
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class AuthKeyFragment : Fragment() {
    private val args: AuthKeyFragmentArgs by navArgs()
    private var payload: String = ""
    private var bitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (args.authKey != null && args.remoteSpaceId >= 0L && args.symmetricKey != null) {
            showDialog(FileAlertDialogType.SHOW_AUTH_KEY, { showQRCode() }, { returnToSettings() })
        } else returnToSettings()
    }

    private fun returnToSettings() {
        findNavController().navigate(AuthKeyFragmentDirections.actionAuthKeyFragmentToSpacePermissionsFragment())
    }

    private fun showQRCode() {
        // create QR code
        val payloadCRC = CRC32Handler.createQRPayload(
            args.authKey!!, args.remoteSpaceId,
            args.symmetricKey!!
        )
        payload = CRC32Handler.parsePayload(payloadCRC).toString()
        Log.e("Vault", "QR Code Payload: $payloadCRC")

        bitmap = QRCodeGen.generateQRCode(payloadCRC)
        if (bitmap != null && view != null) {
            view?.findViewById<ImageView>(R.id.qr_code_image_view)?.setImageBitmap(bitmap)
        } else returnToSettings()

        val copyBtn = view?.findViewById<Button>(R.id.copy_auth_key_btn)
        val saveBtn = view?.findViewById<Button>(R.id.save_auth_key_btn)

        copyBtn?.setOnClickListener {
            showDialog(FileAlertDialogType.COPY_AUTH_KEY, { copyAuthKey() }, { cancelToast() })
        }

        saveBtn?.setOnClickListener {
            showDialog(
                FileAlertDialogType.SAVE_AUTH_KEY,
                { saveAuthKey(args.remoteSpaceId) },
                { cancelToast() })
        }
    }

    private fun copyAuthKey() {
        val clipboardManager: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AuthKey", payload)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copied_auth_key_success, Toast.LENGTH_LONG).show()
    }

    private fun saveAuthKey(remoteSpaceId: Long) {
        if (bitmap == null || remoteSpaceId < 0) return failedSaveAuthKey("bitmap or remoteSpaceId")
        var filename: String = requireContext().getExternalFilesDir(null)?.absolutePath
            ?: return failedSaveAuthKey("externalFilesDir")
        filename += "/vaultionizer/qrcode_space$remoteSpaceId.png"
        val file = File(filename)
        if (!file.exists()) {
            try {
                file.parentFile?.mkdirs()
                file.createNewFile()
            } catch (e: Exception) {
                return failedSaveAuthKey("Mkdirs")
            }
        }
        try {
            FileOutputStream(file, false).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return failedSaveAuthKey("OutStream")
        }
        Toast.makeText(context, R.string.success_saving_auth_key, Toast.LENGTH_SHORT).show()
    }

    private fun failedSaveAuthKey(t: String? = null) {
        Toast.makeText(context, R.string.failed_saving_auth_key, Toast.LENGTH_SHORT).show()
        if (t != null) Log.e("Vault", t)
    }

    private fun cancelToast() {
        Toast.makeText(context, R.string.wise_decision_not_copy_save, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = AuthKeyFragment()
    }
}