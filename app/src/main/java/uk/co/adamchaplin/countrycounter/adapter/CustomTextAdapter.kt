package uk.co.adamchaplin.countrycounter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.adamchaplin.countrycounter.databinding.RecyclerViewItemBinding

class CustomTextAdapter(
    private val listValues: List<String>,
    private val colour: Int
): RecyclerView.Adapter<CustomTextAdapter.ViewHolder>() {

    class ViewHolder(val item: RecyclerViewItemBinding) : RecyclerView.ViewHolder(item.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =  RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.textView.text = listValues[position]
        holder.item.textView.setTextColor(colour)
    }

    override fun getItemCount(): Int {
        return listValues.size
    }
}