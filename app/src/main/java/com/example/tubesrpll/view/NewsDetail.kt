package com.example.tubesrpll.view

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.tubesrpll.R
import com.example.tubesrpll.model.NewsItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

/**
 * Aktivitas untuk menampilkan detail berita.
 */
class NewsDetail : AppCompatActivity() {

    // Deklarasi variabel untuk elemen UI
    private lateinit var profileImageView: ImageView
    private lateinit var textView2: TextView
    private lateinit var textDetailHeadline: TextView
    private lateinit var textDetailTime: TextView
    private lateinit var textDetailContent: TextView
    private lateinit var imageDetail: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_detail)

        // Inisialisasi elemen UI
        textDetailHeadline = findViewById(R.id.textDetailHeadline)
        textDetailTime = findViewById(R.id.textDetailTime)
        textDetailContent = findViewById(R.id.textDetailContent)
        imageDetail = findViewById(R.id.imageView2)

        // Tombol kembali
        val backImageView = findViewById<ImageView>(R.id.imageViewBack)
        backImageView.setOnClickListener {
            onBackPressed()
        }

        // Menambahkan padding untuk menghindari sistem bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Mengambil documentId dari intent dan memuat detail berita
        val documentId = intent.getStringExtra("documentId")
        if (documentId != null) {
            fetchNewsDetail(documentId)
        }
    }

    /**
     * Memuat detail berita dari Firestore.
     * @param documentId ID dokumen berita.
     */
    private fun fetchNewsDetail(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("news")
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val newsItem = document.toObject(NewsItem::class.java)
                    if (newsItem != null) {
                        // Menampilkan detail berita
                        textDetailHeadline.text = newsItem.Headline
                        textDetailContent.text = formatContent(newsItem.Content)

                        // Memformat tanggal
                        val formattedDate = newsItem.Timestamp?.let {
                            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(it.toDate())
                        } ?: "Invalid timestamp"
                        textDetailTime.text = formattedDate

                        // Memuat gambar berita menggunakan Glide
                        if (newsItem.Image.isNotEmpty()) {
                            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(newsItem.Image)
                            storageReference.downloadUrl.addOnSuccessListener { uri ->
                                Glide.with(this)
                                    .load(uri)
                                    .override(850, 750)
                                    .into(imageDetail)
                            }.addOnFailureListener { e ->
                                Log.e("FirebaseStorage", "Error fetching image", e)
                            }
                        }
                    } else {
                        // Jika berita tidak ditemukan
                        textDetailHeadline.text = "No news available"
                        textDetailContent.text = ""
                        textDetailTime.text = "Unknown date"
                    }
                } else {
                    // Jika dokumen berita tidak ditemukan
                    textDetailHeadline.text = "No news available"
                    textDetailContent.text = ""
                    textDetailTime.text = "Unknown date"
                }
            }
            .addOnFailureListener { e ->
                // Jika gagal memuat berita
                textDetailHeadline.text = "Error fetching news"
                textDetailContent.text = ""
                textDetailTime.text = ""
                Log.e("Firestore", "Error fetching news", e)
            }
    }

    /**
     * Memformat konten berita untuk ditampilkan dengan baik.
     * @param content Konten berita asli.
     * @return Konten berita yang diformat.
     */
    private fun formatContent(content: String): String {
        val sentences = content.split(". ")
        val stringBuilder = StringBuilder()
        var sentenceCount = 0

        for (sentence in sentences) {
            if (sentenceCount % 5 == 0 && sentenceCount != 0) {
                stringBuilder.append(".\n\n")
            } else {
                if (sentenceCount != 0) {
                    stringBuilder.append(". ")
                }
            }
            stringBuilder.append(sentence.trim())
            sentenceCount++
        }
        return stringBuilder.toString().trim()
    }
}
