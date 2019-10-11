package edu.newhaven.chatbuzz

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  //  LoginButton loginButton;
  //  FirbaseAuth auth;
  //  FirebaseUser user;
  //  CallbackManager callbackManager;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    //    FacebookSdk.sdkInitialize(getApplicationContext())
   //     AppEventsLogger.activateApp(this)

    //    loginButton = (login_button)findViewbtId(R.id.login)

        button_login.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()

            Log.d("MainActivity", "Email is" + email)
            Log.d("MainActivity", "Password is" + password)

        }


        signUp_textView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }



    }
}

