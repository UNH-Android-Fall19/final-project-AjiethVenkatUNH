package edu.newhaven.chatbuzz.Controller

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.newhaven.chatbuzz.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private var imageview: ImageView? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        downloadFirebase()

        imageview = findViewById(R.id.imageView_edit_profile) as ImageView

        imageview!!.setOnClickListener { showPictureDialog() }

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        save_button.setOnClickListener {
            downloadFirebase()
            Log.d("ProfileActivity", "Updated successfully 1")
        }
    }

    /* Navigation Action to the Settings Activity*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, SettingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*Download data from Firebasedatabase */
    private fun downloadFirebase() {

        var user: User? = null
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var ref = FirebaseDatabase.getInstance().getReference("/androidusers").child((uid!!))

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)

                textView_name_profile.text = user?.name
                textView_email_profile.text = user?.email
                Picasso.get().load(user?.profileImage).into(imageView_edit_profile)

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    /* Prompt Box for the selection of Camera or Gallery*/
    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select image from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> chooseImageFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    /* If the option selected is Gallery*/
    fun chooseImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    /* If the option selected is Camera*/
    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA)
    }

    /* Check for the image and upload the image to the storage and image View in the profile Activity*/
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    if (contentURI == null) return

                    val filename = UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("/Image/$filename")

                    ref.putFile(contentURI).addOnSuccessListener {

                        ref.downloadUrl.addOnSuccessListener {
                            uploadFirebaseProfileImage(it.toString())
                        }
                    }
                        .addOnFailureListener {

                        }
                    Toast.makeText(this@ProfileActivity, "Image Show!", Toast.LENGTH_SHORT).show()
                    imageView_edit_profile.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            imageView_edit_profile.setImageBitmap(thumbnail)

        }
    }


    /* uploading the edited profile Image to the Firebase Database */
    private fun uploadFirebaseProfileImage(image: String) {

        var uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        var userRef = FirebaseDatabase.getInstance().getReference("/androidusers/$uid")

        userRef.child("profileImage").setValue(image).addOnSuccessListener {
        }
            .addOnFailureListener {
            }
    }

    companion object {
        private val IMAGE_DIRECTORY = "/nalhdaf"
    }

}


