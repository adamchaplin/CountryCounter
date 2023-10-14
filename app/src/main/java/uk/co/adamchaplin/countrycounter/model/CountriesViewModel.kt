package uk.co.adamchaplin.countrycounter.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import uk.co.adamchaplin.countrycounter.Continent

class CountriesViewModel : ViewModel() {
    val visitedCountries: MutableLiveData<MutableMap<Continent, Set<String>>> = MutableLiveData<MutableMap<Continent, Set<String>>>(mutableMapOf())
    val allCountries: MutableLiveData<MutableMap<Continent, Set<String>>> = MutableLiveData<MutableMap<Continent, Set<String>>>(mutableMapOf())
    private val activeSettings: MutableLiveData<MutableMap<Continent, Set<String>>> = MutableLiveData<MutableMap<Continent, Set<String>>>(mutableMapOf())
    private val allSettings: MutableLiveData<MutableMap<Continent, Set<String>>> = MutableLiveData<MutableMap<Continent, Set<String>>>(mutableMapOf())

    fun getVisitedCountries(): MutableMap<Continent, Set<String>> {
        return visitedCountries.value ?: mutableMapOf()
    }

    fun getVisitedCountriesForContinent(continent: Continent): Set<String> {
        return visitedCountries.value?.get(continent) ?: setOf()
    }

    fun getVisitedCountriesFlattened(): Set<String> {
        return visitedCountries.value?.values?.flatten()?.toSet() ?: setOf()
    }

    fun getVisitedContinents(): Set<String> {
        return visitedCountries.value?.flatMap{ (continent, countries) ->
            if(countries.isNotEmpty()) {
                setOf(continent.value)
            } else {
                setOf()
            }
        }?.toSet() ?: setOf()
    }

    fun setVisitedCountries(countries: MutableMap<Continent, Set<String>>) {
        visitedCountries.value = countries
    }

    fun setVisitedCountriesForContinent(continent: Continent, countries: Set<String>) {
        visitedCountries.value?.set(continent, countries)
        visitedCountries.value = visitedCountries.value
    }

    fun getAllCountriesForContinent(continent: Continent): Set<String> {
        return allCountries.value?.get(continent) ?: setOf()
    }

    fun getAllCountriesFlattened(): Set<String> {
        return allCountries.value?.values?.flatten()?.toSet() ?: setOf()
    }

    fun setAllCountriesForContinent(continent: Continent, countries: Set<String>) {
        allCountries.value?.set(continent, countries)
        allCountries.value = allCountries.value
    }

    fun setAllCountries(countries: MutableMap<Continent, Set<String>>) {
        allCountries.value = countries
    }

    fun addCountryToAllCountriesForContinent(continent: Continent, country: String) {
        val currentCountries = getAllCountriesForContinent(continent)
        setAllCountriesForContinent(continent, (currentCountries + country).sorted().toSet())
    }

    fun removeCountryFromAllCountriesForContinent(continent: Continent, country: String) {
        val currentCountries = getAllCountriesForContinent(continent)
        setAllCountriesForContinent(continent, currentCountries - country)
    }

    fun getActiveSettings(): MutableMap<Continent, Set<String>> {
        return activeSettings.value ?: mutableMapOf()
    }

    fun getActiveSettingsForContinent(continent: Continent): Set<String> {
        return activeSettings.value?.get(continent) ?: setOf()
    }

    fun setActiveSettingsForContinent(continent: Continent, settings: Set<String>) {
        activeSettings.value?.set(continent, settings)
        activeSettings.value = activeSettings.value
    }

    fun setActiveSettings(settings: MutableMap<Continent, Set<String>>) {
        activeSettings.value = settings
    }

    fun getAllSettingsForContinent(continent: Continent): Set<String> {
        return allSettings.value?.get(continent) ?: setOf()
    }

    fun setAllSettings(settings: MutableMap<Continent, Set<String>>) {
        allSettings.value = settings
    }

}