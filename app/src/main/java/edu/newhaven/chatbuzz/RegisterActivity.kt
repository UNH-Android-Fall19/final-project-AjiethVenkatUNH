package edu.newhaven.chatbuzz

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_register.setOnClickListener {
            perform_register()
        }

        button_profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        signIn_textView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    var selectPhoto:Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectPhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPhoto)

            profileImageLayer.setImageBitmap(bitmap)
            button_profileImage.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            button_profileImage.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun perform_register() {
            val email = email_register.text.toString()
            val password = password_register.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email or Password is empty", Toast.LENGTH_SHORT).show()
                return
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d("RegisterActivity", "Email is:" + email)//${it.result.user.uid}")
                    uploadFirebaseProfileImage()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email is not valid", Toast.LENGTH_SHORT).show()
                }
        }
    private fun  uploadFirebaseProfileImage(){
        if(selectPhoto == null) return

        val filename =  UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/Image/$filename")

        ref.putFile(selectPhoto!!).addOnSuccessListener {

            ref.downloadUrl.addOnSuccessListener {
                userDeatilsToDatabase(it.toString())
            }
        }
            .addOnFailureListener{

            }
    }

    private fun userDeatilsToDatabase(profileImage: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/androidusers/$uid")
        val user = User(uid,email_register.text.toString(),username_register.text.toString(), password_register.text.toString(), profileImage)
        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this , ChatMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{

            }

    }
}

@Parcelize
class User(val uid:String ,val email: String,val name: String, val password: String, val profileImage:String): Parcelable{
    constructor(): this("","","","","")
}
