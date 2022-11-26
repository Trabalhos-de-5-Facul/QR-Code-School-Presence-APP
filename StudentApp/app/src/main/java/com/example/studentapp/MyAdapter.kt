package com.example.studentapp

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.class_info_card.view.*

class MyAdapter(val arrayList: ArrayList<Model>, val context: Context) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindItems(model: Model){
            itemView.titleTv.text = model.title
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.class_info_card, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems((arrayList[position]))
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }


}