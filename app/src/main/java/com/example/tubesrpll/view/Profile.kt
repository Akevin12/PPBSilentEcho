package com.example.tubesrpll.view

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import com.example.tubesrpll.R

/**
 * Aktivitas untuk menampilkan profile
 */
class Profile : AppCompatActivity() {

    // Deklarasi variabel UI
    private lateinit var imageView: ImageView
    private lateinit var imageGender: ImageView
    private lateinit var imageAddress: ImageView
    private lateinit var button: FloatingActionButton
    private lateinit var nameProfile: TextView
    private lateinit var phoneProfile: TextView
    private lateinit var emailProfile: TextView
    private lateinit var genderProfile: TextView
    private lateinit var addressProfile: TextView
    private lateinit var buttonEdit: Button
    private lateinit var buttonLogout: Button

    companion object {
        const val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        // Inisialisasi variabel UI
        imageView = findViewById(R.id.imagePhoto)
        imageGender = findViewById(R.id.imageGender)
        imageAddress = findViewById(R.id.imageAddress)
        button = findViewById(R.id.cameraButton)
        nameProfile = findViewById(R.id.NameProfile)
        phoneProfile = findViewById(R.id.phoneProfile)
        emailProfile = findViewById(R.id.emailProfile)
        genderProfile = findViewById(R.id.genderProfile)
        addressProfile = findViewById(R.id.addressProfile)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonLogout = findViewById(R.id.buttonLogout)

        // Tombol kembali
        val backImageView = findViewById<ImageView>(R.id.imageViewBack)
        backImageView.setOnClickListener {
            onBackPressed()
        }

        // Set listener untuk tombol kamera
        button.setOnClickListener {
            pickImage()
        }

        // Set listener untuk tombol logout
        buttonLogout.setOnClickListener {
            logout()
        }

        imageView.setOnClickListener {
            showLargeImage()
        }

        // Ambil data profil dari Firestore
        fetchProfileData()
    }

    private fun showLargeImage() {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_large_image)
        val largeImageView: ImageView = dialog.findViewById(R.id.largeImageView)
        val backButton: ImageView = dialog.findViewById(R.id.imageViewBack)

        // Mengambil gambar dari imageProfile dan menampilkannya di largeImageView
        imageView.drawable?.let {
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

    // Fungsi untuk memilih gambar dari galeri
    private fun pickImage() {
        ImagePicker.with(this)
            .galleryOnly()
            .crop()  // Optional cropping
            .start(IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                imageView.setImageURI(uri)
                uploadImageToFirebase(uri)
            }
        }
    }

    // Fungsi untuk mengunggah gambar ke Firebase Storage
    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val storageReference = FirebaseStorage.getInstance().reference.child("profile/$userId.jpg")

            storageReference.putFile(uri)
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateProfileImage(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("FirebaseStorage", "Error uploading image", exception)
                }
        }
    }

    // Fungsi untuk memperbarui URL gambar profil di Firestore
    private fun updateProfileImage(imageUrl: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId)
                .update("profileImage", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to update profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error updating profile image", exception)
                }
        }
    }

    // Fungsi untuk mengambil data profil dari Firestore
    private fun fetchProfileData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = FirebaseFirestore.getInstance()
            val email = currentUser.email
            emailProfile.text = email

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        val phone = document.getString("phone")
                        val profileImage = document.getString("profileImage")
                        val gender = document.getString("gender")
                        val address = document.getString("address")

                        nameProfile.text = name
                        phoneProfile.text = phone

                        if (profileImage != null && profileImage.isNotEmpty()) {
                            Glide.with(this)
                                .load(profileImage)
                                .into(imageView)
                        }

                        if (gender != null && gender.isNotEmpty()) {
                            genderProfile.text = gender
                            imageGender.visibility = View.VISIBLE
                        } else {
                            imageGender.visibility = View.GONE
                        }

                        if (address != null && address.isNotEmpty()) {
                            addressProfile.text = address
                            imageAddress.visibility = View.VISIBLE
                        } else {
                            addressProfile.text = ""
                            imageAddress.visibility = View.GONE
                        }

                        // Adjust button position based on fields presence
                        if (gender != null && gender.isNotEmpty() || (address != null && address.isNotEmpty())) {
                            buttonEdit.visibility = View.VISIBLE
                            val params = buttonEdit.layoutParams as ConstraintLayout.LayoutParams
                            params.topMargin = resources.getDimensionPixelSize(R.dimen.button_edit_top_margin)
                            buttonEdit.layoutParams = params
                        } else {
                            buttonEdit.visibility = View.VISIBLE
                            val params = buttonEdit.layoutParams as ConstraintLayout.LayoutParams
                            params.topMargin = resources.getDimensionPixelSize(R.dimen.button_edit_top_margin_no_fields)
                            buttonEdit.layoutParams = params
                        }
                    } else {
                        Toast.makeText(this, "Document does not exist", Toast.LENGTH_SHORT).show()
                        Log.e("Firestore", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch profile data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Firestore", "Error fetching profile data", exception)
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }



    // Fungsi untuk pindah ke halaman Edit Profile
    fun moveToEditProfile(view: View) {
        val intent = Intent(this, EditProfile::class.java)
        startActivity(intent)
    }

    // Fungsi untuk logout
    private fun logout() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { dialog, which ->
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
