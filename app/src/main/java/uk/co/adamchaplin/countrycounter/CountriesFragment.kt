package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import uk.co.adamchaplin.countrycounter.Utils.addArrayToIntent
import uk.co.adamchaplin.countrycounter.databinding.FragmentCountriesBinding

class CountriesFragment: Fragment() {

    private var _binding: FragmentCountriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        val view = binding.root
        applicationContext = activity as Context
        mainActivity = activity as MainActivity

        return view
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshData() {
        val continents = arrayListOf(resources.getString(R.string.internal_africa), resources.getString(R.string.internal_antarctica), resources.getString(R.string.internal_asia), resources.getString(R.string.internal_europe), resources.getString(R.string.internal_north_america), resources.getString(R.string.internal_oceania), resources.getString(R.string.internal_south_america))

        val adapter = CustomTextEditAdapter(applicationContext, continents)
        binding.continentList.adapter = adapter

        binding.continentList.setOnItemClickListener { _, _, position, _ ->

            mainActivity.refreshCountries()
            val element = adapter.getItem(position)
            val intent = Intent(mainActivity, CountryListActivity::class.java)
            when(element) {

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
        }
    }

}