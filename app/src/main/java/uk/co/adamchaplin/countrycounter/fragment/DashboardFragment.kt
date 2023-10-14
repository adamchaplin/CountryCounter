package uk.co.adamchaplin.countrycounter.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uk.co.adamchaplin.countrycounter.CircularProgressBar
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.CountryListActivity
import uk.co.adamchaplin.countrycounter.R
import uk.co.adamchaplin.countrycounter.adapter.CustomTextAdapter
import uk.co.adamchaplin.countrycounter.utils.Utils.addCountriesToIntent
import uk.co.adamchaplin.countrycounter.databinding.FragmentDashboardBinding
import uk.co.adamchaplin.countrycounter.databinding.WindowPopupBinding
import uk.co.adamchaplin.countrycounter.model.CountriesViewModel
import uk.co.adamchaplin.countrycounter.utils.Utils

class DashboardFragment: Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private val countriesViewModel: CountriesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        applicationContext = activity as Context

        createObservables()
        updateStats()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupProgressBarClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateStats() {
        val totalVisitedCountries = countriesViewModel.getVisitedCountriesFlattened().size
        val totalCountries = countriesViewModel.getAllCountriesFlattened().size
        updateContinentStats(countriesViewModel.getVisitedContinents().size)
        updateCountryStats(totalVisitedCountries, totalCountries, binding.worldCountryText, binding.worldCountryProgressBar)
        updateCountryStats(Continent.AFRICA, binding.africaText, binding.africaProgressBar)
        updateCountryStats(Continent.ANTARCTICA, binding.antarcticaText, binding.antarcticaProgressBar)
        updateCountryStats(Continent.ASIA, binding.asiaText, binding.asiaProgressBar)
        updateCountryStats(Continent.EUROPE, binding.europeText, binding.europeProgressBar)
        updateCountryStats(Continent.NORTH_AMERICA, binding.northAmericanText, binding.northAmericaProgressBar)
        updateCountryStats(Continent.OCEANIA, binding.oceaniaText, binding.oceaniaProgressBar)
        updateCountryStats(Continent.SOUTH_AMERICA, binding.southAmericanText, binding.southAmericaProgressBar)
    }

    private fun updateContinentStats(visitedContinents: Int){
        val percentRaw = (visitedContinents.toFloat() / 7) * 100
        val percent = String.format("%.2f", percentRaw)
        binding.worldContinentText.text = getString(R.string.stats, visitedContinents.toString(), 7.toString(), percent)
        binding.worldContinentProgressBar.setMax(7)
        binding.worldContinentProgressBar.setProgress(visitedContinents.toFloat())
    }

    private fun updateCountryStats(continent: Continent, textView: TextView, progressBar: CircularProgressBar) {
        val visitedCountries = countriesViewModel.getVisitedCountriesForContinent(continent).size
        val totalCountries = countriesViewModel.getAllCountriesForContinent(continent).size
        updateCountryStats(visitedCountries, totalCountries, textView, progressBar)
    }

    private fun updateCountryStats(visitedCountries: Int, totalCountries: Int, textView: TextView, progressBar: CircularProgressBar) {
        val percentRaw = (visitedCountries.toFloat() / totalCountries) * 100
        val percent = String.format("%.2f", percentRaw)
        textView.text = getString(R.string.stats, visitedCountries.toString(), totalCountries.toString(), percent)
        progressBar.setMax(totalCountries)
        progressBar.setProgress(visitedCountries.toFloat())
    }

    private fun setupProgressBarClickListeners() {
        binding.worldCountryProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesFlattened(),
                Continent.ALL_COUNTRIES
            )
        }
        binding.worldContinentProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedContinents(),
                Continent.ALL_CONTINENTS
            )
        }
        binding.africaProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.AFRICA),
                Continent.AFRICA
            )
        }
        binding.antarcticaProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.ANTARCTICA),
                Continent.ANTARCTICA
            )
        }
        binding.asiaProgressBar.setOnClickListener {
                showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.ASIA),
                Continent.ASIA
            )
        }
        binding.europeProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.EUROPE),
                Continent.EUROPE
            )
        }
        binding.northAmericaProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.NORTH_AMERICA),
                Continent.NORTH_AMERICA
            )
        }
        binding.oceaniaProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.OCEANIA),
                Continent.OCEANIA
            )
        }
        binding.southAmericaProgressBar.setOnClickListener {
            showPopupWindow(
                view,
                countriesViewModel.getVisitedCountriesForContinent(Continent.SOUTH_AMERICA),
                Continent.SOUTH_AMERICA
            )
        }
    }

    private fun showPopupWindow(view: View?, visitedCountryList: Set<String>, continent: Continent) {
        val inflater = LayoutInflater.from(applicationContext)
        val windowPopupBinding = WindowPopupBinding.inflate(inflater)
        val popupView = windowPopupBinding.root
        val popUpRecyclerView = windowPopupBinding.popUpRecyclerView
        val colour = Utils.getColourFromContinent(resources, continent)

        val countriesToDisplayList = if (visitedCountryList.isEmpty() && continent == Continent.ALL_CONTINENTS) {
            setOf("No visited continents")
        } else if (visitedCountryList.isEmpty()) {
            setOf("No visited countries")
        } else {
            visitedCountryList.sorted()
        }

        popUpRecyclerView.setHasFixedSize(true)
        popUpRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        popUpRecyclerView.adapter = CustomTextAdapter(countriesToDisplayList.toList(), colour)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val popupWindow = PopupWindow(popupView, width, height, true)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }

        if(continent == Continent.ALL_CONTINENTS || continent == Continent.ALL_COUNTRIES) {
            windowPopupBinding.editView.visibility = INVISIBLE
        }

        windowPopupBinding.editView.setOnClickListener {
            val intent = Intent(applicationContext, CountryListActivity::class.java)
            addCountriesToIntent(intent, countriesViewModel.getAllCountriesForContinent(continent), continent, resources)
            startActivity(intent)
            popupWindow.dismiss()
        }
    }

    private fun createObservables() {
        countriesViewModel.visitedCountries.observe(viewLifecycleOwner) {
            Log.d("DashboardFragment", "Updating statistics because 'visitedCountries' was updated")
            updateStats()
        }
        countriesViewModel.allCountries.observe(viewLifecycleOwner) {
            Log.d("DashboardFragment", "Updating statistics because 'allCountries' was updated")
            updateStats()
        }
    }

}