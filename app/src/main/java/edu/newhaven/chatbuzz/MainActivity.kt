package edu.newhaven.chatbuzz

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  //  LoginButton loginButton;
  //  FirebaseUser user;
  //  CallbackManager callbackManager;



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        button_login.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()

            if (email.isEmpty() && password.isEmpty()) {

                Toast.makeText(this, "Email or Password is not valid", Toast.LENGTH_SHORT).show()

            }
            else {
                val auth = FirebaseAuth.getInstance()

                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            val intent = Intent(this, ChatMenuActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
            }

            Log.d("MainActivity", "Email is" + email)
            Log.d("MainActivity", "Password is" + password)

        }

        signUp_textView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}

