package uk.co.adamchaplin.countrycounter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.CheckedTextView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import uk.co.adamchaplin.countrycounter.databinding.ActivityCountryListBinding
import uk.co.adamchaplin.countrycounter.databinding.HelperSaveCancelBinding

class CountryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCountryListBinding
    private lateinit var continentName: String
    private lateinit var countries: ArrayList<Country>
    private var visitedAfricanCountries: MutableSet<String> = mutableSetOf()
    private var visitedAntarcticaCountries: MutableSet<String> = mutableSetOf()
    private var visitedAsianCountries: MutableSet<String> = mutableSetOf()
    private var visitedEuropeanCountries: MutableSet<String> = mutableSetOf()
    private var visitedNorthAmericanCountries: MutableSet<String> = mutableSetOf()
    private var visitedOceanianCountries: MutableSet<String> = mutableSetOf()
    private var visitedSouthAmericanCountries: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountryListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()
        countries = intent.getParcelableArrayListExtra(applicationContext.resources.getString(R.string.countriesListParcel))!!
        continentName = intent.getStringExtra(applicationContext.resources.getString(R.string.continentName))!!
        refreshView()
    }

    override fun onStart(){
        super.onStart()
        binding.saveView.setOnClickListener {
            saveCountries()
            finish()
        }
        binding.cancelView.setOnClickListener {
            finish()
        }
        introCountriesSaveCancel(binding.root)
    }

    private fun refreshView() {

        val adapter = CustomCheckAdapter(applicationContext, countries)
        binding.countryList.adapter = adapter

        binding.countryList.setOnItemClickListener { _, view, position, _ ->
            (view as CheckedTextView)
            view.isChecked = !view.isChecked
            val element = adapter.getItem(position)
            element.unsavedVisited = view.isChecked
        }
    }

    private fun saveCountries(){
        importVisitedCountries()
        saveVisitedCountries()
    }

    private fun importVisitedCountries(){
        val data = Utils.readFromFile(resources.getString(R.string.countries_file), this)
        if (!data.isNullOrEmpty()) {
            val visited = Gson().fromJson(data, JsonObject::class.java)
            val version = visited.get("Version").asString
            if(version == "1.0.0") {
                visitedAfricanCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_africa))
                visitedAntarcticaCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_antarctica))
                visitedAsianCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_asia))
                visitedEuropeanCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_europe))
                visitedNorthAmericanCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_north_america))
                visitedOceanianCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_oceania))
                visitedSouthAmericanCountries = Utils.getContinentVisitedCountries(visited, getString(R.string.internal_south_america))
            } else {
                val e = Exception("Error reading file version for ${resources.getString(R.string.countries_file)}: $version")
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("CountryListActivity", "Error reading file version for ${resources.getString(R.string.countries_file)}: $version")
            }
        } else {
            Log.e("CountryListActivity", "Failed to process countries file.")
        }
    }

    private fun saveVisitedCountries(){
        updateSingleVisitedList(visitedAfricanCountries, getString(R.string.internal_africa))
        updateSingleVisitedList(visitedAntarcticaCountries, getString(R.string.internal_antarctica))
        updateSingleVisitedList(visitedAsianCountries, getString(R.string.internal_asia))
        updateSingleVisitedList(visitedEuropeanCountries, getString(R.string.internal_europe))
        updateSingleVisitedList(visitedNorthAmericanCountries, getString(R.string.internal_north_america))
        updateSingleVisitedList(visitedOceanianCountries, getString(R.string.internal_oceania))
        updateSingleVisitedList(visitedSouthAmericanCountries, getString(R.string.internal_south_america))
        val visitedObject = JSONObject()
        visitedObject.put("Version", "1.0.0")
        val afVisited = Gson().toJson(visitedAfricanCountries)
        visitedObject.put(getString(R.string.internal_africa), afVisited)
        val anVisited = Gson().toJson(visitedAntarcticaCountries)
        visitedObject.put(getString(R.string.internal_antarctica), anVisited)
        val asVisited = Gson().toJson(visitedAsianCountries)
        visitedObject.put(getString(R.string.internal_asia), asVisited)
        val euVisited = Gson().toJson(visitedEuropeanCountries)
        visitedObject.put(getString(R.string.internal_europe), euVisited)
        val naVisited = Gson().toJson(visitedNorthAmericanCountries)
        visitedObject.put(getString(R.string.internal_north_america), naVisited)
        val ocVisited = Gson().toJson(visitedOceanianCountries)
        visitedObject.put(getString(R.string.internal_oceania), ocVisited)
        val saVisited = Gson().toJson(visitedSouthAmericanCountries)
        visitedObject.put(getString(R.string.internal_south_america), saVisited)
        Utils.writeToFile(resources.getString(R.string.countries_file), visitedObject.toString() , this)
        Utils.checkEasterEggs(applicationContext, resources, visitedAfricanCountries, visitedAntarcticaCountries, visitedAsianCountries, visitedEuropeanCountries, visitedNorthAmericanCountries, visitedOceanianCountries, visitedSouthAmericanCountries)
    }

    private fun updateSingleVisitedList(visitedCountries: MutableSet<String>, thisContinent: String){
        if(thisContinent == continentName) {
            visitedCountries.clear()
            for (i in countries) {
                if (i.unsavedVisited) {
                    visitedCountries.add(i.countryName)
                }
            }
        }
    }

    private fun introCountriesSaveCancel(mainCountries: FrameLayout){
        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        val sharedPref = applicationContext.getSharedPreferences(getString(R.string.intro_file),
            Context.MODE_PRIVATE)

        var introSave = sharedPref.getBoolean(getString(R.string.intro_countries_save_cancel), false)

        if(!introSave) {
            val inflater = LayoutInflater.from(applicationContext)
            val introBinding = HelperSaveCancelBinding.inflate(inflater)
            val childLayout = introBinding.root

            introBinding.fullLayoutView.setOnClickListener {
                childLayout.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainCountries.removeView(childLayout)
                        }
                    })
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.intro_countries_save_cancel), true)
                    apply()
                }
                introSave = true
            }
            mainCountries.addView(childLayout)
        }
    }
}