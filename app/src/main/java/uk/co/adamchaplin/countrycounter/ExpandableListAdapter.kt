package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import uk.co.adamchaplin.countrycounter.databinding.ListviewSwitchGroupBinding
import uk.co.adamchaplin.countrycounter.databinding.ListviewSwitchItemBinding

class ExpandableListAdapter(
    private val _context: Context,
    private val _listDataHeader: List<String>,
    private val _listDataChild: HashMap<String, List<String>>,
    private val mainActivity: MainActivity
) : BaseExpandableListAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(_context)
    private lateinit var childBinding: ListviewSwitchItemBinding
    private lateinit var parentBinding: ListviewSwitchGroupBinding

    override fun getChild(groupPosition: Int, childPosition: Int): String {
        return _listDataChild[_listDataHeader[groupPosition]]!![childPosition]
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return try {
            _listDataChild[_listDataHeader[groupPosition]]!!.size
        } catch (e: KotlinNullPointerException){
            FirebaseCrashlytics.getInstance().recordException(e)
            0
        }
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        var tempView = convertView
        val holder: ItemViewHolder
        val parentText = getGroup(groupPosition)
        val childText = getChild(groupPosition, childPosition)
        if (tempView == null) {
            childBinding = ListviewSwitchItemBinding.inflate(inflater)
            tempView = childBinding.root
            holder = ItemViewHolder()
            holder.switch = childBinding.itemSwitch
            tempView.tag = holder
        } else {
            holder = tempView.tag as ItemViewHolder
        }
        holder.switch.text = childText
        when(getGroup(groupPosition)) {
            _context.getString(R.string.africa_title) -> holder.switch.isChecked = childText in mainActivity.africanSettings
            _context.getString(R.string.antarctica_title) -> holder.switch.isChecked = childText in mainActivity.antarcticaSettings
            _context.getString(R.string.asia_title) -> holder.switch.isChecked = childText in mainActivity.asianSettings
            _context.getString(R.string.europe_title) -> holder.switch.isChecked = childText in mainActivity.europeanSettings
            _context.getString(R.string.north_america_title) -> holder.switch.isChecked = childText in mainActivity.northAmericanSettings
            _context.getString(R.string.oceania_title) -> holder.switch.isChecked = childText in mainActivity.oceanianSettings
            _context.getString(R.string.south_america_title) -> holder.switch.isChecked = childText in mainActivity.southAmericanSettings
        }
        setSwitchColour(groupPosition, holder.switch)
        holder.switch.setOnClickListener { buttonView ->
            clickListener(
                parentText,
                childText,
                buttonView as CompoundButton
            )
        }
        return tempView

    }

    override fun getGroup(groupPosition: Int): String {
        return _listDataHeader[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return _listDataHeader.size
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View {
        var tempView = convertView
        val holder: GroupViewHolder

        val headerTitle = getGroup(groupPosition)
        if (tempView == null) {
            parentBinding = ListviewSwitchGroupBinding.inflate(inflater)
            tempView = parentBinding.root
            holder = GroupViewHolder()
            holder.title = parentBinding.groupText
            holder.arrow = parentBinding.groupArrow
            tempView.tag = holder
        } else {
            holder = tempView.tag as GroupViewHolder
        }

        setArrowState(parent as ExpandableListView, holder, groupPosition)

        holder.title.text = headerTitle
        setTextViewColour(groupPosition, holder)

        return tempView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    private fun clickListener(parentText: String, childText: String, buttonView: CompoundButton) {
        val checked = buttonView.isChecked
        if(checked) {
            when(parentText) {
                _context.getString(R.string.africa_title) ->
                    if(childText !in mainActivity.africanSettings){
                        mainActivity.africanSettings.add(childText)
                        mainActivity.totalAfricanCountries++
                    }
                _context.getString(R.string.antarctica_title) ->
                    if(childText !in mainActivity.antarcticaSettings) {
                        mainActivity.antarcticaSettings.add(childText)
                        mainActivity.totalAntarcticaCountries++
                    }
                _context.getString(R.string.asia_title) ->
                    if(childText !in mainActivity.asianSettings) {
                        mainActivity.asianSettings.add(childText)
                        mainActivity.totalAsianCountries++
                    }
                _context.getString(R.string.europe_title) ->
                    if(childText !in mainActivity.europeanSettings) {
                        mainActivity.europeanSettings.add(childText)
                        mainActivity.totalEuropeanCountries++
                        if(childText == _context.getString(R.string.split_uk)){
                            if(_context.getString(R.string.internal_uk) in mainActivity.visitedEuropeanCountries) {
                                mainActivity.visitedEuropeanCountries.remove(_context.getString(R.string.internal_uk))
                                for (j in mainActivity.states) {
                                    if (_context.getString(R.string.internal_uk) in j.continentName) {
                                        mainActivity.visitedEuropeanCountries.add(j.countryName)
                                    }
                                }
                            }
                            mainActivity.totalEuropeanCountries += 2
                        }
                    }
                _context.getString(R.string.north_america_title) ->
                    if(childText !in mainActivity.northAmericanSettings) {
                        mainActivity.northAmericanSettings.add(childText)
                        mainActivity.totalNorthAmericanCountries++
                    }
                _context.getString(R.string.oceania_title) ->
                    if(childText !in mainActivity.oceanianSettings) {
                        mainActivity.oceanianSettings.add(childText)
                        mainActivity.totalOceanianCountries++
                    }
                _context.getString(R.string.south_america_title) ->
                    if(childText !in mainActivity.southAmericanSettings) {
                        mainActivity.southAmericanSettings.add(childText)
                        mainActivity.totalSouthAmericanCountries++
                    }
            }
        } else if (!checked){
            when(parentText) {
                _context.getString(R.string.africa_title) ->
                    if(childText in mainActivity.africanSettings){
                        mainActivity.africanSettings.remove(childText)
                        mainActivity.visitedAfricanCountries.remove(childText)
                        mainActivity.totalAfricanCountries--
                    }
                _context.getString(R.string.antarctica_title) ->
                    if(childText in mainActivity.antarcticaSettings) {
                        mainActivity.antarcticaSettings.remove(childText)
                        mainActivity.visitedAntarcticaCountries.remove(childText)
                        mainActivity.totalAntarcticaCountries--
                    }
                _context.getString(R.string.asia_title) ->
                    if(childText in mainActivity.asianSettings) {
                        mainActivity.asianSettings.remove(childText)
                        mainActivity.visitedAsianCountries.remove(childText)
                        mainActivity.totalAsianCountries--
                    }
                _context.getString(R.string.europe_title) ->
                    if(childText in mainActivity.europeanSettings) {
                        mainActivity.europeanSettings.remove(childText)
                        mainActivity.visitedEuropeanCountries.remove(childText)
                        mainActivity.totalEuropeanCountries--
                        if(childText == _context.getString(R.string.split_uk)){
                            val states: MutableList<String> = mutableListOf()
                            for (j in mainActivity.states) {
                                if (_context.getString(R.string.internal_uk) in j.continentName) {
                                    states.add(j.countryName)
                                }
                            }
                            if(mainActivity.visitedEuropeanCountries.containsAll(states)) {
                                mainActivity.visitedEuropeanCountries.add(_context.getString(R.string.internal_uk))
                            }
                            mainActivity.visitedEuropeanCountries.removeAll(states.toSet())
                            mainActivity.totalEuropeanCountries -= 2
                        }
                    }
                _context.getString(R.string.north_america_title) ->
                    if(childText in mainActivity.northAmericanSettings) {
                        mainActivity.northAmericanSettings.remove(childText)
                        mainActivity.visitedNorthAmericanCountries.remove(childText)
                        mainActivity.totalNorthAmericanCountries--
                    }
                _context.getString(R.string.oceania_title) ->
                    if(childText in mainActivity.oceanianSettings) {
                        mainActivity.oceanianSettings.remove(childText)
                        mainActivity.visitedOceanianCountries.remove(childText)
                        mainActivity.totalOceanianCountries--
                    }
                _context.getString(R.string.south_america_title) ->
                    if(childText in mainActivity.southAmericanSettings) {
                        mainActivity.southAmericanSettings.remove(childText)
                        mainActivity.visitedSouthAmericanCountries.remove(childText)
                        mainActivity.totalSouthAmericanCountries--
                    }
            }
        }
        mainActivity.dashboardFrag.updateStats()
        mainActivity.countriesFrag.refreshData()
        mainActivity.saveVisitedCountries()
        mainActivity.saveSettings()
    }

    private fun setArrowState(
        parent: ExpandableListView,
        groupViewHolder: GroupViewHolder,
        groupPosition: Int
    ) {
        if(getChildrenCount(groupPosition) > 0) {
            groupViewHolder.arrow.visibility = View.VISIBLE
            if (parent.isGroupExpanded(groupPosition)) {
                groupViewHolder.arrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            } else {
                groupViewHolder.arrow.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp)
            }
        } else {
            groupViewHolder.arrow.visibility = View.INVISIBLE
        }
    }

    private fun setSwitchColour(groupPosition: Int, switch: SwitchCompat){
        val colour: Int = Utils.getColourFromContinent(mainActivity.resources, getGroup(groupPosition))
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                mainActivity.resources.getColor(R.color.pastelRed, null),
                mainActivity.resources.getColor(R.color.pastelGreen, null)
            )
        )
        switch.trackTintList = colorStateList
        switch.thumbTintList = colorStateList
        switch.setTextColor(colour)
    }

    private fun setTextViewColour(groupPosition: Int, holder: GroupViewHolder){
        val colour: Int = Utils.getColourFromContinent(mainActivity.resources, getGroup(groupPosition))
        holder.title.setTextColor(colour)
        holder.arrow.setColorFilter(colour)
    }

    inner class ItemViewHolder {
        internal var switch: SwitchCompat = SwitchCompat( _context)
    }

    inner class GroupViewHolder {
        internal var title: TextView = TextView(_context)
        internal var arrow: ImageView = ImageView(_context)
    }


}