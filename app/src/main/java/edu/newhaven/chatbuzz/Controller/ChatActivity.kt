package edu.newhaven.chatbuzz.Controller

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import edu.newhaven.chatbuzz.Model.Chat_Model
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_message_from_row.view.*
import kotlinx.android.synthetic.main.chat_message_to_row.view.*
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.newhaven.chatbuzz.Model.Image_Model
import kotlinx.android.synthetic.main.chat_imagemessage_from_row.view.*
import edu.newhaven.chatbuzz.R
import kotlinx.android.synthetic.main.chat_imagemessage_to_row.view.*
import java.util.*

class ChatActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    companion object {
        val TAG = "Chat"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUserVar: User? = null
    private lateinit var mDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mDetector = GestureDetectorCompat(this, this)

        toUserVar = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUserVar?.name

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        Picasso.get().load(toUserVar?.profileImage).into(imageView_chatProfile)


        recycler_chat.adapter = adapter
        updateMessagesOnScreen()
        imageButton_chat.setOnClickListener {
            sendMessage()
        }
        imageView_gallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }
    }


    var selectPhoto: Uri? = null
    var chatImageUrl: String? = null
    /* Activity To select Photo form Gallery*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectPhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPhoto)

            uploadFirebaseChatImage()
        }
    }

    /* Upload the Images to Firebase Storage in the chat which was selected*/
    private fun uploadFirebaseChatImage() {
        if (selectPhoto == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/Image/$filename")

        ref.putFile(selectPhoto!!).addOnSuccessListener {

            ref.downloadUrl.addOnSuccessListener {
                chatImageUrl = it.toString()

                val fromId = FirebaseAuth.getInstance().currentUser?.uid
                val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                val toId = user.uid

                val ref =
                    FirebaseDatabase.getInstance().getReference("/androidmessages/$fromId/$toId")
                        .push()
                val toRefer =
                    FirebaseDatabase.getInstance().getReference("/androidmessages/$toId/$fromId")
                        .push()

                val chatMessage = Image_Model(
                    ref.key!!,
                    fromId,
                    chatImageUrl,
                    toId,
                    timeStamp = System.currentTimeMillis() / 1000
                )
                ref.setValue(chatMessage)
                    .addOnSuccessListener {
                        recycler_chat.scrollToPosition(adapter.itemCount - 1)
                    }
                toRefer.setValue(chatMessage)

                val newFromMessage =
                    FirebaseDatabase.getInstance().getReference("/updateMessage/$fromId/$toId")
                newFromMessage.setValue(chatMessage)
                val newToMessage =
                    FirebaseDatabase.getInstance().getReference("/updateMessage/$toId/$fromId")
                newToMessage.setValue(chatMessage)
            }
        }
            .addOnFailureListener {

            }
    }

    /*Gesture in the Chat Activity*/
    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Toast.makeText(this, "Scroll Pressed", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
        Toast.makeText(this, "Fling Pressed", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onDown(p0: MotionEvent?): Boolean {
        return true
    }

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        Toast.makeText(this, "Single Tap Pressed", Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onLongPress(p0: MotionEvent?) {
        Toast.makeText(this, "Long Pressed", Toast.LENGTH_SHORT).show()
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse("https://www.google.com/")
        startActivity(openURL)
    }

    override fun onShowPress(p0: MotionEvent?) {
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }


    /* update Message OnScreen to View by the user from his friend and the user's chat */
    private fun updateMessagesOnScreen() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUserVar?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/androidmessages/$fromId/$toId")

        ref.addChildEventListener((object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chat = p0.getValue(Chat_Model::class.java)
                val chatImage = p0.getValue(Image_Model::class.java)

                /*Defined the path of the Text child to split the text and Image that are displayed in the chat Activity*/
                if (p0.hasChild("text")) {
                    if (chat?.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatToItem(chat?.text!!))
                    } else {
                        adapter.add(ChatFromItem(chat?.text!!))
                    }
                } else {
                    if (chatImage?.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatImageToItem(chatImage?.url!!))
                    } else {
                        adapter.add(ChatImageFromItem(chatImage?.url!!))
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        }))
    }

    /* Sending the message to the Firebase after the user presses the send button */
    private fun sendMessage() {

        val text = editText_chat.text.toString()
        val fromId = FirebaseAuth.getInstance().currentUser?.uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if (fromId == null) return

        val ref =
            FirebaseDatabase.getInstance().getReference("/androidmessages/$fromId/$toId").push()
        val toRefer =
            FirebaseDatabase.getInstance().getReference("/androidmessages/$toId/$fromId").push()

        val chatMessage =
            Chat_Model(ref.key!!, fromId, text, toId, timeStamp = System.currentTimeMillis() / 1000)
        ref.setValue(chatMessage)
            .addOnSuccessListener {
                editText_chat.text.clear()
                recycler_chat.scrollToPosition(adapter.itemCount - 1)
            }
        toRefer.setValue(chatMessage)

        val newFromMessage =
            FirebaseDatabase.getInstance().getReference("/updateMessage/$fromId/$toId")
        newFromMessage.setValue(chatMessage)
        val newToMessage =
            FirebaseDatabase.getInstance().getReference("/updateMessage/$toId/$fromId")
        newToMessage.setValue(chatMessage)
    }
}


/* Class to place the text in the chat_message_from_row */
class ChatFromItem(val text: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_message_from_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_message_from_row
    }
}

/* Class to place the text in the chat_message_to_row */
class ChatToItem(val text: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textview_message_to_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_message_to_row
    }
}

/* Class to place the text in the chat_imagemessage_from_row */
class ChatImageFromItem(val text: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Glide.with(getApplicationContext()).load(text).into(viewHolder.itemView.imageFromChat)
    }

    override fun getLayout(): Int {
        return R.layout.chat_imagemessage_from_row
    }
}

/* Class to place the text in the chat_imagemessage_to_row */
class ChatImageToItem(val text: String) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Glide.with(getApplicationContext()).load(text).into(viewHolder.itemView.imageToChat)
    }

    override fun getLayout(): Int {
        return R.layout.chat_imagemessage_to_row
    }
}
