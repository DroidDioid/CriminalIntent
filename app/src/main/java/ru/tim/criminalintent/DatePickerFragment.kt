package ru.tim.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"
const val REQUEST_DATE_KEY = "date_key"
const val RESULT_DATE_KEY = "extra_key"

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)

        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val resultDate =
                GregorianCalendar(year, month, dayOfMonth, initialHour, initialMinute).time
            parentFragmentManager.setFragmentResult(
                REQUEST_DATE_KEY,
                bundleOf(RESULT_DATE_KEY to resultDate)
            )
        }

        return DatePickerDialog(
            requireContext(),
            dateListener,
            initialYear,
            initialMonth,
            initialDay
        )
    }

    companion object {
        fun newInstance(date: Date): DatePickerFragment {
            val args = Bundle().apply { putSerializable(ARG_DATE, date) }
            return DatePickerFragment().apply { arguments = args }
        }
    }

}