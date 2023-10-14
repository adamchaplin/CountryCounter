package uk.co.adamchaplin.countrycounter.model

import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.adapter.CustomExpandableTextSwitchAdapter

data class ExpandableListData(
    val continent: Continent,
    var type:Int = CustomExpandableTextSwitchAdapter.PARENT,
    var subList : List<String> = ArrayList(),
    var isExpanded:Boolean = false
)
