package com.example.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.netflixremake.model.Category
import com.example.netflixremake.model.Movie
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask (private val callback: Callback){

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()
        val executor = Executors.newSingleThreadExecutor()


        //Nova thread
        executor.execute{

            var urlConnection : HttpsURLConnection? = null
            var stream : InputStream? = null

            try{
                val requestUrl = URL(url) // abrir a URL
                urlConnection = requestUrl.openConnection() as HttpsURLConnection // Abrindo conexão com o servidor
                urlConnection.readTimeout = 2000 // tempo de leitura 2s
                urlConnection.connectTimeout = 2000 // tempo de conexão 2s

                val statusCode : Int = urlConnection.responseCode // me retorna o status do servidor
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor")
                }

                stream = urlConnection.inputStream // sequencia de bytes
                val jsonAsString = stream.bufferedReader().use{it.readText()} // buffer é o espaço de memoria, está transformando de bytes para Strings
                val categories = toCategories(jsonAsString)

                handler.post {
                    callback.onResult(categories)
                }

            } catch (e: Exception) {
                val message = e.message ?: "erro Desconhecido"
                handler.post{
                    callback.onFailure(message)
                }
            } finally {
                urlConnection?.disconnect()
                stream?.close()

            }
        }
    }

    private fun toCategories(jsonAsString : String) : List<Category> {

        val categories = mutableListOf<Category>()

        val jsonRoot = JSONObject(jsonAsString) // Busca a categoria
        val jsonCategories = jsonRoot.getJSONArray("category") // me devolve os dados das categorias

        for (i in 0 until jsonCategories.length()) { // loop para pegar cada um objeto dentro da categoria
            val jsonCategory = jsonCategories.getJSONObject(i) // primeiro indice

            val title = jsonCategory.getString("title") // pegar a chave do valor titulo
            val jsonMovies = jsonCategory.getJSONArray("movie") // pegar a chave do valor filmes

            val movies = mutableListOf<Movie>() // importando / criando minha lista de filme vazia
            for (j in 0 until jsonMovies.length()) { // loop para pegar os filmes
                val jsonMovie = jsonMovies.getJSONObject(j) // primeiro indice
                val id = jsonMovie.getInt("id") // pegar a chave do valor id
                val coverUrl = jsonMovie.getString("cover_url") // pegar a chave do valor url

                movies.add(Movie(id, coverUrl)) // lista de filme de uma categoria
            }

            categories.add(Category(title, movies))

        }
        return categories
    }
}