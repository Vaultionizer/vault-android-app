package com.vaultionizer.vaultapp.ui.main.qr_scanner

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.frame.FrameProcessor
import com.otaliastudios.cameraview.size.Size
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.util.qr.CRC32Handler
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class QRCodeScanFragment : Fragment() {
    private val args: QRCodeScanFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_qr_code_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val camera: CameraView = view.findViewById(R.id.camera)
        camera.setLifecycleOwner(viewLifecycleOwner)

        val hintMap: MutableMap<EncodeHintType, ErrorCorrectionLevel> =
            EnumMap(com.google.zxing.EncodeHintType::class.java)
        hintMap[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L

        // we use Camera1 engine
        camera.addFrameProcessor(FrameProcessor { frame ->
            val size: Size = frame.size
            if (frame.dataClass === ByteArray::class.java) {
                val data: ByteArray = frame.getData()
                val binaryBitmap = BinaryBitmap(
                    HybridBinarizer(
                        PlanarYUVLuminanceSource(
                            data,
                            size.width,
                            size.height,
                            0,
                            0,
                            size.width,
                            size.height,
                            false
                        )
                    )
                )
                try {
                    val qrCodeResult: Result = MultiFormatReader().decodeWithState(binaryBitmap)
                    evaluateQRCode(qrCodeResult.text)
                } catch (e: Exception) {
                    return@FrameProcessor
                }

                // Process byte array...
            } else if (frame.dataClass === Image::class.java) {
                throw NotImplementedError("Camera2 engine is not supported as it was experimental at the time this was implemented.")
            }
        })

    }

    private fun evaluateQRCode(content: String){
        if(!CRC32Handler().checkValid(content)) return
        val payload = CRC32Handler().parsePayload(content) ?: return

        if (args.scanType == 0){
            // authentication code parsing
            findNavController().navigate(QRCodeScanFragmentDirections.actionQRCodeScanFragmentToJoinSpaceFragment(payload))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = QRCodeScanFragment()
    }
}