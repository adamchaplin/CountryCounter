package uk.co.adamchaplin.countrycounter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import uk.co.adamchaplin.countrycounter.adapter.CustomCheckAdapter
import uk.co.adamchaplin.countrycounter.databinding.ActivityCountryListBinding
import uk.co.adamchaplin.countrycounter.databinding.HelperActivityCountriesBinding
import uk.co.adamchaplin.countrycounter.databinding.RecyclerViewCheckboxItemBinding
import uk.co.adamchaplin.countrycounter.utils.FileUtils
import uk.co.adamchaplin.countrycounter.utils.Utils

class CountryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountryListBinding
    private var visitedCountries: MutableMap<Continent, Set<String>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CountriesListActivity","Create CountriesListActivity")
        super.onCreate(savedInstanceState)
        binding = ActivityCountryListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val countriesList: ArrayList<String> = intent.getStringArrayListExtra(applicationContext.resources.getString(R.string.countriesListParcel))!!
        val currentContinent: Continent = intent.getSerializableExtra(applicationContext.resources.getString(R.string.continentName))!! as Continent
        visitedCountries = FileUtils.getVisitedCountriesFromFile(resources.getString(R.string.countries_file), applicationContext)
        resetContentView(currentContinent, countriesList)
        binding.saveView.setOnClickListener {
            FileUtils.saveVisitedCountriesToFile(visitedCountries, resources, applicationContext)
            finish()
        }
        binding.cancelView.setOnClickListener {
            finish()
        }
        displayCountriesActivityHelper(binding.root)
    }

    private fun resetContentView(currentContinent: Continent, countriesList: ArrayList<String>) {
        val countryListRecyclerView = binding.countryList
        countryListRecyclerView.setHasFixedSize(true)
        countryListRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

        val colour = Utils.getColourFromContinent(resources, currentContinent)

        val onClick: CustomCheckAdapter.OnClickListener = object: CustomCheckAdapter.OnClickListener {
            override fun onClick(card: RecyclerViewCheckboxItemBinding) {
                card.checkedText.isChecked = !card.checkedText.isChecked
                if(card.checkedText.isChecked) {
                    visitedCountries[currentContinent] = visitedCountries[currentContinent]?.plus(card.checkedText.text.toString()) ?: setOf(card.checkedText.text.toString())
                } else {
                    visitedCountries[currentContinent] = visitedCountries[currentContinent]?.minus(card.checkedText.text.toString()) ?: setOf()
                }
            }
        }

        countryListRecyclerView.adapter = CustomCheckAdapter(countriesList, visitedCountries[currentContinent]?.toMutableSet() ?: mutableSetOf(), colour, onClick)
    }

    private fun displayCountriesActivityHelper(mainCountries: FrameLayout){
        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val sharedPreferences = applicationContext.getSharedPreferences(getString(R.string.intro_file),
            Context.MODE_PRIVATE)

        val hasSeenHelper = sharedPreferences.getBoolean(getString(R.string.countries_activity_helper), false)

        if(!hasSeenHelper) {
            val introBinding = HelperActivityCountriesBinding.inflate(LayoutInflater.from(applicationContext))

            introBinding.fullLayoutView.setOnClickListener {
                introBinding.root.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainCountries.removeView(introBinding.root)
                        }
                    })
                with(sharedPreferences.edit()) {
                    putBoolean(getString(R.string.countries_activity_helper), true)
                    apply()
                }
            }
            mainCountries.addView(introBinding.root)
        }
    }
}