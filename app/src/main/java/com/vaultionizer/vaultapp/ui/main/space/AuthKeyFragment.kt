package com.vaultionizer.vaultapp.ui.main.space

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.util.qr.CRC32Handler
import com.vaultionizer.vaultapp.util.qr.QRCodeGen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthKeyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bitmap = QRCodeGen().generateQRCode(CRC32Handler().generateCRC32("Hallo Welt"))
        Log.e("Vault", "QR Code")
        if (bitmap != null) {
            view.findViewById<ImageView>(R.id.qr_code_image_view).setImageBitmap(bitmap)
        }
    }

    companion object {
        fun newInstance() = AuthKeyFragment()
    }
}