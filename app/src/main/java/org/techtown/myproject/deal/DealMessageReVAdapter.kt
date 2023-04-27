package org.techtown.myproject.deal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.chat.MessageImageAdapter
import org.techtown.myproject.note.ImageDetailActivity
import org.techtown.myproject.utils.DealMessageModel
import org.techtown.myproject.utils.FBRef
import java.util.ArrayList

class DealMessageReVAdapter(val messageList : MutableList<DealMessageModel>) : BaseAdapter() {

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

        if (messageList[position].sendUid == myUid)
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.my_chat_list_item, parent, false)
        else if (messageList[position].sendUid != myUid)
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.your_chat_list_item, parent, false)

        Log.d(
            "deleteMessage",
            messageList[position].chatConnectionId + messageList[position].messageId
        )
        Log.d("deleteMessage", messageList[position].type)

        val imageDataList = ArrayList<String>()
        lateinit var imageListView: RecyclerView

        var messageImageVAdapter = MessageImageAdapter(imageDataList)
        imageListView = view!!.findViewById(R.id.imageRecyclerView)
        imageListView.setItemViewCacheSize(20)
        imageListView.setHasFixedSize(true)
        var layoutManager =
            LinearLayoutManager(view!!.context, LinearLayoutManager.HORIZONTAL, false)
        imageListView.layoutManager = layoutManager
        imageListView.adapter = messageImageVAdapter

        val dateArea = view!!.findViewById<TextView>(R.id.dateArea)

        if (messageList[position].sendUid == myUid && messageList[position].shown == "true")
            view!!.findViewById<TextView>(R.id.isShown).visibility = View.GONE
        else if (messageList[position].sendUid == myUid && messageList[position].shown == "false")
            view!!.findViewById<TextView>(R.id.isShown).visibility = View.VISIBLE

        when {
            position == 0 -> { // 해당 메시지가 첫 메시지라면 메시지 전송 날짜를 위에 표시하도록
                val date = messageList[position].sendDate.split(" ")[0]
                val dateSp = date.split(".")

                dateArea.visibility = View.VISIBLE
                dateArea.text =
                    dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
            }
            messageList[position - 1].sendDate.split(" ")[0] != messageList[position].sendDate.split(
                " "
            )[0] -> { // 해당 메시지가 다음날에 보낸 메시지라면
                val date = messageList[position].sendDate.split(" ")[0]
                val dateSp = date.split(".")

                dateArea.visibility = View.VISIBLE
                dateArea.text =
                    dateSp[0] + "." + dateSp[1] + "." + dateSp[2] + " (" + dateSp[3] + ")"
            }
            else -> dateArea.visibility = View.GONE
        }

        if (messageList[position].type == "letter") {
            view!!.findViewById<TextView>(R.id.contentArea).text = messageList[position].content
            view!!.findViewById<TextView>(R.id.contentArea).visibility = View.VISIBLE
            view!!.findViewById<RecyclerView>(R.id.imageRecyclerView).visibility = View.GONE
        } else if (messageList[position].type == "picture") {

            messageImageVAdapter = MessageImageAdapter(imageDataList)
            imageListView = view!!.findViewById(R.id.imageRecyclerView)
            imageListView.setItemViewCacheSize(20)
            imageListView.setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(view!!.context, LinearLayoutManager.HORIZONTAL, false)
            imageListView.layoutManager = layoutManager
            imageListView.adapter = messageImageVAdapter

            var chatConnectionId = messageList[position].chatConnectionId
            var messageId = messageList[position].messageId

            Log.d("deleteMessage", "실행")

            imageDataList.clear()

            if (messageList[position].picNum.toInt() > 0) {
                for (index in 0 until messageList[position].picNum.toInt()) {
                    imageDataList.add("messageImage/$chatConnectionId/$messageId/$messageId$index.png")
                }
                Log.d("imageDataList", imageDataList.toString())
            }
            messageImageVAdapter.notifyDataSetChanged()

            view!!.findViewById<TextView>(R.id.contentArea).visibility = View.GONE
            view!!.findViewById<RecyclerView>(R.id.imageRecyclerView).visibility = View.VISIBLE
        }

        view!!.findViewById<TextView>(R.id.contentArea).maxWidth =
            15 * view!!.findViewById<TextView>(R.id.contentArea).lineHeight

        val timeSp = messageList[position].sendDate.split(" ")[1]
        view!!.findViewById<TextView>(R.id.sendTimeArea).text = timeSp

        messageImageVAdapter.setItemClickListener(object : MessageImageAdapter.OnItemClickListener {
            override fun onClick(v: View, p: Int) {
                if (messageList[position].sendUid != myUid) { // 메시지를 보낸 사람이 사용자가 아닐 경우
                    val intent = Intent(view!!.context, ImageDetailActivity::class.java)
                    Log.d("imageDataList", imageDataList[p])
                    intent.putExtra("image", imageDataList[p]) // 사진 링크 넘기기
                    view!!.context.startActivity(intent)
                } else if (messageList[position].sendUid == myUid) { // 메시지를 보낸 사람이 사용자일 경우
                    val mDialogView = LayoutInflater.from(view!!.context)
                        .inflate(R.layout.message_image_dialog, null)
                    val mBuilder = AlertDialog.Builder(view!!.context).setView(mDialogView)

                    val alertDialog = mBuilder.show()

                    val deleteMessageBtn = alertDialog.findViewById<Button>(R.id.deleteMessageBtn)
                    deleteMessageBtn?.setOnClickListener { // 메시지 삭제 버튼 클릭 시
                        Log.d("messageClicked", "delete Message Button Clicked")

                        var chatConnectionId = messageList[position].chatConnectionId
                        var messageId = messageList[position].messageId

                        Log.d(
                            "deleteMessage",
                            messageList[position].chatConnectionId + messageList[position].messageId
                        )
                        Log.d("deleteMessage", messageList[position].type)

                        if (messageList[position].type == "picture") {
                            for (index in 0 until messageList[position].picNum.toInt()) {
                                Firebase.storage.reference.child("messageImage/$chatConnectionId/$messageId/$messageId$index.png")
                                    .delete().addOnSuccessListener { // 사진 삭제
                                    }.addOnFailureListener {
                                    }
                            }
                            FBRef.messageRef.child(messageList[position].chatConnectionId)
                                .child(messageList[position].messageId)
                                .removeValue() // 파이어베이스에서 해당 메시지 삭제
                            Toast.makeText(view!!.context, "메시지가 삭제되었습니다!", Toast.LENGTH_SHORT)
                                .show()
                        }

                        alertDialog.dismiss()
                    }

                    val showMessageBtn = alertDialog.findViewById<Button>(R.id.showMessageBtn)
                    showMessageBtn?.setOnClickListener {  // 메시지 보기 버튼 클릭 시
                        Log.d("messageClicked", "show Message Button Clicked")

                        val intent = Intent(view!!.context, ImageDetailActivity::class.java)
                        Log.d("imageDataList", imageDataList[p])
                        intent.putExtra("image", imageDataList[p]) // 사진 링크 넘기기
                        view!!.context.startActivity(intent)

                        alertDialog.dismiss()
                    }
                }
            }
        })

        view!!.findViewById<TextView>(R.id.contentArea).setOnLongClickListener {

            if (messageList[position].sendUid == myUid) { // 메시지를 작성한 사람이 현재 사용자일 경우

                val mDialogView =
                    LayoutInflater.from(view!!.context).inflate(R.layout.message_dialog, null)
                val mBuilder = AlertDialog.Builder(view!!.context).setView(mDialogView)

                val alertDialog = mBuilder.show()
                val deleteMessageBtn = alertDialog.findViewById<Button>(R.id.deleteMessageBtn)
                deleteMessageBtn?.setOnClickListener { // 메시지 삭제 버튼 클릭 시
                    Log.d("messageClicked", "delete Message Button Clicked")

                    FBRef.messageRef.child(messageList[position].chatConnectionId)
                        .child(messageList[position].messageId).removeValue() // 파이어베이스에서 해당 메시지 삭제
                    Toast.makeText(view!!.context, "메시지가 삭제되었습니다!", Toast.LENGTH_SHORT).show()

                    alertDialog.dismiss()
                }

                val copyMessageBtn = alertDialog.findViewById<Button>(R.id.copyMessageBtn)
                copyMessageBtn?.setOnClickListener {  // 메시지 복사 버튼 클릭 시
                    Log.d("messageClicked", "copy Message Button Clicked")

                    val clipboard =
                        view?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText(
                        "text",
                        view!!.findViewById<TextView>(R.id.contentArea).text.trim()
                    )
                    clipboard.setPrimaryClip(clipData)
                    Toast.makeText(view!!.context, "메시지가 복사되었습니다!", Toast.LENGTH_SHORT).show()

                    alertDialog.dismiss()
                }
            } else if (messageList[position].sendUid != myUid) { // 메시지를 작성한 사람이 현재 사용자가 아닐 경우
                val clipboard =
                    view?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText(
                    "text",
                    view!!.findViewById<TextView>(R.id.contentArea).text.trim()
                )
                clipboard.setPrimaryClip(clipData)
                Toast.makeText(view!!.context, "메시지가 복사되었습니다!", Toast.LENGTH_SHORT).show()
            }

            true
        }

        return view!!
    }
}