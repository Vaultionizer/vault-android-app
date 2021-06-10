package com.vaultionizer.vaultapp.ui.main.file.viewer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImageFileViewerFragment : Fragment() {

    private val args: ImageFileViewerFragmentArgs by navArgs()

    @Inject
    lateinit var decryptionResultCache: DecryptionResultCache

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_file_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = decryptionResultCache.getResultByFileId(args.fileArgs.fileId)
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)
        val imageView = view.findViewById<ImageView>(R.id.viewer_image)
        imageView.setImageBitmap(bitmap)

        decryptionResultCache.invalidateResultByFileId(args.fileArgs.fileId)
    }

}