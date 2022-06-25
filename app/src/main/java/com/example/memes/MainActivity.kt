package com.example.memes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var retrofit: Retrofit
    lateinit var methodCallApi: MethodCallApi
    lateinit var imageView: ImageView
    lateinit var share: Button
    lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshLayout = findViewById(R.id.refreshLayout)
        share = findViewById(R.id.share)
        imageView = findViewById(R.id.imageView)

        loadImage()

        refreshLayout.setOnRefreshListener {
            refreshLayout.setRefreshing(true)
            loadImage()
            refreshLayout.isRefreshing = false
        }
    }

    fun loadImage(){

        retrofit = Retrofit.Builder()
            .baseUrl("https://meme-api.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        methodCallApi = retrofit.create(MethodCallApi::class.java)

        // Easy api call using Coroutine....
        /*GlobalScope.launch(Dispatchers.IO){
            val d: Gimme = methodCallApi.getGimme().await()
        }*/

        val gimme: Call<Gimme> = methodCallApi.getGimme()
        gimme.enqueue(object : Callback<Gimme>{
            override fun onResponse(call: Call<Gimme>, response: Response<Gimme>) {
                if(! response.isSuccessful){
                    return
                }
                val currGimme = response.body()

                val url: String? = currGimme?.url
                val s: String? = url?.substring(url.length -3, url.length)
                if(s.equals("gif")){
                    loadImage()
                    return
                    //share.visibility = View.INVISIBLE
                }else{
                    //share.visibility = View.VISIBLE
                }
                Glide.with(this@MainActivity).load(url).into(imageView)

            }

            override fun onFailure(call: Call<Gimme>, t: Throwable) {
                if(t is IOException){
                    Toast.makeText(this@MainActivity, "Internet Connection Problem", Toast.LENGTH_LONG).show()
                    return
                }
                //Toast.makeText(this@MainActivity, "${t.printStackTrace()}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun onNext(view: View) {
        GlobalScope.launch {
            loadImage()
        }
    }

    fun onShare(view: View) {
        val mDrawable = imageView.drawable
        if(mDrawable == null){
            Toast.makeText(this, "Wait for a second", Toast.LENGTH_SHORT).show()
            return
        }
        val bitMap = (mDrawable as BitmapDrawable).bitmap
        val path: String = MediaStore.Images.Media.insertImage(contentResolver, bitMap, "Image Description", null)
        val uri = Uri.parse(path)

        Intent().setAction(Intent.ACTION_SEND).apply {
            setType("image/*")
            putExtra(Intent.EXTRA_STREAM, uri)
            startActivity(Intent.createChooser(this, "Share Image"))
        }

    }
}