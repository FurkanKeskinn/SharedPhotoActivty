package com.example.sharedphotosactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_feed.*

class FeedActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseFirestore
    private lateinit var recyclerViewAdapter : FeedRecyclerAdapter


    var postList = ArrayList<Post>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        getData()

        var layoutManager = LinearLayoutManager(this)
        recycler.layoutManager = layoutManager
        recyclerViewAdapter = FeedRecyclerAdapter(postList)
        recycler.adapter = recyclerViewAdapter
    }

    fun getData(){
        database.collection("Post")
            //tarihe göre dizdirme işlemi (son girilen ilk görülen işlemi)
            .orderBy("date", Query.Direction.DESCENDING)

            .addSnapshotListener{
            snapshot, exception ->

            if (exception !=null){
                Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_LONG).show()

            }else{

                if (snapshot != null){
                    if (!snapshot.isEmpty){

                        val documents = snapshot.documents

                        postList.clear() // post listesinde daha önceden bir şey varsa silme işlemi yapıyoruz.

                        for (document in documents){

                        //her bir dökümanı bu işlemle alıyoruz (post altındaki uIdler)

                            val userEmail = document.get("userEmail") as String
                            val userComment = document.get("userComment") as String
                            val pictureUrl = document.get("pictureUrl") as String

                            val downloadPost = Post(userEmail, userComment,pictureUrl)
                            postList.add(downloadPost)

                        }

                        recyclerViewAdapter.notifyDataSetChanged()// yeni veri geldi kendini yenile işlemi
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.options_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.shared_photo){
        //Fotoğraf paylaşma aktivitesine gidecek

            val intent = Intent(this,SharedPhotoActivity::class.java)
            startActivity(intent)

        }else if (item.itemId == R.id.sign_out){

            auth.signOut()
            val intent = Intent(this,UserActivity::class.java)
            startActivity(intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}