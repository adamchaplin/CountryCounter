package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import uk.co.adamchaplin.countrycounter.Utils.addArrayToIntent
import uk.co.adamchaplin.countrycounter.databinding.FragmentDashboardBinding
import uk.co.adamchaplin.countrycounter.databinding.WindowPopupBinding

class DashboardFragment: Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        applicationContext = activity as Context
        mainActivity = activity as MainActivity

        return view
    }

    override fun onStart() {
        super.onStart()
        updateStats()
        binding.worldCountryProgressBar.setOnClickListener { showPopupWindow(view, (mainActivity.visitedAfricanCountries + mainActivity.visitedAfricanCountries + mainActivity.visitedAntarcticaCountries + mainActivity.visitedAsianCountries + mainActivity.visitedEuropeanCountries + mainActivity.visitedNorthAmericanCountries + mainActivity.visitedOceanianCountries + mainActivity.visitedSouthAmericanCountries).sorted().toMutableSet(), getString(R.string.world_country_title)) }
        binding.worldContinentProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedContinents, getString(R.string.world_continent_title), true) }
        binding.africaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedAfricanCountries, getString(R.string.internal_africa)) }
        binding.antarcticaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedAntarcticaCountries, getString(R.string.internal_antarctica)) }
        binding.asiaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedAsianCountries, getString(R.string.internal_asia)) }
        binding.europeProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedEuropeanCountries, getString(R.string.internal_europe)) }
        binding.northAmericaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedNorthAmericanCountries, getString(R.string.internal_north_america)) }
        binding.oceaniaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedOceanianCountries, getString(R.string.internal_oceania)) }
        binding.southAmericaProgressBar.setOnClickListener { showPopupWindow(view, mainActivity.visitedSouthAmericanCountries, getString(R.string.internal_south_america)) }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.importVisitedCountries(false)
        updateStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateStats() {
        val totalVisitedCountries =
            mainActivity.visitedAfricanCountries.size + mainActivity.visitedAntarcticaCountries.size + mainActivity.visitedAsianCountries.size + mainActivity.visitedEuropeanCountries.size + mainActivity.visitedNorthAmericanCountries.size + mainActivity.visitedOceanianCountries.size + mainActivity.visitedSouthAmericanCountries.size
        val totalCountries =
            mainActivity.totalAfricanCountries + mainActivity.totalAntarcticaCountries + mainActivity.totalAsianCountries + mainActivity.totalEuropeanCountries + mainActivity.totalNorthAmericanCountries + mainActivity.totalOceanianCountries + mainActivity.totalSouthAmericanCountries
        updateCountryStats(totalVisitedCountries, totalCountries, binding.worldCountryText, binding.worldCountryProgressBar)
        updateContinentStats(mainActivity.visitedContinents.size)
        updateCountryStats(mainActivity.visitedAfricanCountries.size, mainActivity.totalAfricanCountries, binding.africaText, binding.africaProgressBar)
        updateCountryStats(mainActivity.visitedAntarcticaCountries.size, mainActivity.totalAntarcticaCountries, binding.antarcticaText, binding.antarcticaProgressBar)
        updateCountryStats(mainActivity.visitedAsianCountries.size, mainActivity.totalAsianCountries, binding.asiaText, binding.asiaProgressBar)
        updateCountryStats(mainActivity.visitedEuropeanCountries.size, mainActivity.totalEuropeanCountries, binding.europeText, binding.europeProgressBar)
        updateCountryStats(mainActivity.visitedNorthAmericanCountries.size, mainActivity.totalNorthAmericanCountries, binding.northAmericanText, binding.northAmericaProgressBar)
        updateCountryStats(mainActivity.visitedOceanianCountries.size, mainActivity.totalOceanianCountries, binding.oceaniaText, binding.oceaniaProgressBar)
        updateCountryStats(mainActivity.visitedSouthAmericanCountries.size, mainActivity.totalSouthAmericanCountries, binding.southAmericanText, binding.southAmericaProgressBar)
    }

    private fun updateContinentStats(visitedContinents: Int){
        val percentRaw = (visitedContinents.toFloat() / 7) * 100
        val percent = String.format("%.2f", percentRaw)
        binding.worldContinentText.text = getString(R.string.stats, visitedContinents.toString(), 7.toString(), percent)
        binding.worldContinentProgressBar.setMax(7)
        binding.worldContinentProgressBar.setProgress(visitedContinents.toFloat())
    }

    private fun updateCountryStats(visitedCountries: Int, totalCountries: Int, textView: TextView, progressBar: CircularProgressBar) {
        val percentRaw = (visitedCountries.toFloat() / totalCountries) * 100
        val percent = String.format("%.2f", percentRaw)
        textView.text = getString(R.string.stats, visitedCountries.toString(), totalCountries.toString(), percent)
        progressBar.setMax(totalCountries)
        progressBar.setProgress(visitedCountries.toFloat())
    }

    private fun showPopupWindow(view: View?, visitedCountryList: MutableSet<String>, continent: String, continents: Boolean = false) {

        val inflater = LayoutInflater.from(applicationContext)
        val windowPopupBinding = WindowPopupBinding.inflate(inflater)
        val popupView = windowPopupBinding.root
        val popUpListView = windowPopupBinding.popUpListView
        val tempList: ArrayList<String> = arrayListOf()
        if (visitedCountryList.size < 1 && continents) {
            tempList.add("No visited continents")
        } else if (visitedCountryList.size < 1) {
            tempList.add("No visited countries")
        } else {
            tempList.addAll(visitedCountryList)
        }
        val adapter = CustomTextAdapter(applicationContext, tempList, continent)
        popUpListView.adapter = adapter

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        popupView.setOnClickListener {
            popupWindow.dismiss()
        }
        if(continents || continent == resources.getString(R.string.world_country_title)) {
            windowPopupBinding.editView.visibility = INVISIBLE
        }

        windowPopupBinding.editView.setOnClickListener {
            mainActivity.refreshCountries()
            val intent = Intent(mainActivity, CountryListActivity::class.java)
            when(continent) {
                resources.getString(R.string.africa_title) -> addArrayToIntent(intent, mainActivity.africanCountries,
                    resources.getString(R.string.internal_africa), resources)
                resources.getString(R.string.antarctica_title) -> addArrayToIntent(intent, mainActivity.antarcticaCountries,
                    resources.getString(R.string.internal_antarctica), resources)
                resources.getString(R.string.asia_title) -> addArrayToIntent(intent, mainActivity.asianCountries,
                    resources.getString(R.string.internal_asia), resources)
                resources.getString(R.string.europe_title) -> addArrayToIntent(intent, mainActivity.europeanCountries,
                    resources.getString(R.string.internal_europe), resources)
                resources.getString(R.string.north_america_title) -> addArrayToIntent(intent, mainActivity.northAmericanCountries,
                    resources.getString(R.string.internal_north_america), resources)
                resources.getString(R.string.oceania_title) -> addArrayToIntent(intent, mainActivity.oceanianCountries,
                    resources.getString(R.string.internal_oceania), resources)
                resources.getString(R.string.south_america_title) -> addArrayToIntent(intent, mainActivity.southAmericanCountries,
                    resources.getString(R.string.internal_south_america), resources)
            }
            startActivity(intent)
            popupWindow.dismiss()
        }
    }

}