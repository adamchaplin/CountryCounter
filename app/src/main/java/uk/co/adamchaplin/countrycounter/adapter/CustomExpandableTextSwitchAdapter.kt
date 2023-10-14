package uk.co.adamchaplin.countrycounter.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.R
import uk.co.adamchaplin.countrycounter.databinding.ListviewSwitchGroupBinding
import uk.co.adamchaplin.countrycounter.databinding.ListviewSwitchItemBinding
import uk.co.adamchaplin.countrycounter.model.CountriesViewModel
import uk.co.adamchaplin.countrycounter.model.ExpandableListData
import uk.co.adamchaplin.countrycounter.utils.Utils

class CustomExpandableTextSwitchAdapter(
    private val context: Context,
    private val data: MutableList<ExpandableListData>,
    private val countriesViewModel: CountriesViewModel,
    private val onClickListener: OnClickListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        const val PARENT = 0
        const val CHILD = 1
    }

    class ParentViewHolder(val item: ListviewSwitchGroupBinding) : RecyclerView.ViewHolder(item.root)

    class ChildViewHolder(val item: ListviewSwitchItemBinding) : RecyclerView.ViewHolder(item.root)

    interface OnClickListener {
        fun onClick(continent: Continent, card: ListviewSwitchItemBinding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == PARENT){
            val binding =  ListviewSwitchGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ParentViewHolder(binding)
        } else {
            val binding =  ListviewSwitchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ChildViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int = data[position].type

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val expandableListData = data[position]
        if (expandableListData.type == PARENT) {
            holder as ParentViewHolder
            val tempColour = Utils.getColourFromContinent(context.resources, expandableListData.continent)
            holder.item.groupText.text = expandableListData.continent.value
            holder.item.groupText.setTextColor(tempColour)
            holder.item.groupArrow.setColorFilter(tempColour)
            setArrowState(expandableListData, holder)
            holder.item.root.setOnClickListener {
                expandOrCollapseParentItem(expandableListData, position)
            }
        } else {
            holder as ChildViewHolder
            val tempContinent = expandableListData.continent
            holder.item.itemSwitch.text = expandableListData.subList.first()
            holder.item.itemSwitch.isChecked = expandableListData.subList.first() in countriesViewModel.getActiveSettingsForContinent(tempContinent)
            setSwitchColour(tempContinent, holder.item.itemSwitch)
            holder.item.itemSwitch.setOnClickListener{
                onClickListener.onClick(tempContinent, holder.item)
            }
        }
    }

    private fun expandOrCollapseParentItem(singleBoarding: ExpandableListData, position: Int) {
        if (singleBoarding.isExpanded) {
            collapseParentRow(position)
        } else {
            expandParentRow(position)
        }
    }

    private fun expandParentRow(position: Int){
        val currentBoardingRow = data[position]
        val children = currentBoardingRow.subList
        currentBoardingRow.isExpanded = true
        var nextPosition = position
        if(currentBoardingRow.type==PARENT){
            Log.d("ExpandableAdapter", "Setting Expanded: " + currentBoardingRow.continent.value)
            children.forEach { child ->
                val parentModel =  ExpandableListData(currentBoardingRow.continent)
                parentModel.type = CHILD
                val subList : ArrayList<String> = ArrayList()
                subList.add(child)
                parentModel.subList=subList
                data.add(++nextPosition,parentModel)
            }
            notifyDataSetChanged()
        }
    }

    private fun collapseParentRow(position: Int){
        val currentBoardingRow = data[position]
        val children = currentBoardingRow.subList
        data[position].isExpanded = false
        if(data[position].type==PARENT){
            Log.i("Setting Collapsed", currentBoardingRow.continent.value)
            children.forEach { _ ->
                data.removeAt(position + 1)
            }
            notifyDataSetChanged()
        }
    }

    private fun setSwitchColour(continent: Continent, switch: SwitchCompat){
        val colour: Int = Utils.getColourFromContinent(context.resources, continent)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                context.resources.getColor(R.color.pastelRed, null),
                context.resources.getColor(R.color.pastelGreen, null)
            )
        )
        switch.trackTintList = colorStateList
        switch.thumbTintList = colorStateList
        switch.setTextColor(colour)
    }

    private fun setArrowState(
        parent: ExpandableListData,
        parentViewHolder: ParentViewHolder
    ) {
        if(parent.subList.isNotEmpty()) {
            parentViewHolder.item.groupArrow.visibility = View.VISIBLE
            if (parent.isExpanded) {
                parentViewHolder.item.groupArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
            } else {
                parentViewHolder.item.groupArrow.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp)
            }
        } else {
            parentViewHolder.item.groupArrow.visibility = View.INVISIBLE
        }
    }

}
