package com.example.tubesrpll.view

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesrpll.R
import com.example.tubesrpll.model.NewsItem
import com.example.tubesrpll.viewmodel.NewsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.squareup.picasso.Picasso

class Home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var textView2: TextView
    private lateinit var recyclerView: RecyclerView
    private val newsList = mutableListOf<NewsItem>()
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NewsAdapter(newsList)
        recyclerView.adapter = adapter

        textView2 = findViewById(R.id.textView)
        profileImageView = findViewById(R.id.imageProfile) // Ensure you have this ImageView in your layout

        // Mengambil berita utama dan gambar profil
        getMainNews()
        fetchProfileImage()

        // Inisialisasi Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_news -> {
                    startNewsActivity()
                    true
                }
                R.id.navigation_profile -> {
                    startProfileActivity()
                    true
                }
                else -> false
            }
        }

        profileImageView.setOnClickListener {
            showLargeImage()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchProfileImage()
    }

    private fun fetchProfileImage() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profileImage = document.getString("profileImage")
                        if (profileImage != null && profileImage.isNotEmpty()) {
                            Picasso.get().load(profileImage).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.baseline_person_24)
                        }

                        val userName = document.getString("name")
                        if (userName != null && userName.isNotEmpty()) {
                            textView2.text = "Welcome $userName"
                        } else {
                            textView2.text = "Welcome Guest"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error fetching profile image", exception)
                    textView2.text = "Welcome Guest"
                }
        } else {
            profileImageView.setImageResource(R.drawable.baseline_person_24)
            textView2.text = "Welcome Guest"
        }
    }

    private fun showLargeImage() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_large_image)
        val largeImageView: ImageView = dialog.findViewById(R.id.largeImageView)
        val backButton: ImageView = dialog.findViewById(R.id.imageViewBack)

        // Mengambil gambar dari imageProfile dan menampilkannya di largeImageView
        profileImageView.drawable?.let {
            largeImageView.setImageDrawable(it)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.black)

        // Set the ImageView to have MATCH_PARENT for both width and height
        largeImageView.layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
        largeImageView.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
        largeImageView.scaleType = ImageView.ScaleType.FIT_CENTER

        // Show the back button initially
        backButton.visibility = View.VISIBLE

        largeImageView.setOnClickListener {
            // Close dialog when image is clicked
            dialog.dismiss()
        }

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun getMainNews() {
        val db = FirebaseFirestore.getInstance()
        db.collection("news")
            .orderBy("Timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val newsItem = document.toObject<NewsItem>()
                    if (newsItem != null) {
                        newsItem.id = document.id
                        newsList.add(newsItem)
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    Log.d("Home", "No news found")
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Home", "Error getting documents.", exception)
            }
    }

    fun moveToVideoASLToText(view: View) {
        navigateToActivity(TranslateVideoASLToText::class.java)
    }

    fun moveToVideoASLToBISINDO(view: View) {
        navigateToActivity(TranslateVideoASLToBISINDO::class.java)
    }

    fun moveToTextToASL(view: View) {
        navigateToActivity(TranslateTextToASL::class.java)
    }

    fun moveToTextToBISINDO(view: View) {
        navigateToActivity(TranslateTextToBISINDO::class.java)
    }

    private fun navigateToActivity(targetActivity: Class<*>) {
        if (auth.currentUser != null) {
            startActivity(Intent(this, targetActivity))
        } else {
            promptLogin()
        }
    }

    private fun startProfileActivity() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, Profile::class.java))
        } else {
            startActivity(Intent(this, SignIn::class.java))
        }
    }

    private fun startNewsActivity() {
        startActivity(Intent(this, AllNews::class.java))
    }

    private fun promptLogin() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setMessage("You need to login to use the features")
            .setCancelable(false)
            .setPositiveButton("Login") { dialog, id ->
                val loginIntent = Intent(this, SignIn::class.java)
                startActivity(loginIntent)
            }
            .setNegativeButton("Cancel") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}
