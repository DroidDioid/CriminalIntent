package ru.tim.criminalintent

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.DialogCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.util.*

private const val ARG_FILE = "file"
const val DIALOG_DETAIL_IMAGE = "DetailImageFragment"

class DetailImageFragment : DialogFragment() {

    private lateinit var detailImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_detail_image, container)
        detailImage = view.findViewById(R.id.detailImage)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoFile = arguments?.getSerializable(ARG_FILE) as File

        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            detailImage.setImageBitmap(bitmap)
        } else {
            detailImage.setImageDrawable(null)
        }
    }

    companion object {
        fun newInstance(file: File): DetailImageFragment {
            val args = Bundle().apply { putSerializable(ARG_FILE, file) }
            return DetailImageFragment().apply { arguments = args }
        }
    }

}