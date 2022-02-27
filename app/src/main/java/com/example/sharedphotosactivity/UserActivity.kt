package com.example.sharedphotosactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)


        auth = FirebaseAuth.getInstance()
        val guncelKullanici = auth.currentUser
        if(guncelKullanici != null){
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun girisYap(view: View) {

        auth.signInWithEmailAndPassword(emailText.text.toString(), passwordText.text.toString())  .addOnCompleteListener{
            task ->
            if (task.isSuccessful){

                val guncelKullanici = auth.currentUser?.email.toString()
                Toast.makeText(this, "Hoşgeldin: ${guncelKullanici}",Toast.LENGTH_LONG).show()
                val intent = Intent(this, FeedActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener{ exception ->

            Toast.makeText(this, exception.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

    fun kayitOl(view: View) {
        var email= emailText.text.toString()
        var password= passwordText.text.toString()


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task ->

                //asenkron
                if (task.isSuccessful){
                    //diğer aktiviteye git
                    val intent = Intent(this, FeedActivity::class.java)
                    startActivity(intent)
                    finish()

                }


            }
            .addOnFailureListener { exception ->

                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()


            }


    }
}


