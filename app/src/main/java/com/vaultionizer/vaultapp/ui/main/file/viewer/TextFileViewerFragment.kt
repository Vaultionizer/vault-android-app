package com.vaultionizer.vaultapp.ui.main.file.viewer

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.OutlinedGoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.vaultionizer.vaultapp.R
import com.vaultionizer.vaultapp.data.cache.DecryptionResultCache
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TextFileViewerFragment : Fragment() {

    private val args: TextFileViewerFragmentArgs by navArgs()
    private lateinit var editToggleButton: FloatingActionButton
    private lateinit var textEdit: EditText

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

        textEdit = view.findViewById(R.id.viewer_edit)
        editToggleButton = view.findViewById(R.id.button_edit_toggle)

        val text = String(
            decryptionResultCache.getResultByFileId(args.fileArgs.fileId)
                ?: getString(R.string.text_file_viewer_error).toByteArray(), Charsets.UTF_8
        )

        textEdit.movementMethod = ScrollingMovementMethod()
        textEdit.setText(text)
        updateEditCapabilities(false)
        updateEditToggleButtonIcon()

        editToggleButton.setOnClickListener {
            updateEditCapabilities(!textEdit.isFocusable)
            updateEditToggleButtonIcon()
        }

        decryptionResultCache.invalidateResultByFileId(args.fileArgs.fileId)
    }

    private fun updateEditToggleButtonIcon() {
        val icon = if (textEdit.isFocusable)
            OutlinedGoogleMaterial.Icon.gmo_save
        else
            OutlinedGoogleMaterial.Icon.gmo_edit

        editToggleButton.imageTintList = null
        editToggleButton.setImageDrawable(IconicsDrawable(requireContext(), icon).apply {
            colorInt = Color.WHITE
        })
    }

    private fun updateEditCapabilities(allowEditing: Boolean) {
        textEdit.isFocusable = allowEditing
        textEdit.clearFocus()
    }
}