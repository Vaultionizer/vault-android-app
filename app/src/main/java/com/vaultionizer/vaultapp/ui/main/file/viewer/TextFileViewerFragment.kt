package com.vaultionizer.vaultapp.ui.main.file.viewer

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TextFileViewerFragment : Fragment() {

    val args: TextFileViewerFragmentArgs by navArgs()

    @Inject
    lateinit var decryptionResultCache: DecryptionResultCache

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_text_file_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewer = view.findViewById<TextView>(R.id.viewer_text)
        textViewer.text = String(
            decryptionResultCache.getResultByFileId(args.fileId)
                ?: getString(R.string.text_file_viewer_error).toByteArray(), Charsets.UTF_8
        )
        textViewer.movementMethod = ScrollingMovementMethod()

        decryptionResultCache.invalidateResultByFileId(args.fileId)
    }

}