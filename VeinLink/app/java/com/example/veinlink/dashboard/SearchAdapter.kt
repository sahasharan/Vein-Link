package com.example.veinlink.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.veinlink.R
import com.example.veinlink.models.Donor

class SearchAdapter(
    private var donorList: List<Donor>
) : RecyclerView.Adapter<SearchAdapter.DonorViewHolder>() {

    class DonorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val donorName: TextView = itemView.findViewById(R.id.donorName)
        val donorBlood: TextView = itemView.findViewById(R.id.donorBlood)
        val donorLocation: TextView = itemView.findViewById(R.id.donorLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor, parent, false)
        return DonorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val donor = donorList[position]
        holder.donorName.text = donor.name
        holder.donorBlood.text = "Blood Group: ${donor.bloodGroup}"
        holder.donorLocation.text = "Location: ${donor.location}"
    }

    override fun getItemCount(): Int = donorList.size

    fun updateData(newList: List<Donor>) {
        donorList = newList
        notifyDataSetChanged()
    }
}
