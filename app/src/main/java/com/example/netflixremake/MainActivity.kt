package com.example.netflixremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import com.example.netflixremake.util.CategoryTask
import java.io.File

class MainActivity : AppCompatActivity(), CategoryTask.Callback {
    private val categories = mutableListOf<Category>()


    lateinit var progressBar : ProgressBar
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val api_key = BuildConfig.API_KEY

        progressBar = findViewById(R.id.progress_home)

        adapter = CategoryAdapter(categories) {id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        val rv : RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv.adapter = adapter


        CategoryTask(this).execute("https://api.tiagoaguiar.co/netflixapp/home?$api_key")


    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE

    }

    override fun onResult(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged()
        progressBar.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBar.visibility = View.GONE

    }


}