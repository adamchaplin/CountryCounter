package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.util.Locale

object Utils {
    @JvmStatic
    fun getColourFromContinent(resources: Resources, continent: String): Int {
        with(continent) {
            return when {
                contains(resources.getString(R.string.africa_title)) -> resources.getColor(R.color.africa, null)
                contains(resources.getString(R.string.antarctica_title)) -> resources.getColor(R.color.antarctica, null)
                contains(resources.getString(R.string.asia_title))-> resources.getColor(R.color.asia, null)
                contains(resources.getString(R.string.europe_title)) -> resources.getColor(R.color.europe, null)
                contains(resources.getString(R.string.north_america_title)) -> resources.getColor(R.color.northAmerica,null)
                contains(resources.getString(R.string.oceania_title)) -> resources.getColor(R.color.oceania, null)
                contains(resources.getString(R.string.south_america_title)) -> resources.getColor(R.color.southAmerica, null)
                contains("none") -> resources.getColor(R.color.background_tint, null)
                else -> {
                    val e = Exception("Error getting color for unknown continent: $continent")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Color.TRANSPARENT
                }
            }
        }
    }

    @JvmStatic
    fun writeToFile(file: String, data: String, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "File write failed: $e")
        }
    }

    @JvmStatic
    fun getContinentVisitedCountries(visited: JsonObject, countryName: String): MutableSet<String> {
        try {
            val continentVisited = visited.get(countryName).asString
            return Gson().fromJson(continentVisited, MutableSet::class.java) as MutableSet<String>
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving $countryName countries from visited file")
        }
        return mutableSetOf()
    }

    @JvmStatic
    fun readFromFile(file: String, context: Context): String? {
        var ret = ""
        try {
            val inputStream: InputStream? = context.openFileInput(file)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String?
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "File not found: $e")
            return null
        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Can not read file: $e")
            return null
        }
        return ret
    }

    @JvmStatic
    fun loadStringFromAsset(resources: Resources, resourceId: Int): String {
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
    fun getContinentSettings(settings: JsonObject, countryName: String) : MutableSet<String> {
        try {
            val countrySettings = settings.get(countryName).asString
            return Gson().fromJson(countrySettings, MutableSet::class.java) as MutableSet<String>
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Utils", "Error retrieving $countryName settings from settings file")
        }
        return mutableSetOf()
    }

    @JvmStatic
    fun checkEasterEggs(
        applicationContext: Context,
        resources: Resources,
        visitedAfricanCountries: MutableSet<String>,
        visitedAntarcticaCountries: MutableSet<String>,
        visitedAsianCountries: MutableSet<String>,
        visitedEuropeanCountries: MutableSet<String>,
        visitedNorthAmericanCountries: MutableSet<String>,
        visitedOceanianCountries: MutableSet<String>,
        visitedSouthAmericanCountries: MutableSet<String>
    ) {
        checkWondersOfTheWorldEasterEgg(applicationContext, resources, visitedAfricanCountries, visitedAntarcticaCountries, visitedAsianCountries, visitedEuropeanCountries, visitedNorthAmericanCountries, visitedOceanianCountries, visitedSouthAmericanCountries)
        unStatesAfter2000EasterEgg(applicationContext, resources, visitedAfricanCountries, visitedAntarcticaCountries, visitedAsianCountries, visitedEuropeanCountries, visitedNorthAmericanCountries, visitedOceanianCountries, visitedSouthAmericanCountries)

    }

    @JvmStatic
    fun checkWondersOfTheWorldEasterEgg(
        applicationContext: Context,
        resources: Resources,
        visitedAfricanCountries: MutableSet<String>,
        visitedAntarcticaCountries: MutableSet<String>,
        visitedAsianCountries: MutableSet<String>,
        visitedEuropeanCountries: MutableSet<String>,
        visitedNorthAmericanCountries: MutableSet<String>,
        visitedOceanianCountries: MutableSet<String>,
        visitedSouthAmericanCountries: MutableSet<String>
    ){
        if(visitedAfricanCountries.isEmpty()
            && visitedAntarcticaCountries.isEmpty()
            && visitedAsianCountries == mutableSetOf("China", "Jordan", "India")
            && visitedEuropeanCountries == mutableSetOf("Italy")
            && visitedNorthAmericanCountries == mutableSetOf("Mexico")
            && visitedOceanianCountries.isEmpty()
            && visitedSouthAmericanCountries == mutableSetOf("Peru", "Brazil")) {
            val sharedPref = applicationContext.getSharedPreferences(resources.getString(R.string.other_prefs_file),Context.MODE_PRIVATE)

            val hideAds = sharedPref.getBoolean(resources.getString(R.string.hide_ads), false)
            val action: String
            val firebaseAnalytics = Firebase.analytics
            action = if(hideAds){
                firebaseAnalytics.logEvent("Ads_enabled") {
                }
                "Enabled"
            } else {
                firebaseAnalytics.logEvent("Ads_disabled") {
                }
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
    fun unStatesAfter2000EasterEgg(
        applicationContext: Context,
        resources: Resources,
        visitedAfricanCountries: MutableSet<String>,
        visitedAntarcticaCountries: MutableSet<String>,
        visitedAsianCountries: MutableSet<String>,
        visitedEuropeanCountries: MutableSet<String>,
        visitedNorthAmericanCountries: MutableSet<String>,
        visitedOceanianCountries: MutableSet<String>,
        visitedSouthAmericanCountries: MutableSet<String>){
        if(visitedAfricanCountries == mutableSetOf("South Sudan")
            && visitedAntarcticaCountries.isEmpty()
            && visitedAsianCountries == mutableSetOf("Timor-Leste")
            && visitedEuropeanCountries == mutableSetOf("Serbia", "Switzerland", "Montenegro")
            && visitedNorthAmericanCountries.isEmpty()
            && visitedOceanianCountries == mutableSetOf("Tuvalu")
            && visitedSouthAmericanCountries.isEmpty()) {
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
    fun addArrayToIntent(intent: Intent, countries: MutableSet<Country>, name: String, resources: Resources) {
        val tempList: ArrayList<Country> = arrayListOf()
        tempList.addAll(countries)
        intent.putParcelableArrayListExtra(resources.getString(R.string.countriesListParcel), tempList)
        intent.putExtra(resources.getString(R.string.continentName), name)
    }
}