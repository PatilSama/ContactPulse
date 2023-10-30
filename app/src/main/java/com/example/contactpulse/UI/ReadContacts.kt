package com.example.contactpulse.UI

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactpulse.Domain.RcReadContacts
import com.example.contactpulse.databinding.ReadContactsLayoutBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class ReadContacts : AppCompatActivity() {

    private val REQUEST_CONTACTS = 1

    val contactsList: ArrayList<Contact> = ArrayList()
    private lateinit var rcReadContacts: RcReadContacts

    data class Contact(val name: String, val phoneNumber: String)

    // view binding for the activity
    private var _binding: ReadContactsLayoutBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ReadContactsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reWriteContacts.setHasFixedSize(true)

        permissionReadContacts();
        searchContacts()
    }

    private fun searchContacts() {
        binding.searchContacts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                filterData(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }
    private fun filterData(querydar: String) {
        val filteredList = ArrayList<Contact>()
        for(contact in contactsList)
        {
            if(contact.name.contains(querydar, ignoreCase = true))
            {
                filteredList.add(Contact(contact.name,contact.phoneNumber))
            }
        }

//        rcReadContacts.filterList(filteredList)
        // Check if the filteredList contains any items before passing it to the adapter
        if (filteredList.isNotEmpty()) {
            rcReadContacts.filterList(filteredList)
        } else {
            // If filteredList is empty, pass the original contactsList to show all items
            rcReadContacts.filterList(contactsList)
        }
    }

    @SuppressLint("Range")
    private fun readAndStoreContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val contact = Contact(name, phoneNumber)
                contactsList.add(contact)
            }
        }
        // Sort the contacts list in ascending order based on contact names
        contactsList.sortBy { it.name }

        rcReadContacts = RcReadContacts(contactsList,this)
        binding.reWriteContacts.adapter = rcReadContacts
        binding.reWriteContacts.layoutManager = LinearLayoutManager(this)

        if (contactsList.size != 0) {
            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("contacts")

            var deviceID =
                Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
            // Push the contactsList to Firebase under a unique key
            val uniqueKey: String = databaseReference.push().key!!
            databaseReference.child(deviceID).setValue(contactsList)
                .addOnSuccessListener {
                    // Contacts successfully stored in Firebase
//                    Toast.makeText(this, "Data Successful", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle the error
//                    Toast.makeText(this, "Data Fail", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No Contacts", Toast.LENGTH_SHORT).show()
        }
    }

    // Access Grant Permission.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED||grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, read and store contacts
                readAndStoreContacts()
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Get Read Contacts Permission at runtime.
    private fun permissionReadContacts() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE),
                REQUEST_CONTACTS
            )
        } else {
            // Permission is already granted
            readAndStoreContacts()
        }
    }
}

