package ru.tim.criminalintent

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RationaleFragment(private val permission: String) : BottomSheetDialogFragment() {

    private lateinit var rationaleButton: Button
    private lateinit var rationaleTitle: TextView
    private lateinit var rationaleText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rationale, container, false)
        rationaleButton = view.findViewById(R.id.rationale_button)
        rationaleText = view.findViewById(R.id.rationale_text)
        rationaleTitle = view.findViewById(R.id.rationale_title)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (permission) {
            Manifest.permission.CAMERA -> {
                rationaleText.text = getString(R.string.camera_rationale_text)
                rationaleTitle.text = getString(R.string.camera_rationale_title)
            }
            Manifest.permission.READ_CONTACTS -> {
                rationaleText.text = getString(R.string.contacts_rationale_text)
                rationaleTitle.text = getString(R.string.contacts_rationale_title)
            }
        }

        rationaleButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        when (permission) {
            Manifest.permission.CAMERA -> {
                setFragmentResult(
                    REQUEST_CAMERA_RATIONALE_KEY,
                    bundleOf()
                )
            }
            Manifest.permission.READ_CONTACTS -> {
                setFragmentResult(
                    REQUEST_CONTACTS_RATIONALE_KEY,
                    bundleOf()
                )
            }
        }
    }

    companion object {
        const val TAG = "RationaleFragment"
        const val REQUEST_CAMERA_RATIONALE_KEY = "camera_rationale_key"
        const val REQUEST_CONTACTS_RATIONALE_KEY = "contacts_rationale_key"
    }
}