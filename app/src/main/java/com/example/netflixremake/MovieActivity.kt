package com.example.netflixremake


import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixremake.model.Movie
import com.example.netflixremake.model.MovieDetail
import com.example.netflixremake.util.DownloadImageTask
import com.example.netflixremake.util.MovieTask
import java.io.File

class MovieActivity : AppCompatActivity(), MovieTask.Callback {

    private lateinit var txtTittle : TextView
    private lateinit var txtDesc : TextView
    private lateinit var txtCast : TextView
    private lateinit var progress : ProgressBar
    private lateinit var adapter : MovieAdapter
    private val movies = mutableListOf<Movie>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        val api_key = BuildConfig.API_KEY

        txtTittle = findViewById(R.id.movie_txt_title)
        txtCast = findViewById(R.id.movie_txt_cast)
        txtDesc = findViewById(R.id.movie_txt_desc)
        val rv : RecyclerView = findViewById(R.id.movie_rv_similar)
        progress = findViewById(R.id.movie_progressbar)


        val id = intent?.getIntExtra("id", 0) ?: throw IllegalStateException("ID não foi encontrado! ")

        val url = "https://api.tiagoaguiar.co/netflixapp/movie/$id?$api_key"

        MovieTask(this).execute(url)



        adapter = MovieAdapter(movies, R.layout.movie_item_grid)
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter


        val toolbar : Toolbar = findViewById(R.id.movie_toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null




    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResult(movieDetail: MovieDetail) {
        progress.visibility = View.GONE

        txtTittle.text = movieDetail.movie.title
        txtDesc.text = movieDetail.movie.desc
        txtCast.text = getString(R.string.cast, movieDetail.movie.cast)

        movies.clear()
        movies.addAll(movieDetail.similars)
        adapter.notifyDataSetChanged()


        DownloadImageTask(object : DownloadImageTask.Callback {
            override fun onResult(bitmap: Bitmap) {

                val layerDrawable: LayerDrawable = ContextCompat.getDrawable(this@MovieActivity, R.drawable.shadows) as LayerDrawable
                val movieCover = BitmapDrawable(resources, bitmap)
                layerDrawable.setDrawableByLayerId(R.id.cover_drawable, movieCover)
                val coverImg : ImageView = findViewById(R.id.movie_png)
                coverImg.setImageDrawable(layerDrawable)

            }
        }).execute(movieDetail.movie.coverUrl)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

}