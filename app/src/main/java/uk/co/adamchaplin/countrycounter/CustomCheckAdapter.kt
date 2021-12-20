package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import uk.co.adamchaplin.countrycounter.databinding.ListviewCheckboxItemBinding

class CustomCheckAdapter(
    private val context: Context,
    private val listValues: ArrayList<Country>
) : BaseAdapter() {

    private lateinit var binding: ListviewCheckboxItemBinding

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getCount(): Int {
        return listValues.size
    }

    override fun getItem(position: Int): Country {
        return listValues[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var tempView = convertView
        if (tempView == null) {
            val inflater = LayoutInflater.from(context)
            binding = ListviewCheckboxItemBinding.inflate(inflater)
            tempView = binding.root

        }
        val tempCountry = getItem(position)
        binding.checkedText.text = tempCountry.countryName
        binding.checkedText.isChecked = tempCountry.unsavedVisited
        binding.checkedText.setTextColor(tempCountry.colour)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                tempCountry.colour,
                tempCountry.colour
            )
        )
        binding.checkedText.checkMarkTintList = colorStateList

        return tempView
    }
}