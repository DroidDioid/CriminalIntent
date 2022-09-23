package ru.tim.criminalintent

import androidx.lifecycle.ViewModel
import java.io.File

class CrimeListViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

    companion object {
        fun Boolean.toInt(): Int {
            return if (this) 1 else 0
        }
    }
}