package uk.co.adamchaplin.countrycounter.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.R
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.Locale

object Utils {
    @JvmStatic
    fun checkEasterEggs(
        applicationContext: Context,
        resources: Resources,
        visitedCountries: MutableMap<Continent, Set<String>>
    ) {
        checkWondersOfTheWorldEasterEgg(applicationContext, resources, visitedCountries)
        checkUNStatesAfter2000EasterEgg(applicationContext, resources, visitedCountries)
    }

    @JvmStatic
    fun checkWondersOfTheWorldEasterEgg(
        applicationContext: Context,
        resources: Resources,
        visitedCountries: MutableMap<Continent, Set<String>>
    ){
        if(visitedCountries[Continent.AFRICA]?.isEmpty() != false
            && visitedCountries[Continent.ANTARCTICA]?.isEmpty() != false
            && visitedCountries[Continent.ASIA] == mutableSetOf("China", "Jordan", "India")
            && visitedCountries[Continent.EUROPE] == mutableSetOf("Italy")
            && visitedCountries[Continent.NORTH_AMERICA] == mutableSetOf("Mexico")
            && visitedCountries[Continent.OCEANIA]?.isEmpty() != false
            && visitedCountries[Continent.SOUTH_AMERICA] == mutableSetOf("Peru", "Brazil")) {
            val sharedPref = applicationContext.getSharedPreferences(resources.getString(R.string.other_prefs_file),Context.MODE_PRIVATE)

            val hideAds = sharedPref.getBoolean(resources.getString(R.string.hide_ads), false)
            val action: String
            val firebaseAnalytics = Firebase.analytics
            action = if(hideAds){
                firebaseAnalytics.logEvent("Ads_enabled"){}
                "Enabled"
            } else {
                firebaseAnalytics.logEvent("Ads_disabled") {}
                "Disabled"
            }
            val toastText = "Ads have been ${action.lowercase(Locale.getDefault())}!\nPlease restart the app for it to take effect"
            with(sharedPref.edit()) {
                putBoolean(resources.getString(R.string.hide_ads), !hideAds)
                apply()
            }
            val toast = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    @JvmStatic
    fun checkUNStatesAfter2000EasterEgg(
        applicationContext: Context,
        resources: Resources,
        visitedCountries: MutableMap<Continent, Set<String>>
    ){
        if(visitedCountries[Continent.AFRICA] == mutableSetOf("South Sudan")
            && visitedCountries[Continent.ANTARCTICA]?.isEmpty() != false
            && visitedCountries[Continent.ASIA] == mutableSetOf("Timor-Leste")
            && visitedCountries[Continent.EUROPE] == mutableSetOf("Serbia", "Switzerland", "Montenegro")
            && visitedCountries[Continent.NORTH_AMERICA]?.isEmpty() != false
            && visitedCountries[Continent.OCEANIA] == mutableSetOf("Tuvalu")
            && visitedCountries[Continent.SOUTH_AMERICA]?.isEmpty() != false) {
            val sharedPref = applicationContext.getSharedPreferences(resources.getString(R.string.other_prefs_file),Context.MODE_PRIVATE)

            val stopAnalytics = sharedPref.getBoolean(resources.getString(R.string.stop_analytics), false)
            val action: String
            val firebaseAnalytics = Firebase.analytics
            action = if(stopAnalytics){
                firebaseAnalytics.logEvent("Analytics_enabled") {
                }
                "Enabled"
            } else {
                firebaseAnalytics.logEvent("Analytics_disabled") {
                }
                "Disabled"
            }
            val toastText = "Analytics data sending has been ${action.lowercase(Locale.getDefault())}!\nPlease restart the app for it to take effect"

            with(sharedPref.edit()) {
                putBoolean(resources.getString(R.string.stop_analytics), !stopAnalytics)
                apply()
            }
            val toast = Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    @JvmStatic
    fun getColourFromContinent(resources: Resources, continent: Continent): Int {
        return when(continent) {
            Continent.AFRICA -> resources.getColor(R.color.africa, null)
            Continent.ANTARCTICA -> resources.getColor(R.color.antarctica, null)
            Continent.ASIA -> resources.getColor(R.color.asia, null)
            Continent.EUROPE -> resources.getColor(R.color.europe, null)
            Continent.NORTH_AMERICA -> resources.getColor(R.color.northAmerica,null)
            Continent.OCEANIA -> resources.getColor(R.color.oceania, null)
            Continent.SOUTH_AMERICA -> resources.getColor(R.color.southAmerica, null)
            Continent.ALL_COUNTRIES -> resources.getColor(R.color.worldCountry, null)
            Continent.ALL_CONTINENTS -> resources.getColor(R.color.worldContinent, null)
            Continent.NONE -> resources.getColor(R.color.background_tint, null)
        }
    }

    @JvmStatic
    fun getStringFromResource(resources: Resources, resourceId: Int): String {
        return try {
            val inputStream: InputStream = resources.openRawResource(resourceId)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
    }

    @JvmStatic
    fun addCountriesToIntent(intent: Intent, countries: Set<String>, name: Continent, resources: Resources) {
        val tempList: ArrayList<String> = arrayListOf()
        tempList.addAll(countries)
        intent.putStringArrayListExtra(resources.getString(R.string.countriesListParcel), tempList)
        intent.putExtra(resources.getString(R.string.continentName), name)
    }
}