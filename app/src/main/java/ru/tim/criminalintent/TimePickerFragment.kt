package ru.tim.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_TIME = "time"
const val REQUEST_TIME_KEY = "time_key"
const val RESULT_TIME_KEY = "time_result"

class TimePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_TIME) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val resultDate = GregorianCalendar(
                initialYear,
                initialMonth,
                initialDay,
                hourOfDay,
                minute
            ).time
            setFragmentResult(REQUEST_TIME_KEY, bundleOf(RESULT_TIME_KEY to resultDate))
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            true
        )
    }

    companion object {
        fun newInstance(time: Date): TimePickerFragment {
            val args = Bundle().apply { putSerializable(ARG_TIME, time) }
            return TimePickerFragment().apply { arguments = args }
        }
    }
}