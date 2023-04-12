package org.techtown.myproject.note

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.techtown.myproject.R
import org.techtown.myproject.comment.CommentEditActivity
import org.techtown.myproject.utils.DogMedicineModel
import org.techtown.myproject.utils.DogMedicinePlanModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.ReceiptModel
import java.text.DecimalFormat

class MedicinePlanReVAdapter(val dogMeidicinePlanList : ArrayList<DogMedicinePlanModel>, val dateList : ArrayList<String>):
    RecyclerView.Adapter<MedicinePlanReVAdapter.MedicinePlanViewHolder>() {

    override fun getItemCount(): Int {
        return dogMeidicinePlanList.count()
    }

    override fun onBindViewHolder(holder: MedicinePlanViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid
        val dogId = dogMeidicinePlanList[position].dogId // 현재 대표 반려견의 id
        val medicinePlanId = dogMeidicinePlanList[position].dogMedicinePlanId
        val nowDate = dateList[position] // 선택된 날짜를 가져옴
        val medicineName = dogMeidicinePlanList[position].medicineName

        Toast.makeText(holder!!.view!!.context, nowDate, Toast.LENGTH_SHORT).show()

        holder.repeatArea!!.text = dogMeidicinePlanList[position].repeat
        holder.timeArea!!.text = dogMeidicinePlanList[position].time
        holder.medicineNameArea!!.text = dogMeidicinePlanList[position].medicineName

        if(dogMeidicinePlanList[position].repeat == "매일") { // 투약 일정이 반복될 경우, 반복 기간이 보이도록 설정
            holder.dateArea!!.visibility = VISIBLE // 반복 기간이 보이도록 설정
            holder.startDateArea!!.text = dogMeidicinePlanList[position].startDate
            holder.endDateArea!!.text = dogMeidicinePlanList[position].endDate
        }

        val postListener = object : ValueEventListener { // 투약 기록이 있다면, 그 투약 기록에 해당되는 투약 일정에 체크 표시
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    var id = ""

                    for(dataModel in dataSnapshot.children) {
                        // dataModel.key
                        val item = dataModel.getValue(DogMedicineModel::class.java)

                        val nowDateSplit = nowDate.split(".")
                        val selectedDateSplit = item!!.date.split(".")

                        if(medicinePlanId == item!!.dogMedicinePlanId && medicineName == item!!.medicineName && nowDateSplit[0].toInt() == selectedDateSplit[0].toInt() && nowDateSplit[1].toInt() == selectedDateSplit[1].toInt() && nowDateSplit[2].toInt() == selectedDateSplit[2].toInt()) {
                            id = item!!.dogMedicineId
                            break
                        }
                    }

                    if(id != "") { // 현재 투약 일정에 해당되는 투약 기록이 존재한다면
                        holder!!.checkImage!!.visibility = VISIBLE // 체크 표시
                    }
                    else { // 현재 투약 일정에 해당되는 투약 기록이 존재하지 않는다면
                        holder!!.checkImage!!.visibility = INVISIBLE // 체크 표시 해제
                    }
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.medicineRef.child(myUid).child(dogId).addValueEventListener(postListener)

        holder!!.commentSet!!.setOnClickListener { View ->
            val mDialogView = LayoutInflater.from(holder!!.view!!.context).inflate(R.layout.medicine_plan_dialog, null)
            val mBuilder = AlertDialog.Builder(holder!!.view!!.context).setView(mDialogView)

            val alertDialog = mBuilder.show()

            val checkBtn = alertDialog.findViewById<Button>(R.id.checkBtn)
            checkBtn?.setOnClickListener { // 체크 버튼 클릭 시 -> 체크 표시 & 투약 기록 추가
                holder!!.checkImage!!.visibility = VISIBLE

                val key = FBRef.medicineRef.child(myUid).child(dogId).push().key.toString() // 키 값을 먼저 받아옴

                FBRef.medicineRef.child(myUid).child(dogId).child(key).setValue(DogMedicineModel(key, dogMeidicinePlanList[position].dogMedicinePlanId, dogId, dateList[position], dogMeidicinePlanList[position].time, dogMeidicinePlanList[position].medicineName)) // 반려견 투약 기록 정보 데이터베이스에 저장

                alertDialog.dismiss()
            }

            val cancelBtn = alertDialog.findViewById<Button>(R.id.cancelBtn)
            cancelBtn?.setOnClickListener { // 취소 버튼 클릭 시 -> 체크 표시 해제 & 투약 기록 삭제
                holder!!.checkImage!!.visibility = INVISIBLE

                Toast.makeText(holder!!.view!!.context, "취소", Toast.LENGTH_SHORT).show()

                val postCancleListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            var id = ""
//                            Toast.makeText(holder!!.view!!.context, "취소", Toast.LENGTH_SHORT).show()

                            for(dataModel in dataSnapshot.children) {
                                // dataModel.key
                                val item = dataModel.getValue(DogMedicineModel::class.java)

                                val nowDateSplit = nowDate.split(".")
                                val selectedDateSplit = item!!.date.split(".")

                                if(medicinePlanId == item!!.dogMedicinePlanId && nowDateSplit[0].toInt() == selectedDateSplit[0].toInt() && nowDateSplit[1].toInt() == selectedDateSplit[1].toInt() && nowDateSplit[2].toInt() == selectedDateSplit[2].toInt()) {
                                    id = item!!.dogMedicineId
                                    break
                                }
                            }

                            if(id != "")
                                FBRef.medicineRef.child(myUid).child(dogId).child(id).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                        } catch (e: Exception) {
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                FBRef.medicineRef.child(myUid).child(dogId).addValueEventListener(postCancleListener)

                alertDialog.dismiss()
            }

            val editBtn = alertDialog.findViewById<Button>(R.id.editBtn)
            editBtn?.setOnClickListener { // 수정 버튼 클릭 시
                val intent = Intent(holder!!.view!!.context, DogMedicinePlanEditActivity::class.java)
                intent.putExtra("id", dogMeidicinePlanList[position].dogMedicinePlanId) // dogMedicinePlan id 전송
                holder!!.view!!.context.startActivity(intent) // 수정 페이지로 이동

                alertDialog.dismiss()
            }

            val rmBtn = alertDialog.findViewById<Button>(R.id.rmBtn)
            rmBtn?.setOnClickListener {  // 삭제 버튼 클릭 시
                alertDialog.dismiss()

                val postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            var itemList = ArrayList<String>()

                            for(dataModel in dataSnapshot.children) {
                                // dataModel.key
                                val item = dataModel.getValue(DogMedicineModel::class.java)

                                if(medicinePlanId == item!!.dogMedicinePlanId) {
                                    itemList.add(item!!.dogMedicineId)
                                }
                            }

                            if(itemList.size != 0)
                                for(i in 0 until itemList.size)
                                    FBRef.medicineRef.child(myUid).child(dogId).child(itemList[i]).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
                        } catch (e: Exception) {
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                FBRef.medicineRef.child(myUid).child(dogId).addValueEventListener(postListener)

                FBRef.medicinePlanRef.child(myUid).child(dogMeidicinePlanList[position].dogId).child(dogMeidicinePlanList[position].dogMedicinePlanId).removeValue() // 파이어베이스에서 해당 기록의 데이터 삭제
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicinePlanViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.medicine_plan_list_item, parent, false)
        return MedicinePlanViewHolder(view)
    }

    inner class MedicinePlanViewHolder(view : View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val repeatArea = view?.findViewById<TextView>(R.id.repeatArea)
        val timeArea = view?.findViewById<TextView>(R.id.timeArea)
        val dateArea = view?.findViewById<LinearLayout>(R.id.dateArea)
        val startDateArea = view?.findViewById<TextView>(R.id.startDateArea)
        val endDateArea = view?.findViewById<TextView>(R.id.endDateArea)
        val medicineNameArea = view?.findViewById<TextView>(R.id.medicineNameArea)
        val commentSet = view?.findViewById<ImageView>(R.id.commentSet)
        val checkImage = view?.findViewById<ImageView>(R.id.checkImage)
    }
}