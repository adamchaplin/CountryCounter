package uk.co.adamchaplin.countrycounter.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.CountryListActivity
import uk.co.adamchaplin.countrycounter.adapter.CustomTextEditAdapter
import uk.co.adamchaplin.countrycounter.utils.Utils.addCountriesToIntent
import uk.co.adamchaplin.countrycounter.databinding.FragmentCountriesBinding
import uk.co.adamchaplin.countrycounter.databinding.RecyclerViewEditItemBinding
import uk.co.adamchaplin.countrycounter.model.CountriesViewModel

class CountriesFragment: Fragment() {

    private var _binding: FragmentCountriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private val countriesViewModel: CountriesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountriesBinding.inflate(inflater, container, false)
        applicationContext = activity as Context

        createObservable()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setUpContinentListView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpContinentListView() {
        val continentListRecyclerView = binding.continentListRecyclerView
        continentListRecyclerView.setHasFixedSize(true)
        continentListRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

        val onClick: CustomTextEditAdapter.OnClickListener = object: CustomTextEditAdapter.OnClickListener {
            override fun onClick(continent: Continent, card: RecyclerViewEditItemBinding) {
                Log.d("CountriesFragment","Continent Clicked: " + continent.value)
                val intent = Intent(applicationContext, CountryListActivity::class.java)
                addCountriesToIntent(
                    intent,
                    countriesViewModel.getAllCountriesForContinent(continent),
                    continent,
                    resources
                )
                startActivity(intent)
            }
        }

        continentListRecyclerView.adapter = CustomTextEditAdapter(applicationContext, Continent.basicContinents, onClick)
    }

    private fun createObservable() {
        countriesViewModel.allCountries.observe(viewLifecycleOwner) {
            Log.d("CountriesFragment", "Updating continent list because 'allCountries' was updated")
            setUpContinentListView()
        }
    }

}