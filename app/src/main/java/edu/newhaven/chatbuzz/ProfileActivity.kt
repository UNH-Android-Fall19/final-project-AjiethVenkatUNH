package edu.newhaven.chatbuzz

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.chat_menu_message.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*


class ProfileActivity: AppCompatActivity() {

    private var imageview: ImageView? = null
    private val GALLERY = 1
    private val CAMERA = 2

    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)

        downloadFirebase()

        imageview = findViewById(R.id.imageView_edit_profile) as ImageView

        imageview!!.setOnClickListener{ showPictureDialog() }

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        save_button.setOnClickListener {
           downloadFirebase()
            Log.d("ProfileActivity","Updated successfully 1")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                val intent = Intent(this , SettingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    var tempName: String? = null;
    var tempEmail: String? = null;
    var tempPassword: String? = null;
    private fun downloadFirebase(){

        var user: User?  = null
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        var ref = FirebaseDatabase.getInstance().getReference("/androidusers").child((uid!!))
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)

                textView_name_profile.text = user?.name
                tempName = user?.name

                textView_email_profile.text = user?.email
                tempEmail = user?.email

                tempPassword = user?.password

                Picasso.get().load(user?.profileImage).into(imageView_edit_profile)

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select image from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> chooseImageFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    fun chooseImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY)
        {
            if (data != null)
            {
                val contentURI = data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                  //  saveImage(bitmap)
                    if(contentURI == null) return

                    val filename =  UUID.randomUUID().toString()
                    val ref = FirebaseStorage.getInstance().getReference("/Image/$filename")

                    ref.putFile(contentURI).addOnSuccessListener {

                        ref.downloadUrl.addOnSuccessListener {
                            uploadFirebaseProfileImage(it.toString())
                        }
                    }
                        .addOnFailureListener{

                        }
                    Toast.makeText(this@ProfileActivity, "Image Show!", Toast.LENGTH_SHORT).show()
                    imageView_edit_profile.setImageBitmap(bitmap)
                }
                catch (e: IOException)
                {
                    e.printStackTrace()
                    Toast.makeText(this@ProfileActivity, "Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (requestCode == CAMERA)
        {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            imageView_edit_profile.setImageBitmap(thumbnail)
         //   saveImage(thumbnail)
            Toast.makeText(this@ProfileActivity, "Photo Show!", Toast.LENGTH_SHORT).show()
        }
    }



    private fun  uploadFirebaseProfileImage(image: String){

        Log.d("ProfileActivity","Updated successfully 2")


            var uid = FirebaseAuth.getInstance().currentUser?.uid?: ""
            var userRef = FirebaseDatabase.getInstance().getReference("/androidusers/$uid")
            //    var image:String = it.toString();
            //   Log.d("ProfileActivity","Updated successfully  result" + uid + tempEmail + tempName + tempPassword + image)
            //     var user = UserProf(uid, tempEmail.toString(), tempName.toString(), tempPassword.toString(),image)
            //   userRef.setValue(it.toString()).addOnSuccessListener {
        Log.d("ProfileActivity","Updated successfully 3")
            userRef.child("profileImage").setValue(image).addOnSuccessListener {
                Log.d("ProfileActivity","Updated successfully  4")
            //  FirebaseDatabase.getInstance().getReference("/androidusers").child((uid!!)).child("profileImage").setValue(it.toString())

        }
            .addOnFailureListener{

            }
    }

    fun saveImage(myBitmap: Bitmap):String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
        val wallpaperDirectory = File (
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists())
        {
            wallpaperDirectory.mkdirs()
        }
        try
        {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(wallpaperDirectory, ((Calendar.getInstance()
                .getTimeInMillis()).toString() + ".png"))
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.getPath()), arrayOf("image/png"), null)
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath())

            return f.getAbsolutePath()
        }
        catch (e1: IOException){
            e1.printStackTrace()
        }
        return ""
    }

    companion object {
        private val IMAGE_DIRECTORY = "/nalhdaf"
    }

}

@Parcelize
class UserProf(val uid:String ,val email: String,val name: String, val password: String, val profileImage:String): Parcelable{
    constructor(): this("","","","","")
}
