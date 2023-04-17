package org.techtown.myproject.walk

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.techtown.myproject.R
import org.techtown.myproject.utils.DogModel
import org.techtown.myproject.utils.FBRef
import org.techtown.myproject.utils.WalkDogModel

class WithDogReVAdapter(val dogList : ArrayList<String>):
    RecyclerView.Adapter<WithDogReVAdapter.DogViewHolder>() {

    override fun getItemCount(): Int {
        return dogList.count()
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString() // 현재 로그인된 유저의 uid

        var dogKey = dogList[position]

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {

                    for (dataModel in dataSnapshot.children) {
                        val item = dataModel.getValue(DogModel::class.java)

                        if(dataModel!!.key == dogKey) {

                            val imageFile = item!!.dogProfileFile
                            val storageReference = Firebase.storage.reference.child(imageFile)
                            val imageView = holder.view?.findViewById<ImageView>(R.id.dogImage)
                            storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Glide.with(holder.view!!).load(task.result).into(imageView!!)
                                } else {
                                    // view?.findViewById<ImageView>(R.id.communityImage)!!.isVisible = false
                                }
                            })

                            val str = item!!.dogProfileFile
                            val split = str.split("/", ".")
                            Log.d("split", split.toString())
                        }
                    }
                } catch (e: Exception) {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        FBRef.dogRef.child(myUid).addValueEventListener(postListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view =
            LayoutInflater.from(parent!!.context).inflate(R.layout.walk_dog_pic_list_item, parent, false)
        return DogViewHolder(view)
    }

    inner class DogViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {
        val view = view
        val imageView = view?.findViewById<ImageView>(R.id.dogImage)
    }
}