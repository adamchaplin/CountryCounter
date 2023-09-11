package uk.co.adamchaplin.countrycounter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import uk.co.adamchaplin.countrycounter.Utils.getContinentSettings
import uk.co.adamchaplin.countrycounter.Utils.getContinentVisitedCountries
import uk.co.adamchaplin.countrycounter.databinding.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val countries: MutableList<CountryDao> = mutableListOf()
    val states: MutableList<CountryDao> = mutableListOf()
    var visitedAfricanCountries: MutableSet<String> = mutableSetOf()
    var visitedAntarcticaCountries: MutableSet<String> = mutableSetOf()
    var visitedAsianCountries: MutableSet<String> = mutableSetOf()
    var visitedEuropeanCountries: MutableSet<String> = mutableSetOf()
    var visitedNorthAmericanCountries: MutableSet<String> = mutableSetOf()
    var visitedOceanianCountries: MutableSet<String> = mutableSetOf()
    var visitedSouthAmericanCountries: MutableSet<String> = mutableSetOf()
    var visitedContinents: MutableSet<String> = mutableSetOf()
    var totalAfricanCountries: Int = 54
    var totalAntarcticaCountries: Int = 1
    var totalAsianCountries: Int = 41
    var totalEuropeanCountries: Int = 42
    var totalNorthAmericanCountries: Int = 23
    var totalOceanianCountries: Int = 14
    var totalSouthAmericanCountries: Int = 12
    var africanSettings: MutableSet<String> = mutableSetOf()
    var antarcticaSettings: MutableSet<String> = mutableSetOf()
    var asianSettings: MutableSet<String> = mutableSetOf()
    var europeanSettings: MutableSet<String> = mutableSetOf()
    var northAmericanSettings: MutableSet<String> = mutableSetOf()
    var oceanianSettings: MutableSet<String> = mutableSetOf()
    var southAmericanSettings: MutableSet<String> = mutableSetOf()
    var africanCountries: MutableSet<Country> = mutableSetOf()
    var antarcticaCountries: MutableSet<Country> = mutableSetOf()
    var asianCountries: MutableSet<Country> = mutableSetOf()
    var europeanCountries: MutableSet<Country> = mutableSetOf()
    var northAmericanCountries: MutableSet<Country> = mutableSetOf()
    var oceanianCountries: MutableSet<Country> = mutableSetOf()
    var southAmericanCountries: MutableSet<Country> = mutableSetOf()
    var countriesFrag: CountriesFragment = CountriesFragment()
    var dashboardFrag: DashboardFragment = DashboardFragment()
    private var settingsFrag: SettingsFragment = SettingsFragment()
    private var introSwipe: Boolean = false
    private var introEdit: Boolean = false
    private var introSettings: Boolean = false
    private lateinit var introSharedPref: SharedPreferences
    private var shortAnimationDuration : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.hide()

        getCountryData()
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        introSharedPref = applicationContext.getSharedPreferences(getString(R.string.intro_file),Context.MODE_PRIVATE)
        val otherSharedPref = applicationContext.getSharedPreferences(getString(R.string.other_prefs_file),Context.MODE_PRIVATE)

        introSwipe = introSharedPref.getBoolean(getString(R.string.intro_dashboard_swipe), false)
        introEdit = introSharedPref.getBoolean(getString(R.string.intro_countries_edit), false)
        introSettings = introSharedPref.getBoolean(getString(R.string.intro_settings), false)

        displayPrivacyPolicy(view, otherSharedPref)
    }

    public override fun onPause() {
        findViewById<AdView>(R.id.ad_view)?.pause()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        findViewById<AdView>(R.id.ad_view)?.resume()
    }

    public override fun onDestroy() {
        findViewById<AdView>(R.id.ad_view)?.destroy()
        super.onDestroy()
    }

    private fun displayPrivacyPolicy(mainLayout: FrameLayout, otherSharedPref: SharedPreferences) {
        val completePp = otherSharedPref.getBoolean(getString(R.string.privacy_policy_accepted), false)

        if(!completePp) {
            val inflater = LayoutInflater.from(applicationContext)
            val privacyPolicyBinding = ViewPrivacyPolicyBinding.inflate(inflater)
            val childLayout = privacyPolicyBinding.root
            privacyPolicyBinding.policyText.text = Utils.loadStringFromAsset(resources, R.raw.privacy_policy)
            privacyPolicyBinding.policyButton.setOnClickListener {
                childLayout.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainLayout.removeView(childLayout)
                        }
                    })
                with(otherSharedPref.edit()) {
                    putBoolean(resources.getString(R.string.privacy_policy_accepted), true)
                    apply()
                }
                checkAndCreateFile(resources.getString(R.string.countries_file))
                checkAndCreateFile(resources.getString(R.string.settings_file))
                resetAndLoad()
                setupTabs(mainLayout)
                setupAdsAndAnalytics(otherSharedPref)
            }
            mainLayout.addView(childLayout)
        } else {
            resetAndLoad()
            setupTabs(mainLayout)
            setupAdsAndAnalytics(otherSharedPref)
        }

    }

    private fun checkAndCreateFile(fileName: String) {
        if(!applicationContext.getFileStreamPath(fileName).exists()) {
            Utils.writeToFile(fileName, "", applicationContext)
            if(fileName == resources.getString(R.string.settings_file)) {
                getDefaultSettings()
                saveSettings()
            }
        }
    }

    private fun getDefaultSettings() {
        val defaultSettings = resources.openRawResource(R.raw.default_settings).bufferedReader().use { it.readText() }
        val defaultSettingsJson : JsonObject? = Gson().fromJson(defaultSettings, JsonObject::class.java)
        if (defaultSettingsJson != null) {
            africanSettings =
                getContinentSettings(defaultSettingsJson, getString(R.string.internal_africa))
            totalAfricanCountries += africanSettings.size
            antarcticaSettings =
                getContinentSettings(defaultSettingsJson, getString(R.string.internal_antarctica))
            totalAntarcticaCountries += antarcticaSettings.size
            asianSettings =
                getContinentSettings(defaultSettingsJson, getString(R.string.internal_asia))
            totalAsianCountries += asianSettings.size
            europeanSettings =
                getContinentSettings(defaultSettingsJson, getString(R.string.internal_europe))
            totalEuropeanCountries += europeanSettings.size
            northAmericanSettings = getContinentSettings(
                defaultSettingsJson,
                getString(R.string.internal_north_america)
            )
            totalNorthAmericanCountries += northAmericanSettings.size
            oceanianSettings =
                getContinentSettings(defaultSettingsJson, getString(R.string.internal_oceania))
            totalOceanianCountries += oceanianSettings.size
            southAmericanSettings = getContinentSettings(
                defaultSettingsJson,
                getString(R.string.internal_south_america)
            )
            totalSouthAmericanCountries += southAmericanSettings.size
        }
    }

    private fun resetAndLoad(){
        totalAfricanCountries = 54
        totalAntarcticaCountries = 1
        totalAsianCountries = 41
        totalEuropeanCountries = 42
        totalNorthAmericanCountries = 23
        totalOceanianCountries = 14
        totalSouthAmericanCountries = 12
        importSettings()
        importVisitedCountries(true)
    }

    private fun setupTabs(view: FrameLayout) {
        binding.tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when(tab.position){
                    0 -> {
                        binding.tabs.getTabAt(0)?.text = getString(R.string.tab_0)
                        binding.tabs.getTabAt(1)?.text = ""
                        binding.tabs.getTabAt(2)?.text = ""
                        introEdit(view)
                    }
                    1 -> {
                        binding.tabs.getTabAt(0)?.text = ""
                        binding.tabs.getTabAt(1)?.text = getString(R.string.tab_1)
                        binding.tabs.getTabAt(2)?.text = ""
                        introSwipe(view)
                    }
                    2 -> {
                        binding.tabs.getTabAt(0)?.text = ""
                        binding.tabs.getTabAt(1)?.text = ""
                        binding.tabs.getTabAt(2)?.text = getString(R.string.tab_2)
                        introSettings(view)
                    }
                    else -> {
                        val e = Exception("Unknown tab selected: ${tab.position}")
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }

                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                if(tab.position == 0){
                    importVisitedCountries(false)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val temp = (((binding.viewPager.width * position + positionOffsetPixels) * computeFactor()).toInt())
                binding.scrollView.scrollTo(
                    temp,
                    0
                )
            }
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            private fun computeFactor(): Float {
                return (binding.background.width - binding.viewPager.width) /
                        (binding.viewPager.width * ((binding.viewPager.adapter as ViewPagerAdapter)
                            .itemCount - 1)).toFloat()
            }
        })
        binding.viewPager.adapter = ViewPagerAdapter(this, arrayListOf(countriesFrag, dashboardFrag, settingsFrag))
        binding.viewPager.currentItem = 1
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            when(position){
                0 -> {
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_map_black_24dp, null)
                    tab.contentDescription = "Countries tab. Add or remove visited countries."
                }
                1 -> {
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_home_black_24dp, null)
                    tab.contentDescription = "Dashboard tab. View statistics on your visited countries."
                }
                2 -> {
                    tab.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_settings_black_24dp, null)
                    tab.contentDescription = "Settings tab. Adjust the countries list for each continent."
                }
                else -> {
                    val e = Exception("Unknown tab selected: $position")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

        }.attach()
    }

    private fun setupAdsAndAnalytics(otherSharedPref: SharedPreferences) {
        val stopAnalytics = otherSharedPref.getBoolean(getString(R.string.stop_analytics), false)
        val hideAds = otherSharedPref.getBoolean(getString(R.string.hide_ads), false)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!stopAnalytics)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !stopAnalytics

        if (!hideAds) {
            val adView = AdView(this)
            adView.setAdSize(AdSize.BANNER)
            //adView.adUnitId = "ca-app-pub-3940256099942544/6300978111" //Test ads
            adView.adUnitId = "ca-app-pub-9455047935746208/7089645310" //Real ads
            adView.id = R.id.ad_view
            val adViewLp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            adViewLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            adViewLp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            adView.layoutParams = adViewLp
            binding.relativeLayout.addView(adView)
            (binding.viewPager.layoutParams as RelativeLayout.LayoutParams).addRule(
                RelativeLayout.ABOVE,
                R.id.ad_view
            )
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun getCountryData(){
        var stream: InputStream = resources.openRawResource(R.raw.continents)
        readCSV(stream, countries, "country")

        stream = resources.openRawResource(R.raw.states)
        readCSV(stream, states, "state")
    }

    private fun readCSV(stream: InputStream, list: MutableList<CountryDao>, errorTag: String){
        val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
        try {
            run {
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",").toTypedArray()
                    when (tokens.size) {
                        2 -> list.add(CountryDao(tokens[0], tokens[1]))
                        3 -> list.add(CountryDao(tokens[0], tokens[1]))
                        else -> {
                            val e = Exception("Error reading $errorTag from file: ${tokens.joinToString(separator = ", ")}")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            Log.e("Main Activity", "Error reading $errorTag from file: ${tokens.joinToString(separator = ", ")}")
                        }
                    }
                    line = reader.readLine()
                }
                list.sort()
            }
        } catch (e: IOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("MainActivity", "Failed to get $errorTag list: $e")
        }
    }

    private fun importSettings(){
        val data = Utils.readFromFile(resources.getString(R.string.settings_file), this)
        if (!data.isNullOrEmpty()) {
            val settings = Gson().fromJson(data, JsonObject::class.java)
            val version = settings.get("Version").asString
            if(version == "1.0.0") {
                africanSettings = getContinentSettings(settings, getString(R.string.internal_africa))
                totalAfricanCountries += africanSettings.size
                antarcticaSettings = getContinentSettings(settings, getString(R.string.internal_antarctica))
                totalAntarcticaCountries += antarcticaSettings.size
                asianSettings = getContinentSettings(settings, getString(R.string.internal_asia))
                totalAsianCountries += asianSettings.size
                europeanSettings = getContinentSettings(settings, getString(R.string.internal_europe))
                totalEuropeanCountries += europeanSettings.size
//                if (getString(R.string.split_uk) in europeanSettings) {
//                    //totalEuropeanCountries += 2
//                    europeanSettings.remove(getString(R.string.split_uk))
//                    totalEuropeanCountries -= 1
//                }
                northAmericanSettings = getContinentSettings(settings, getString(R.string.internal_north_america))
                totalNorthAmericanCountries += northAmericanSettings.size
                oceanianSettings = getContinentSettings(settings, getString(R.string.internal_oceania))
                totalOceanianCountries += oceanianSettings.size
                southAmericanSettings = getContinentSettings(settings, getString(R.string.internal_south_america))
                totalSouthAmericanCountries += southAmericanSettings.size
            } else {
                val e = Exception("Error reading file version for ${resources.getString(R.string.settings_file)}: $version")
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("MainActivity", "Error reading file version for ${resources.getString(R.string.settings_file)}: $version")
            }
        } else {
            Log.e("MainActivity", "Failed to process settings file.")
        }
    }

    fun saveSettings(){
        val settingsObject = JSONObject()
        settingsObject.put("Version", "1.0.0")
        val afSettings = Gson().toJson(africanSettings)
        settingsObject.put(getString(R.string.internal_africa), afSettings)
        val anSettings = Gson().toJson(antarcticaSettings)
        settingsObject.put(getString(R.string.internal_antarctica), anSettings)
        val asSettings = Gson().toJson(asianSettings)
        settingsObject.put(getString(R.string.internal_asia), asSettings)
        val euSettings = Gson().toJson(europeanSettings)
        settingsObject.put(getString(R.string.internal_europe), euSettings)
        val naSettings = Gson().toJson(northAmericanSettings)
        settingsObject.put(getString(R.string.internal_north_america), naSettings)
        val ocSettings = Gson().toJson(oceanianSettings)
        settingsObject.put(getString(R.string.internal_oceania), ocSettings)
        val saSettings = Gson().toJson(southAmericanSettings)
        settingsObject.put(getString(R.string.internal_south_america), saSettings)
        Utils.writeToFile(resources.getString(R.string.settings_file), settingsObject.toString() , this)
    }

    fun importVisitedCountries(doValidate: Boolean){
        val data = Utils.readFromFile(resources.getString(R.string.countries_file), this)
        if (!data.isNullOrEmpty()) {
            val visited = Gson().fromJson(data, JsonObject::class.java)
            val version = visited.get("Version").asString
            if(version == "1.0.0") {
                visitedAfricanCountries = getContinentVisitedCountries(visited, getString(R.string.internal_africa))
                visitedAntarcticaCountries = getContinentVisitedCountries(visited, getString(R.string.internal_antarctica))
                visitedAsianCountries = getContinentVisitedCountries(visited, getString(R.string.internal_asia))
                visitedEuropeanCountries = getContinentVisitedCountries(visited, getString(R.string.internal_europe))
                visitedNorthAmericanCountries = getContinentVisitedCountries(visited, getString(R.string.internal_north_america))
                visitedOceanianCountries = getContinentVisitedCountries(visited, getString(R.string.internal_oceania))
                visitedSouthAmericanCountries = getContinentVisitedCountries(visited, getString(R.string.internal_south_america))
                visitedContinents = getVisitedContinentsList()
            } else {
                val e = Exception("Error reading file version for ${resources.getString(R.string.countries_file)}: $version")
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("MainActivity", "Error reading file version for ${resources.getString(R.string.countries_file)}: $version")
            }
        } else {
            Log.e("MainActivity", "Failed to process countries file.")
        }
        if(doValidate){
            validateVisited()
        }
    }

    private fun getVisitedContinentsList(): MutableSet<String> {
        val tempVisitedContinents: MutableSet<String> = mutableSetOf()
        if(visitedAfricanCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.africa_title))
        }
        if(visitedAntarcticaCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.antarctica_title))
        }
        if(visitedAsianCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.asia_title))
        }
        if(visitedEuropeanCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.europe_title))
        }
        if(visitedNorthAmericanCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.north_america_title))
        }
        if(visitedOceanianCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.oceania_title))
        }
        if(visitedSouthAmericanCountries.size > 0) {
            tempVisitedContinents.add(getString(R.string.south_america_title))
        }
        return tempVisitedContinents
    }

    fun saveVisitedCountries(){
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
    }

    private fun validateVisited(){
        refreshCountries()
        val changed1 = updateVisitedCountries(getString(R.string.internal_africa), visitedAfricanCountries, africanSettings, africanCountries)
        val changed2 = updateVisitedCountries(getString(R.string.internal_antarctica), visitedAntarcticaCountries, antarcticaSettings, antarcticaCountries)
        val changed3 = updateVisitedCountries(getString(R.string.internal_asia), visitedAsianCountries, asianSettings, asianCountries)
        val changed4 = updateVisitedCountries(getString(R.string.internal_europe), visitedEuropeanCountries, europeanSettings, europeanCountries)
        val changed5 = updateVisitedCountries(getString(R.string.internal_north_america), visitedNorthAmericanCountries, northAmericanSettings, northAmericanCountries)
        val changed6 = updateVisitedCountries(getString(R.string.internal_oceania), visitedOceanianCountries, oceanianSettings, oceanianCountries)
        val changed7 = updateVisitedCountries(getString(R.string.internal_south_america), visitedSouthAmericanCountries, southAmericanSettings, southAmericanCountries)
        if(changed1 || changed2 || changed3 || changed4 || changed5 || changed6 || changed7){
            saveVisitedCountries()
        }
    }

    private fun updateVisitedCountries(continentName: String, visitedCountries: MutableSet<String>, countrySettings: MutableSet<String>, countriesList: MutableSet<Country>): Boolean{
        val removeList = mutableSetOf<String>()
        var changed = false
        for (i in visitedCountries) {
            if (countriesList.find {it.countryName == i} == null && i !in countrySettings) {
                val e = Exception("Unknown country found in continent $continentName: $i")
                FirebaseCrashlytics.getInstance().recordException(e)
                changed = true
                removeList.add(i)
            }
        }
        visitedCountries.removeAll(removeList)
        return changed
    }

    fun refreshCountries(){
        africanCountries = mutableSetOf()
        antarcticaCountries = mutableSetOf()
        asianCountries = mutableSetOf()
        europeanCountries = mutableSetOf()
        northAmericanCountries = mutableSetOf()
        oceanianCountries = mutableSetOf()
        southAmericanCountries = mutableSetOf()
        for (i in countries) {
            checkAndAddCountry(i, getString(R.string.internal_africa), africanSettings, visitedAfricanCountries, africanCountries)
            checkAndAddCountry(i, getString(R.string.internal_antarctica), antarcticaSettings, visitedAntarcticaCountries, antarcticaCountries)
            checkAndAddCountry(i, getString(R.string.internal_asia), asianSettings, visitedAsianCountries, asianCountries)
            checkAndAddCountry(i, getString(R.string.internal_europe), europeanSettings, visitedEuropeanCountries, europeanCountries)
//            if (i.countryName == getString(R.string.internal_uk) && resources.getString(
//                    R.string.split_uk
//                ) in europeanSettings
//            ) {
//                val temp: MutableList<String> = mutableListOf()
//                for (j in states) {
//                    if (i.countryName in j.continentName) {
//                        temp.add(j.countryName)
//                    }
//                }
//            }
            checkAndAddCountry(i, getString(R.string.internal_north_america), northAmericanSettings, visitedNorthAmericanCountries, northAmericanCountries)
            checkAndAddCountry(i, getString(R.string.internal_oceania), oceanianSettings, visitedOceanianCountries, oceanianCountries)
            checkAndAddCountry(i, getString(R.string.internal_south_america), southAmericanSettings, visitedSouthAmericanCountries, southAmericanCountries)
        }
    }

    private fun checkAndAddCountry(country: CountryDao, continentToCheck: String, settingsToCheck: MutableSet<String>, visitedCountries: MutableSet<String>, countriesList: MutableSet<Country>){
        if (country.continentName == continentToCheck || country.countryName in settingsToCheck) {
            val visited = country.countryName in visitedCountries
            val tempCountry = Country(country.continentName, country.countryName, Utils.getColourFromContinent(resources, continentToCheck), visited)
            countriesList.add(tempCountry)
        }
    }

    private fun introEdit(mainLayout: FrameLayout){
        if(!introEdit) {
            val inflater = LayoutInflater.from(applicationContext)
            val introBinding = HelperEditBinding.inflate(inflater)
            val childLayout = introBinding.root

            introBinding.fullLayoutView.setOnClickListener {
            childLayout.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        mainLayout.removeView(childLayout)
                    }
                })
            with(introSharedPref.edit()) {
                putBoolean(getString(R.string.intro_countries_edit), true)
                apply()
            }
            introEdit = true
            }
            mainLayout.addView(childLayout)
        }
    }

    private fun introSwipe(mainLayout: FrameLayout){
        if(!introSwipe){
            val inflater = LayoutInflater.from(applicationContext)
            val introBinding = HelperSwipeBinding.inflate(inflater)
            val childLayout = introBinding.root

            introBinding.fullLayoutView.setOnClickListener {
                childLayout.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainLayout.removeView(childLayout)
                        }
                    })
                with (introSharedPref.edit()) {
                    putBoolean(getString(R.string.intro_dashboard_swipe), true)
                    apply()
                }
                introSwipe = true
            }
            mainLayout.addView(childLayout)
        }
    }

    private fun introSettings(mainLayout: FrameLayout) {
        if(!introSettings) {
            val inflater = LayoutInflater.from(applicationContext)
            val introBinding = HelperSettingsBinding.inflate(inflater)
            val childLayout = introBinding.root

            introBinding.fullLayoutView.setOnClickListener {
                childLayout.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            mainLayout.removeView(childLayout)
                        }
                    })
                with(introSharedPref.edit()) {
                    putBoolean(getString(R.string.intro_settings), true)
                    apply()
                }
                introSettings = true
            }
            mainLayout.addView(childLayout)
        }
    }
}
