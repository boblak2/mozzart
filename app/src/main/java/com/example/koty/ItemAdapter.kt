package com.example.koty

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ItemAdapter(context: Context, private val resource: Int, private val objects: MutableList<Item>) : ArrayAdapter<Item>(context, resource, objects) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //return super.getView(position, convertView, parent)
        //var view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false)
        //val act = context as Activity
        //val view = act.layoutInflater.inflate(R.layout.row_item, null)
        var view = LayoutInflater.from(context).inflate(R.layout.row_item, null)

        val tvName = view.findViewById<TextView>(R.id.tvItemName)
        val tvAge = view.findViewById<TextView>(R.id.tvItemAge)

        val item = objects[position]

        tvName.text = item.name
        tvAge.text = item.path

        return view;
    }

    override fun getCount(): Int {
        return objects.size
    }
}