package edu.newhaven.chatbuzz.Controller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import edu.newhaven.chatbuzz.R
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.message_row.view.*


class NewMessageActivity : AppCompatActivity() {

    var arraySearch = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Message"
        fetchDetails()
    }

    /*Action For going back to the ChatMenu Activity*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, ChatMenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)

    }

    companion object {
        val USER_KEY = "USER_KEY"
    }

    /* Ferching the details from the Firebase from the values whihc are passed from Chatmenu Activity*/
    private fun fetchDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("/androidusers")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach() {
                    Log.d("New Message", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                        arraySearch.add(user.name);
                    }

                    adapter.setOnItemClickListener { item, view ->

                        val userItem = item as UserItem
                        val intent = Intent(view.context, ChatActivity::class.java)
                        intent.putExtra(USER_KEY, userItem.user)
                        startActivity(intent)
                    }
                }
                recyclerview_newmessage.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}

/*A new Class For the image and text of the users friend List*/
class UserItem(val user: User) : Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView2.text = user.name

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.profileImage_View_Contacts)
    }

    override fun getLayout(): Int {
        return R.layout.message_row
    }
}