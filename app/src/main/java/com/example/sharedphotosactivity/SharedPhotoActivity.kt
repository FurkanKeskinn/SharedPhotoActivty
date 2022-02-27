package com.example.sharedphotosactivity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_shared_photo.*
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant.now
import java.util.*

class SharedPhotoActivity : AppCompatActivity() {

    var selectedItem : Uri?= null
    var selectedBitmap: Bitmap?= null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_photo)

        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
    }

    fun Shared(view: View) {

        //depo işlemleri
        //UUID -> Universal unique id        her dosya için farklı değer oluşturma işlemi

        val uuid = UUID.randomUUID()
        val pictureName = "${uuid}.jpg"
        val reference = storage.reference
        val pictureReference = reference.child("images").child(pictureName)


       if (selectedItem != null){
           pictureReference.putFile(selectedItem!!).addOnSuccessListener {
               taskSnapshot ->
               val uploadPictureReference  = FirebaseStorage.getInstance().reference.child("images").child(pictureName)
               uploadPictureReference.downloadUrl.addOnSuccessListener { uri ->
                   val downloadUrl = uri.toString()

                   val currentUserEmail = auth.currentUser!!.email.toString() //kullanıcı email adresi alma işlemi
                   val userComment = comment.text.toString()  //kullanıcı yorumu alma işlemi
                   val date = com.google.firebase.Timestamp.now()  //güncel zamanı alma işlemi


                   //veri tabanı işlemleri ((linki aldık))

                   val postHashMap = hashMapOf<String, Any>()
                   postHashMap.put("pictureUrl",downloadUrl)
                   postHashMap.put("userEmail",currentUserEmail)
                   postHashMap.put("date",date)
                   postHashMap.put("userComment",userComment)

                   database.collection("Post").add(postHashMap).addOnCompleteListener{
                       task ->

                       if(task.isSuccessful){
                           finish() // Feed activityden gelirken kapatmamaıştık burada kapatıyoruz ve geri dönüyoruz
                       }
                   }.addOnFailureListener{
                       exception ->
                       Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

                   }
               }

           }.addOnFailureListener{
               exception ->
               Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()

           }
       }

    }

    fun selectedPicture(view: View) {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {

            //izin zaten varsa
            val galeryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeryIntent,2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode == 1){
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izin verilince yapılacaklar
                val galeryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode ==2 && resultCode == Activity.RESULT_OK && data != null){
            selectedItem = data.data

            if(selectedItem != null){

                if(Build.VERSION.SDK_INT >= 28){

                    val source = ImageDecoder.createSource(this.contentResolver,selectedItem!!)
                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                    imageView.setImageBitmap(selectedBitmap)

                }else{

                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedItem)
                    imageView.setImageBitmap(selectedBitmap)
                }



            }

        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
