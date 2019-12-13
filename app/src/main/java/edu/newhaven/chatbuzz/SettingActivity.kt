package edu.newhaven.chatbuzz

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import android.view.GestureDetector
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.GridLayout.VERTICAL
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() , GestureDetector.OnGestureListener{

    private lateinit var mDetector: GestureDetectorCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        mDetector = GestureDetectorCompat(this, this)

      //  setSupportActionBar(findViewById(R.id.setting_view))

        // Get a support ActionBar corresponding to this toolbar and enable the Up button
       // supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textView_profile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        textView_web.setOnClickListener {
            Toast.makeText(this, "Long Pressed", Toast.LENGTH_SHORT).show()
            Log.d("ChatActivity", "onDoubleTapEvent:")
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.google.com/")
            startActivity(openURL)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                val intent = Intent(this , ChatMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Toast.makeText(this, "Scroll Pressed", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Toast.makeText(this, "Fling Pressed", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return false
    }


    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        Toast.makeText(this, "Single Tap Pressed", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onLongPress(p0: MotionEvent?) {

    }

    override fun onShowPress(p0: MotionEvent?) {

    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}
