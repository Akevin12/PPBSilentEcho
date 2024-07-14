package com.example.tubesrpll.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesrpll.viewmodel.NewsAdapter
import com.example.tubesrpll.R
import com.example.tubesrpll.model.NewsItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AllNews : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView // RecyclerView untuk menampilkan daftar berita
    private lateinit var adapter: NewsAdapter // Adapter untuk RecyclerView
    private val newsList = mutableListOf<NewsItem>() // Daftar Berita
    private lateinit var profileImageView: ImageView // ImageView untuk Gambar Profile Pengguna
    private lateinit var textViewNews: TextView // TextView untuk menampilkan teks
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_news)

        auth = FirebaseAuth.getInstance()

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NewsAdapter(newsList)
        recyclerView.adapter = adapter

        // Memuat berita dari Firebase
        fetchAllNews()

        // Inisialisasi tombol back dan setel onClickListener
        val backImageView = findViewById<ImageView>(R.id.imageViewBack)
        backImageView.setOnClickListener {
            startHomeActivity()
        }

        // Inisialisasi Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startHomeActivity()
                    true
                }
                R.id.navigation_news -> true
                R.id.navigation_profile -> {
                    startProfileActivity()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchAllNews() {
        val db = Firebase.firestore
        db.collection("news")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Mengonversi dokumen Firestore menjadi objek NewsItem
                    val newsItem = document.toObject<NewsItem>().copy(id = document.id)
                    newsList.add(newsItem)
                }
                adapter.notifyDataSetChanged() // Memberitahu adapter bahwa data telah berubah
            }
            .addOnFailureListener { exception ->
                Log.w("AllNewsActivity", "Error getting documents.", exception)
            }
    }

    private fun startProfileActivity() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, Profile::class.java))
        } else {
            startActivity(Intent(this, SignIn::class.java))
        }
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, Home::class.java))
    }
}
