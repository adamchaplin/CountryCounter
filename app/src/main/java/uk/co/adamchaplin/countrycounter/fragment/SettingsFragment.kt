package uk.co.adamchaplin.countrycounter.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.adapter.CustomExpandableTextSwitchAdapter
import uk.co.adamchaplin.countrycounter.databinding.FragmentSettingsBinding
import uk.co.adamchaplin.countrycounter.databinding.ListviewSwitchItemBinding
import uk.co.adamchaplin.countrycounter.model.CountriesViewModel
import uk.co.adamchaplin.countrycounter.model.ExpandableListData
import uk.co.adamchaplin.countrycounter.utils.FileUtils

class SettingsFragment: Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private val countriesViewModel: CountriesViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        applicationContext = activity as Context

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        refreshSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshSettings(){
        val data = Continent.basicContinents.map{ continent ->
            ExpandableListData(continent, CustomExpandableTextSwitchAdapter.PARENT, countriesViewModel.getAllSettingsForContinent(continent).toList(), false)
        }
        val onClick: CustomExpandableTextSwitchAdapter.OnClickListener = object: CustomExpandableTextSwitchAdapter.OnClickListener {
            override fun onClick(continent: Continent, card: ListviewSwitchItemBinding) {
                val checked = card.itemSwitch.isChecked
                val childText = card.itemSwitch.text.toString()
                val currentSettings = countriesViewModel.getActiveSettingsForContinent(continent)
                if(checked) {
                    if(childText !in currentSettings) {
                        countriesViewModel.setActiveSettingsForContinent(continent, currentSettings + childText)
                        countriesViewModel.addCountryToAllCountriesForContinent(continent, childText)
                    }
                } else {
                    if(childText in currentSettings) {
                        countriesViewModel.setActiveSettingsForContinent(continent, currentSettings - childText)
                        countriesViewModel.setVisitedCountriesForContinent(continent, countriesViewModel.getVisitedCountriesForContinent(continent) - childText)
                        countriesViewModel.removeCountryFromAllCountriesForContinent(continent, childText)
                    }
                }
                FileUtils.saveVisitedCountriesToFile(countriesViewModel.getVisitedCountries(), resources, applicationContext)
                FileUtils.saveActiveSettingsToFile(countriesViewModel.getActiveSettings(), resources, applicationContext)
            }
        }
        val listAdapter = CustomExpandableTextSwitchAdapter(applicationContext, data.toMutableList(), countriesViewModel, onClick)
        binding.settingsListRecyclerView.setHasFixedSize(true)
        binding.settingsListRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.settingsListRecyclerView.adapter = listAdapter
    }

}