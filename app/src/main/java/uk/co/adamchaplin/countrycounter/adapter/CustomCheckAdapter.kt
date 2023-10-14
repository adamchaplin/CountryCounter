package uk.co.adamchaplin.countrycounter.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.adamchaplin.countrycounter.databinding.RecyclerViewCheckboxItemBinding

class CustomCheckAdapter(
    private val listValues: ArrayList<String>,
    private val visitedList: Set<String>,
    private val colour: Int,
    private val onClickListener: OnClickListener
): RecyclerView.Adapter<CustomCheckAdapter.ViewHolder>() {

    class ViewHolder(val item: RecyclerViewCheckboxItemBinding) : RecyclerView.ViewHolder(item.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  RecyclerViewCheckboxItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    interface OnClickListener {
        fun onClick(card: RecyclerViewCheckboxItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tempCountry = listValues[position]
        holder.item.checkedText.text = tempCountry
        holder.item.checkedText.isChecked = visitedList.contains(tempCountry)
        holder.item.checkedText.setTextColor(colour)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                colour,
                colour
            )
        )
        holder.item.checkedText.checkMarkTintList = colorStateList
        holder.item.root.setOnClickListener {
            onClickListener.onClick(holder.item)
        }
    }

    override fun getItemCount(): Int {
        return listValues.size
    }
}