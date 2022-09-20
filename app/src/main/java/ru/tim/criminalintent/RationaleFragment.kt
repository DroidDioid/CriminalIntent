package ru.tim.criminalintent

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RationaleFragment : BottomSheetDialogFragment() {

    private lateinit var rationaleButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rationale, container, false)
        rationaleButton = view.findViewById(R.id.rationale_button)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rationaleButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(
            REQUEST_RATIONALE_KEY,
            bundleOf()
        )
    }

    companion object {
        const val TAG = "RationaleFragment"
        const val REQUEST_RATIONALE_KEY = "rationale_key"
    }
}