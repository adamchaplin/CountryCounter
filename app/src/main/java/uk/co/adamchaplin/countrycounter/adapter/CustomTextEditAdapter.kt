package uk.co.adamchaplin.countrycounter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.adamchaplin.countrycounter.Continent
import uk.co.adamchaplin.countrycounter.databinding.RecyclerViewEditItemBinding
import uk.co.adamchaplin.countrycounter.utils.Utils

class CustomTextEditAdapter(
    private val context: Context,
    private val listValues: List<Continent>,
    private val onClickListener: OnClickListener
): RecyclerView.Adapter<CustomTextEditAdapter.ViewHolder>() {

    class ViewHolder(val item: RecyclerViewEditItemBinding) : RecyclerView.ViewHolder(item.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  RecyclerViewEditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    interface OnClickListener {
        fun onClick(continent: Continent, card: RecyclerViewEditItemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tempContinent = listValues[position]
        val tempColour = Utils.getColourFromContinent(context.resources, tempContinent)
        holder.item.textView.text = tempContinent.value
        holder.item.textView.setTextColor(tempColour)
        holder.item.continentEdit.setColorFilter(tempColour)
        holder.item.root.setOnClickListener {
            onClickListener.onClick(tempContinent, holder.item )
        }
    }

    override fun getItemCount(): Int {
        return listValues.size
    }
}