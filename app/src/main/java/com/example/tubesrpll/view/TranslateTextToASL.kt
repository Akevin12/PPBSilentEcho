package com.example.tubesrpll.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tubesrpll.viewmodel.ASLImageAdapter
import com.example.tubesrpll.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class TranslateTextToASL : AppCompatActivity() {

    // Inisialisasi variabel untuk Firebase Storage, Adapter dan elemen UI
    private lateinit var storageReference: StorageReference
    private lateinit var aslImageAdapter: ASLImageAdapter
    private lateinit var profileImageView: ImageView
    private lateinit var textViewASL: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate_text_to_asl)

        // Mendapatkan referensi Firebase Storage
        storageReference = FirebaseStorage.getInstance().reference

        // Menghubungkan variabel dengan elemen UI
        val textASL = findViewById<EditText>(R.id.textInputEditTextASL)
        val buttonASL = findViewById<Button>(R.id.buttonResultASL)
        val recyclerViewASL = findViewById<RecyclerView>(R.id.recyclerViewASL)

        // Inisialisasi adapter dan layout manager untuk RecyclerView
        aslImageAdapter = ASLImageAdapter(this)
        recyclerViewASL.layoutManager = LinearLayoutManager(this)
        recyclerViewASL.adapter = aslImageAdapter

        // Menambahkan TextWatcher untuk membatasi input teks hingga 50 karakter
        textASL.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 50) {
                    textASL.setText(s.subSequence(0, 50))
                    textASL.setSelection(50)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Menambahkan onClickListener untuk tombol translate
        buttonASL.setOnClickListener {
            val inputText = textASL.text.toString()
            if (inputText.isNotEmpty()) {
                updateASLImages(inputText)
            }
        }

        // Tombol kembali
        val backImageView = findViewById<ImageView>(R.id.imageViewBack)
        backImageView.setOnClickListener {
            onBackPressed()
        }
    }

    // Fungsi untuk memperbarui gambar ASL berdasarkan input teks
    private fun updateASLImages(text: String) {
        val imageList = mutableListOf<List<StorageReference>>()
        val lines = text.split(" ")

        lines.forEach { line ->
            val charList = mutableListOf<StorageReference>()
            line.forEach { char ->
                val fileName = "ASL image/${char.lowercaseChar()}.png"
                val imageRef = storageReference.child(fileName)
                charList.add(imageRef)
            }
            imageList.add(charList)
        }

        aslImageAdapter.updateImageList(imageList)
    }
}
