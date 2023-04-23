package org.techtown.myproject.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.MessageModel

class MessageRVAdapter(val messageList : MutableList<MessageModel>) : BaseAdapter() {
    override fun getCount(): Int {
        return messageList.size
    }

    override fun getItem(position: Int): Any {
        return messageList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        if(messageList[position].sendUid == myUid)
            view = LayoutInflater.from(parent?.context).inflate(R.layout.my_chat_list_item, parent, false)
        else if(messageList[position].sendUid != myUid)
            view = LayoutInflater.from(parent?.context).inflate(R.layout.your_chat_list_item, parent, false)

        val dateArea = view!!.findViewById<TextView>(R.id.dateArea)

        if(messageList[position].sendUid == myUid && messageList[position].shown == "true")
            view!!.findViewById<TextView>(R.id.isShown).visibility = GONE
        else if(messageList[position].sendUid == myUid && messageList[position].shown == "false")
            view!!.findViewById<TextView>(R.id.isShown).visibility = VISIBLE

        when {
            position == 0 -> { // 해당 메시지가 첫 메시지라면 메시지 전송 날짜를 위에 표시하도록
                val date = messageList[position].sendDate.split(" ")[0]
                val dateSp = date.split(".")

                dateArea.visibility = VISIBLE
                dateArea.text = dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
            }
            messageList[position - 1].sendDate.split(" ")[0] != messageList[position].sendDate.split(" ")[0] -> { // 해당 메시지가 다음날에 보낸 메시지라면
                val date = messageList[position].sendDate.split(" ")[0]
                val dateSp = date.split(".")

                dateArea.visibility = VISIBLE
                dateArea.text = dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
            }
            else -> dateArea.visibility = GONE
        }

        view!!.findViewById<TextView>(R.id.contentArea).text = messageList[position].content

        val timeSp = messageList[position].sendDate.split(" ")[1]
        view!!.findViewById<TextView>(R.id.sendTimeArea).text = timeSp

        return view!!
    }
}