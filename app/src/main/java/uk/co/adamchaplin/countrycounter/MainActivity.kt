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
import androidx.activity.viewModels
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
import uk.co.adamchaplin.countrycounter.Continent.Companion.basicContinents
import uk.co.adamchaplin.countrycounter.adapter.ViewPagerAdapter
import uk.co.adamchaplin.countrycounter.dao.CountryDao
import uk.co.adamchaplin.countrycounter.utils.JsonUtils.extractSettingsFromJson
import uk.co.adamchaplin.countrycounter.databinding.ActivityMainBinding
import uk.co.adamchaplin.countrycounter.databinding.ViewPrivacyPolicyBinding
import uk.co.adamchaplin.countrycounter.databinding.HelperEditBinding
import uk.co.adamchaplin.countrycounter.databinding.HelperSwipeBinding
import uk.co.adamchaplin.countrycounter.databinding.HelperSettingsBinding
import uk.co.adamchaplin.countrycounter.fragment.CountriesFragment
import uk.co.adamchaplin.countrycounter.fragment.DashboardFragment
import uk.co.adamchaplin.countrycounter.fragment.SettingsFragment
import uk.co.adamchaplin.countrycounter.utils.FileUtils
import uk.co.adamchaplin.countrycounter.utils.Utils
import uk.co.adamchaplin.countrycounter.model.CountriesViewModel
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val countries: MutableList<CountryDao> = mutableListOf()
    private val countriesFrag: CountriesFragment = CountriesFragment()
    private val dashboardFrag: DashboardFragment = DashboardFragment()
    private val settingsFrag: SettingsFragment = SettingsFragment()
    private var introSwipe: Boolean = false
    private var introEdit: Boolean = false
    private var introSettings: Boolean = false
    private lateinit var introSharedPref: SharedPreferences
    private var shortAnimationDuration : Int = 0
    private val countriesViewModel: CountriesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity","Create MainActivity")
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
        Log.d("MainActivity","Pause MainActivity")
        findViewById<AdView>(R.id.ad_view)?.pause()
        super.onPause()
    }

    public override fun onStart() {
        Log.d("MainActivity","Start MainActivity")
        super.onStart()
        refreshAllCountriesList()
    }

    public override fun onResume() {
        Log.d("MainActivity","Resume MainActivity")
        super.onResume()
        importVisitedCountries(false)
        findViewById<AdView>(R.id.ad_view)?.resume()
    }

    public override fun onDestroy() {
        Log.d("MainActivity","Destroy MainActivity")
        findViewById<AdView>(R.id.ad_view)?.destroy()
        super.onDestroy()
    }

    private fun getCountryData(){
        Log.d("MainActivity", "Getting initial country data")
        val stream: InputStream = resources.openRawResource(R.raw.continents)
        readCSV(stream, countries, "country")
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

    private fun displayPrivacyPolicy(mainLayout: FrameLayout, otherSharedPref: SharedPreferences) {
        val completePp = otherSharedPref.getBoolean(getString(R.string.privacy_policy_accepted), false)

        if(!completePp) {
            val inflater = LayoutInflater.from(applicationContext)
            val privacyPolicyBinding = ViewPrivacyPolicyBinding.inflate(inflater)
            val childLayout = privacyPolicyBinding.root
            privacyPolicyBinding.policyText.text = Utils.getStringFromResource(resources, R.raw.privacy_policy)
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
                startMainApp(mainLayout, otherSharedPref)
            }
            mainLayout.addView(childLayout)
        } else {
            checkAndCreateFile(resources.getString(R.string.countries_file))
            checkAndCreateFile(resources.getString(R.string.settings_file))
            startMainApp(mainLayout, otherSharedPref)
        }

    }

    private fun checkAndCreateFile(fileName: String) {
        if(!applicationContext.getFileStreamPath(fileName).exists()) {
            FileUtils.writeToFile(fileName, "", applicationContext)
            if(fileName == resources.getString(R.string.settings_file)) {
                getDefaultSettings()
                FileUtils.saveActiveSettingsToFile(countriesViewModel.getActiveSettings(), resources, this)
            }
        }
    }

    private fun getDefaultSettings() {
        val defaultSettings = resources.openRawResource(R.raw.default_settings).bufferedReader().use { it.readText() }
        val defaultSettingsJson : JsonObject? = Gson().fromJson(defaultSettings, JsonObject::class.java)
        if (defaultSettingsJson != null) {
            countriesViewModel.setActiveSettings(extractSettingsFromJson(defaultSettingsJson))
        }
    }

    private fun startMainApp(mainLayout: FrameLayout, otherSharedPref: SharedPreferences){
        importSettings()
        refreshAllCountriesList()
        importVisitedCountries(true)
        setupTabs(mainLayout)
        setupAdsAndAnalytics(otherSharedPref)
    }

    private fun importSettings(){
        Log.d("MainActivity", "Importing settings from file")
        val settings = FileUtils.getSettingsFromFile(resources.getString(R.string.settings_file), this)
        countriesViewModel.setActiveSettings(settings)
    }

    private fun refreshAllCountriesList(){
        Log.d("MainActivity", "Refreshing 'allCountries' list")
        val allCountries: MutableMap<Continent, Set<String>> = mutableMapOf()
        val allSettings: MutableMap<Continent, Set<String>> = mutableMapOf()
        countries.forEach { country ->
            basicContinents.forEach { continent ->
                updateAllCountries(country, continent, allCountries)
                updateAllSettings(country, continent, allSettings)
            }
        }
        countriesViewModel.setAllCountries(allCountries)
        countriesViewModel.setAllSettings(allSettings)
    }

    private fun updateAllCountries(country: CountryDao, continent: Continent, allCountries: MutableMap<Continent, Set<String>>) {
        val countrySettings: Set<String> = countriesViewModel.getActiveSettingsForContinent(continent)
        if (country.continentName == continent.value || country.countryName in countrySettings) {
            allCountries[continent] = allCountries[continent]?.plus(country.countryName) ?: setOf(country.countryName)
        }
    }

    private fun updateAllSettings(country: CountryDao, continent: Continent, allSettings: MutableMap<Continent, Set<String>>) {
        if (country.continentName != continent.value && country.continentName.contains(continent.value)) {
            allSettings[continent] = allSettings[continent]?.plus(country.countryName) ?: setOf(country.countryName)
        }
    }

    private fun importVisitedCountries(doValidate: Boolean){
        Log.d("MainActivity", "importing visited countries from file")
        val visitedCountries = FileUtils.getVisitedCountriesFromFile(resources.getString(R.string.countries_file), this)
        countriesViewModel.setVisitedCountries(visitedCountries)
        if(doValidate){
            validateVisited()
        }
    }

    private fun validateVisited(){
        Log.d("MainActivity", "Validating visited countries")
        var unknownFound = false
        val validatedVisitedCountries = basicContinents.associateWith { continent ->
            val unknownCountries = getUnknownCountriesFromVisited(continent)
            if (unknownCountries.isNotEmpty()) {
                unknownFound = true
            }
            countriesViewModel.getVisitedCountries()[continent]?.subtract(unknownCountries) ?: setOf()
        }
        if (unknownFound) {
            countriesViewModel.setVisitedCountries(validatedVisitedCountries.toMutableMap())
            FileUtils.saveVisitedCountriesToFile(countriesViewModel.getVisitedCountries(), resources, this)
        }
    }

    private fun getUnknownCountriesFromVisited(continent: Continent): Set<String>{
        val visitedCountries: Set<String> = countriesViewModel.getVisitedCountriesForContinent(continent)
        val allCountries: Set<String> = countriesViewModel.getAllCountriesForContinent(continent)
        val unknownCountriesList: Set<String> = visitedCountries.map { countryName ->
            if (allCountries.find {it == countryName} == null) {
                val e = Exception("Unknown country found in continent $continent: $countryName")
                FirebaseCrashlytics.getInstance().recordException(e)
                setOf(countryName)
            } else {
                setOf()
            }
        }.flatten().toSet()

        return unknownCountriesList
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
            override fun onTabUnselected(tab: TabLayout.Tab) {}
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
        val stopAnalytics = true//otherSharedPref.getBoolean(getString(R.string.stop_analytics), false)
        val hideAds = true//otherSharedPref.getBoolean(getString(R.string.hide_ads), false)

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
