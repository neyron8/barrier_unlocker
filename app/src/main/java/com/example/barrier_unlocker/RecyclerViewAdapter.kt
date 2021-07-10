package com.example.barrier_unlocker

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.barrier_unlocker.db.UserEntity
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.recyclerview_row.view.*

class RecyclerViewAdapter(val listener: RowClickListener) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    var items = ArrayList<UserEntity>()

    fun setListData(data: ArrayList<UserEntity>) {
        this.items = data
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater =
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_row, parent, false)
        return MyViewHolder(inflater, listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            listener.onItemClickListener(items[position])
        }
        holder.bind(items[position])
    }

    class MyViewHolder(view: View, val listener: RowClickListener) : RecyclerView.ViewHolder(view) {

        val tvName = view.tvName
        val tvPhone = view.tvPhone
        val mapPath = view.mapPath_btn
        val deleteUserID = view.deleteUserID
        val callUser = view.callUser

        fun bind(data: UserEntity) {
            tvName.text = data.name
            tvPhone.text = data.phone

            deleteUserID.setOnClickListener {
                listener.onDeleteUserClickListener(data)
            }

            callUser.setOnClickListener {
                listener.onCallUserClickListener(data)
            }

            mapPath.setOnClickListener {
                data.location?.let { it1 -> listener.onMapPathClickListener(it1) }
            }
        }
    }

    interface RowClickListener {
        fun onDeleteUserClickListener(user: UserEntity)
        fun onItemClickListener(user: UserEntity)
        fun onCallUserClickListener(user: UserEntity)
        fun onMapPathClickListener(latlng: String)
    }
}