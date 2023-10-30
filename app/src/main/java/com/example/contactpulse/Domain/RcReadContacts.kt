package com.example.contactpulse.Domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contactpulse.UI.ReadContacts
import com.example.contactpulse.databinding.RcContactsShowDesignBinding
import java.util.Locale

class RcReadContacts(private val originalContactList: ArrayList<ReadContacts.Contact>, private val context: Context) : RecyclerView.Adapter<RcReadContacts.MyViewHolder>() {

    private var filteredContactList = ArrayList(originalContactList)

    inner class MyViewHolder(val binding: RcContactsShowDesignBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = RcContactsShowDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredContactList.size
    }

    fun filterList(query: ArrayList<ReadContacts.Contact>) {
        filteredContactList.clear()
        if (query.isEmpty()) {
            filteredContactList.addAll(originalContactList)
        } else {

            for (contact in query) {
                filteredContactList.add(contact)
            }
        }
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredContactList[position]
        holder.binding.apply {
            userName.text = currentItem.name
            userNumber.text = currentItem.phoneNumber

            userConstraint.setOnClickListener {
                context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${currentItem.phoneNumber}")))
            }
        }
    }
}
