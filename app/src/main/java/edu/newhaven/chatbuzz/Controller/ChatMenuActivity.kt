package edu.newhaven.chatbuzz.Controller

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.xwray.groupie.Item
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import edu.newhaven.chatbuzz.Model.Chat_Model
import edu.newhaven.chatbuzz.R
import kotlinx.android.synthetic.main.activity_chat_menu.*
import kotlinx.android.synthetic.main.chat_menu_message.view.*
//import kotlinx.android.synthetic.main.chat_menu_message.view.myswitch
import java.util.concurrent.Executor
import kotlin.collections.HashMap


class ChatMenuActivity : AppCompatActivity(), Application.ActivityLifecycleCallbacks {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    private var activityReferences = 0
    private val isActivityChangingConfigurations = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)

        /* Authenticating Boimetric for the Fignerprint*/
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        /* Prompt Screen for the Fingerprint*/
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText(" ")
            .build()
        registerActivityLifecycleCallbacks(this);

        chat_menu_recycler.adapter = adapter
        listenForChatMessage()

        /* Send the information from firebase to ChatActivity*/
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatActivity::class.java)
            val row = item as ListMessage
            intent.putExtra(NewMessageActivity.USER_KEY, row.user)
            startActivity(intent)
        }

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN//View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    /* Activity Life Cycle for Fingerprint*/
    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {

    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onActivityStopped(p0: Activity) {
    }


    val messageMap = HashMap<String, Chat_Model>()

    private fun refresh() {
        adapter.clear()
        messageMap.values.forEach() {
            adapter.add(ListMessage(it))
        }
    }

    /*Listening user data form Firebase database*/
    private fun listenForChatMessage() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/updateMessage/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chat = p0.getValue(Chat_Model::class.java) ?: return
                messageMap[p0.key!!] = chat
                refresh()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chat = p0.getValue(Chat_Model::class.java) ?: return
                messageMap[p0.key!!] = chat
                refresh()
            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    /*Menu for selecting items on the top Right Screen*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menu_new_Message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.setting -> {
                val intent = Intent(this, SettingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }
}


/* Display the messages for the chat which has done already (Friend List)*/
class ListMessage(val chat: Chat_Model) : Item<GroupieViewHolder>() {
    var user: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.messagechat_menu.text = chat.text

        val chatPerson: String
        if (chat.fromId == FirebaseAuth.getInstance().uid) {
            chatPerson = chat.toId
        } else {
            chatPerson = chat.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/androidusers/$chatPerson")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)
                viewHolder.itemView.username_chat_menu.text = user?.name
                val image = viewHolder.itemView.imageView_chatMessage
                Picasso.get().load(user?.profileImage).into(image)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.chat_menu_message
    }
}
