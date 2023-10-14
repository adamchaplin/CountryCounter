package uk.co.adamchaplin.countrycounter.utils

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.R
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object FileUtils {
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
    fun importVisitedCountriesFromFile(file: String, context: Context): MutableMap<Continent, Set<String>>{
        val visitedCountriesRaw = readFromFile(file, context)
        if (!visitedCountriesRaw.isNullOrEmpty()) {
            val visitedCountriesJSON = Gson().fromJson(visitedCountriesRaw, JsonObject::class.java)
            val version = visitedCountriesJSON.get("Version").asString
            if(version == "1.0.0") {
                return mutableMapOf(
                    Continent.AFRICA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.AFRICA
                    ),
                    Continent.ANTARCTICA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.ANTARCTICA
                    ),
                    Continent.ASIA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.ASIA
                    ),
                    Continent.EUROPE to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.EUROPE
                    ),
                    Continent.NORTH_AMERICA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.NORTH_AMERICA
                    ),
                    Continent.OCEANIA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.OCEANIA
                    ),
                    Continent.SOUTH_AMERICA to JsonUtils.extractVisitedCountriesForContinentFromJson(
                        visitedCountriesJSON,
                        Continent.SOUTH_AMERICA
                    ),
                )
            } else {
                val e = Exception("Error reading file version for $file: $version")
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("Utils", "Error reading file version for $file: $version")
            }
        } else {
            Log.e("Utils", "Failed to process countries file.")
        }
        return mutableMapOf()
    }

    @JvmStatic
    fun saveVisitedCountriesToFile(visitedCountries: MutableMap<Continent, Set<String>>, resources: Resources, context: Context){
        Log.d("SaveUtils", "Saving visited countries to file")
        val visitedObject = JSONObject()
        visitedObject.put("Version", "1.0.0")
        Continent.basicContinents.forEach { continent ->
            visitedObject.put(continent.value, Gson().toJson(visitedCountries[continent]))
        }
        writeToFile(
            resources.getString(R.string.countries_file),
            visitedObject.toString(),
            context
        )
        Utils.checkEasterEggs(context, resources, visitedCountries)
    }

    @JvmStatic
    fun saveActiveSettingsToFile(activeSettings: MutableMap<Continent, Set<String>>, resources: Resources, context: Context){
        Log.d("SaveUtils", "Saving settings to file")
        val settingsObject = JSONObject()
        settingsObject.put("Version", "1.0.0")
        Continent.basicContinents.forEach{ continent ->
            val settings = Gson().toJson(activeSettings[continent])
            settingsObject.put(continent.value, settings)
        }
        writeToFile(resources.getString(R.string.settings_file), settingsObject.toString(), context)
    }
}