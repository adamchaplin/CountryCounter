package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import uk.co.adamchaplin.countrycounter.databinding.FragmentSettingsBinding

class SettingsFragment: Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var applicationContext: Context
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root
        applicationContext = activity as Context
        mainActivity = activity as MainActivity
        return view
    }

    override fun onStart() {
        super.onStart()
        refreshSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshSettings(){
        binding.scrollLayout.removeAllViews()
        val listDataHeader: MutableList<String> = mutableListOf(getString(R.string.africa_title), getString(R.string.antarctica_title), getString(R.string.asia_title), getString(R.string.europe_title), getString(R.string.north_america_title), getString(R.string.oceania_title), getString(R.string.south_america_title))
        val listDataChild: HashMap<String, List<String>> = hashMapOf()
        val afTemp: MutableList<String> = mutableListOf()
        val asTemp: MutableList<String> = mutableListOf()
        val euTemp: MutableList<String> = mutableListOf()
        val naTemp: MutableList<String> = mutableListOf()
        val ocTemp: MutableList<String> = mutableListOf()
        val saTemp: MutableList<String> = mutableListOf()
        val anTemp: MutableList<String> = mutableListOf()
        for (i in mainActivity.countries) {
            if (i.continentName != getString(R.string.internal_africa) && i.continentName.contains(getString(R.string.internal_africa))) {
                afTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_antarctica) && i.continentName.contains(getString(R.string.internal_antarctica))) {
                anTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_asia) && i.continentName.contains(getString(R.string.internal_asia))) {
                asTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_europe) && i.continentName.contains(getString(R.string.internal_europe))) {
                euTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_north_america) && i.continentName.contains(getString(R.string.internal_north_america))) {
                naTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_oceania) && i.continentName.contains(getString(R.string.internal_oceania))) {
                ocTemp.add(i.countryName)
            }
            if (i.continentName != getString(R.string.internal_south_america) && i.continentName.contains(getString(R.string.internal_south_america))) {
                saTemp.add(i.countryName)
            }
        }
//        val splitUkString = resources.getString(R.string.split_uk)
//        euTemp.add(splitUkString)
        listDataChild[getString(R.string.africa_title)] = afTemp
        listDataChild[getString(R.string.antarctica_title)] = anTemp
        listDataChild[getString(R.string.asia_title)] = asTemp
        listDataChild[getString(R.string.europe_title)] = euTemp
        listDataChild[getString(R.string.north_america_title)] = naTemp
        listDataChild[getString(R.string.oceania_title)] = ocTemp
        listDataChild[getString(R.string.south_america_title)] = saTemp
        addCollapsibleSettings(binding.scrollLayout, listDataHeader, listDataChild)
    }

    private fun addCollapsibleSettings(settingsLayout: LinearLayout, listDataHeader: List<String>, listDataChild: HashMap<String, List<String>>) {
        val listAdapter = ExpandableListAdapter(applicationContext, listDataHeader, listDataChild, mainActivity)
        val expandableListView = ExpandableListView(applicationContext)
        expandableListView.setGroupIndicator(null)
        expandableListView.setAdapter(listAdapter)
        expandableListView.setOnGroupExpandListener(object :
            ExpandableListView.OnGroupExpandListener {
            var previousGroup = -1
            override fun onGroupExpand(groupPosition: Int) {
                Log.i("Setting Expanded", groupPosition.toString())
                if (groupPosition != previousGroup) expandableListView.collapseGroup(previousGroup)
                previousGroup = groupPosition
            }
        })

        settingsLayout.addView(expandableListView)
    }
}