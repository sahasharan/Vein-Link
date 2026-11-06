package com.example.veinlink.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.veinlink.R
import com.example.veinlink.models.Donor

class DashboardActivity : AppCompatActivity() {

    private lateinit var donorRecyclerView: RecyclerView
    private lateinit var donorAdapter: SearchAdapter
    private val donorList = mutableListOf<Donor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        donorRecyclerView = findViewById(R.id.donorRecyclerView)
        donorRecyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter setup
        donorAdapter = SearchAdapter(donorList)
        donorRecyclerView.adapter = donorAdapter

        // Load dummy donor data
        loadDummyDonors()
    }

    private fun loadDummyDonors() {
        val dummyData = listOf(
            Donor("1", "Ravi Kumar", "O+", "Hyderabad", true),
            Donor("2", "Priya Sharma", "A-", "Bangalore", true),
            Donor("3", "Arjun Singh", "B+", "Delhi", false),
            Donor("4", "Meena Reddy", "AB+", "Chennai", true)
        )
        donorList.clear()
        donorList.addAll(dummyData)
        donorAdapter.notifyDataSetChanged()
    }
}