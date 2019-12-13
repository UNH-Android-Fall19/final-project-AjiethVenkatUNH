package edu.newhaven.chatbuzz.Controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import edu.newhaven.chatbuzz.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/* Setting a singIn button and verfying the user with Email and password*/

        button_login.setOnClickListener {
            val email = email_login.text.toString()
            val password = password_login.text.toString()

            if (email.isEmpty() && password.isEmpty()) {

                Toast.makeText(this, "Email or Password is not valid", Toast.LENGTH_SHORT).show()

            }
            else {
                  //  emailAndPassword(email,password)
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
        }

        signUp_textView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /* SignIn method by authenticating with Firebase Auth*/
  //  fun emailAndPassword(email:String, password:String){

    //}
}

