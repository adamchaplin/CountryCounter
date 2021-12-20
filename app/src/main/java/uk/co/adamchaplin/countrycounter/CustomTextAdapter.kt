package uk.co.adamchaplin.countrycounter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import uk.co.adamchaplin.countrycounter.databinding.ListviewItemBinding

class CustomTextAdapter(
    private val context: Context,
    private val listValues: ArrayList<String>,
    private val continent: String
) : BaseAdapter() {

    private lateinit var binding: ListviewItemBinding

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getCount(): Int {
        return listValues.size
    }

    override fun getItem(position: Int): String {
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
            binding = ListviewItemBinding.inflate(inflater)
            tempView = binding.root
        }
        val tempColour = Utils.getColourFromContinent(context.resources, continent)
        binding.textView.text = getItem(position)
        binding.textView.setTextColor(tempColour)

        return tempView
    }
}