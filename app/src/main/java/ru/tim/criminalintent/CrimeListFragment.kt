package ru.tim.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.tim.criminalintent.CrimeListViewModel.Companion.toInt
import java.util.*

private const val TAG = "CrimeListFragment"

class CrimeListFragment : Fragment(), MenuProvider {

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null
    private lateinit var newCrimeButton: Button
    private lateinit var emptyListLayout: LinearLayout
    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this)[CrimeListViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        newCrimeButton = view.findViewById(R.id.new_crime)
        emptyListLayout = view.findViewById(R.id.empty_list)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner) { crimes ->
            crimes?.let {
                emptyListLayout.visibility = if (crimes.isNotEmpty()) View.GONE else View.VISIBLE
                updateUI(crimes)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        newCrimeButton.setOnClickListener {
            createCrime()
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.new_crime -> {
                createCrime()
                true
            }
            else -> false
        }
    }

    private fun createCrime() {
        val crime = Crime()
        crimeListViewModel.addCrime(crime)
        callbacks?.onCrimeSelected(crime.id)
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    private inner class CrimeHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val requirePoliceButton: Button? = itemView.findViewById(R.id.require_police_button)
        private val solvedImageView: ImageView? = itemView.findViewById(R.id.crime_solved_image)

        init {
            itemView.setOnClickListener {
                callbacks?.onCrimeSelected(crime.id)
            }
            requirePoliceButton?.setOnClickListener {
                Toast.makeText(
                    context,
                    "Police required for ${crime.title}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = DateFormat.format("EEEE, dd MMMM, yyyy", this.crime.date)
            solvedImageView?.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }

            val solvedString = if (crime.isSolved) {
                getString(R.string.crime_report_solved)
            } else {
                getString(R.string.crime_report_unsolved)
            }

            val dateString = DateFormat.getLongDateFormat(requireContext()).format(crime.date)
            view.contentDescription = getString(
                R.string.list_item_crime_description,
                crime.title,
                dateString,
                solvedString
            )
        }
    }

    private inner class CrimeAdapter(var crimes: List<Crime>) :
        RecyclerView.Adapter<CrimeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = if (viewType == 0) {
                layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            } else {
                layoutInflater.inflate(R.layout.list_item_crime_with_police, parent, false)
            }
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            holder.bind(crimes[position])
        }

        override fun getItemCount() = crimes.size

        override fun getItemViewType(position: Int) =
            crimes[position].let { (!it.isSolved).toInt() }

    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}