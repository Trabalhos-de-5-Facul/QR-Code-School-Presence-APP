package com.example.studentapp

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyAdapter(private val context : Activity, private val arrayList : ArrayList<StudantProfile>)  : ArrayAdapter<StudantProfile>(context,
                R.layout.list_item,arrayList){


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view : View = inflater.inflate(R.layout.list_item, null)

        val username : TextView = view.findViewById(R.id.personName)

        username.text = arrayList[position].name

        return view
    }


}