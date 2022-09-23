package ru.tim.criminalintent

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.Observer as Observer
import ru.tim.criminalintent.RationaleFragment.Companion.REQUEST_CAMERA_RATIONALE_KEY
import ru.tim.criminalintent.RationaleFragment.Companion.REQUEST_CONTACTS_RATIONALE_KEY
import java.io.File
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DATE_FORMAT = "EEEE, dd MMMM, yyyy"
private const val TIME_FORMAT = "HH:mm"
private const val DATE_REPORT_FORMAT = "EE, MMM, dd"

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var suspectButton: Button
    private lateinit var suspectPhoneButton: Button
    private lateinit var reportButton: Button
    private lateinit var photoView: ImageView
    private lateinit var photoButton: ImageButton

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this)[CrimeDetailViewModel::class.java]
    }

    private val contactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                try {
                    pickContact.launch()
                } catch (e: ActivityNotFoundException) {
                    suspectButton.isEnabled = false
                }
            } else {
                Toast.makeText(context, R.string.contacts_access_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val pickContact =
        registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
            viewLifecycleOwner.lifecycleScope.launch {
                delay(10)
                pickContact(uri)
            }
        }

    private val phoneCall =
        registerForActivityResult(object : ActivityResultContract<String, Void?>() {
            override fun createIntent(context: Context, input: String): Intent {
                return Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$input")
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Void? {
                return null
            }

        }) {}

    private val cameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                try {
                    takePhoto.launch(photoUri)
                } catch (e: ActivityNotFoundException) {
                    suspectButton.isEnabled = false
                }
            } else {
                Toast.makeText(context, R.string.camera_access_denied, Toast.LENGTH_LONG).show()
            }
        }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
            viewLifecycleOwner.lifecycleScope.launch {
                delay(10)
                if (isSaved) updatePhotoView()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        val crimeId = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

        setFragmentResultListener(REQUEST_DATE_KEY) { _, bundle ->
            bundle.getSerializable(RESULT_DATE_KEY)?.let {
                crime.date = it as Date
                updateUI()
            }
        }

        setFragmentResultListener(REQUEST_TIME_KEY) { _, bundle ->
            bundle.getSerializable(RESULT_TIME_KEY)?.let {
                crime.date = it as Date
                updateUI()
            }
        }

        setFragmentResultListener(REQUEST_CONTACTS_RATIONALE_KEY) { _, _ ->
            contactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }

        setFragmentResultListener(REQUEST_CAMERA_RATIONALE_KEY) { _, _ ->
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title)
        dateButton = view.findViewById(R.id.crime_date)
        timeButton = view.findViewById(R.id.crime_time)
        solvedCheckBox = view.findViewById(R.id.crime_solved)
        suspectButton = view.findViewById(R.id.crime_suspect)
        suspectPhoneButton = view.findViewById(R.id.crime_suspect_phone)
        reportButton = view.findViewById(R.id.crime_report)
        photoButton = view.findViewById(R.id.crime_camera)
        photoView = view.findViewById(R.id.crime_photo)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimesLiveData.observe(viewLifecycleOwner) { crime ->
            Log.e("CrimeFragment", "observe$crime")
            crime?.let {
                this.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "ru.tim.criminalintent.fileprovider",
                    photoFile
                )
                updateUI()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).show(parentFragmentManager, DIALOG_DATE)
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).show(parentFragmentManager, DIALOG_TIME)
        }

        suspectButton.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                RationaleFragment(Manifest.permission.READ_CONTACTS).show(
                    parentFragmentManager,
                    RationaleFragment.TAG
                )
            } else {
                contactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        suspectPhoneButton.setOnClickListener {
            if (crime.suspectPhone.isNotEmpty()) {
                try {
                    phoneCall.launch(crime.suspectPhone)
                } catch (e: ActivityNotFoundException) {
                    suspectPhoneButton.isEnabled = false
                }
            } else {
                Toast.makeText(context, R.string.crime_suspect_text, Toast.LENGTH_SHORT).show()
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)

                // Проверяем есть ли подходящие активити, если нет блокируем кнопку отчёта
                val packageManager = requireActivity().packageManager
                val resolvedActivity =
                    packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity == null) reportButton.isEnabled = false
            }
        }

        photoButton.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                RationaleFragment(Manifest.permission.CAMERA).show(
                    parentFragmentManager,
                    RationaleFragment.TAG
                )
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }

        photoView.setOnClickListener {
            if (photoFile.exists()) {
                DetailImageFragment.newInstance(photoFile)
                    .show(parentFragmentManager, DIALOG_DETAIL_IMAGE)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun pickContact(uri: Uri?) {
        val queryFields = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts._ID
        )
        val cursor =
            uri?.let {
                requireActivity().contentResolver.query(
                    it,
                    queryFields,
                    null,
                    null,
                    null
                )
            }

        cursor?.use { c ->
            if (c.count == 0) return@use

            c.moveToFirst()
            val suspect = c.getString(0)
            crime.suspect = suspect

            val suspectId = c.getString(1)
            val phoneCursor = requireActivity().contentResolver.query(
                Phone.CONTENT_URI,
                arrayOf(Phone.NUMBER),
                Phone.CONTACT_ID + " = $suspectId",
                null,
                null
            )

            phoneCursor?.use {
                if (it.count != 0) {
                    it.moveToFirst()
                    val suspectPhone = it.getString(0)
                    crime.suspectPhone = suspectPhone
                }
            }

            // При Dont keep activity происходит пересоздание фрагмента и crime не успевает
            // загрузить данные из БД (и вызвать колбэк метода observe),
            // а заполняет себя данными по умолчанию,
            // поэтому здесь происходит попытка
            // обновить данные нового преступления с другим идентификатором,
            // но так как преступления с таким ID в базе нет
            // то ничего не происходит и вновь выбранный контакт не сохраняется
            // (то же самое происходит и с камерой)
            // поэтому в ActivityResult была добавлена задержка delay(100)
            crimeDetailViewModel.saveCrime(crime)
        }

        //при таком варианте работает нормально
        /*crimeDetailViewModel.crimesLiveData.observe(
            viewLifecycleOwner,
            object : Observer<Crime?> {
                override fun onChanged(c: Crime?) {
                    crimeDetailViewModel.crimesLiveData.removeObserver(this)
                    c?.let {
                        c.suspect = suspect ?: c.suspect
                        c.suspectPhone = suspectPhone ?: c.suspectPhone
                        crimeDetailViewModel.saveCrime(c)
                    }
                }

            })*/
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = DateFormat.getLongDateFormat(requireContext()).format(crime.date)
        timeButton.text = DateFormat.getTimeFormat(requireContext()).format(crime.date)
        //dateButton.text = DateFormat.format(DATE_FORMAT, crime.date)
        //timeButton.text = DateFormat.format(TIME_FORMAT, crime.date)
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect

            if (crime.suspectPhone.isNotEmpty()) {
                suspectPhoneButton.text = crime.suspectPhone
            }
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo)
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_no_photo)
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.getLongDateFormat(requireContext()).format(crime.date)
        //val dateString = DateFormat.format(DATE_REPORT_FORMAT, crime.date).toString()

        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply { putSerializable(ARG_CRIME_ID, crimeId) }
            return CrimeFragment().apply { arguments = args }
        }
    }
}