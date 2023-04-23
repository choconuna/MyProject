package org.techtown.myproject.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
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

        view!!.findViewById<TextView>(R.id.contentArea).setOnLongClickListener {

            val mDialogView = LayoutInflater.from(view!!.context).inflate(R.layout.message_dialog, null)
            val mBuilder = AlertDialog.Builder(view!!.context).setView(mDialogView)

            val alertDialog = mBuilder.show()
            val deleteMessageBtn = alertDialog.findViewById<Button>(R.id.deleteMessageBtn)
            deleteMessageBtn?.setOnClickListener { // 메시지 삭제 버튼 클릭 시
                Log.d("messageClicked", "delete Message Button Clicked")

                FBRef.messageRef.child(messageList[position].chatConnectionId).child(messageList[position].messageId).removeValue() // 파이어베이스에서 해당 메시지 삭제
                Toast.makeText(view!!.context, "메시지가 삭제되었습니다!", Toast.LENGTH_SHORT).show()

                alertDialog.dismiss()
            }

            val copyMessageBtn = alertDialog.findViewById<Button>(R.id.copyMessageBtn)
            copyMessageBtn?.setOnClickListener {  // 메시지 복사 버튼 클릭 시
                Log.d("messageClicked", "copy Message Button Clicked")

                val clipboard =  view?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", view!!.findViewById<TextView>(R.id.contentArea).text.trim())
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(view!!.context, "메시지가 복사되었습니다!", Toast.LENGTH_SHORT).show()

                alertDialog.dismiss()
            }

            true
        }

        return view!!
    }
}