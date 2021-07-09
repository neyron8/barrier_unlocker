package com.example.barrier_unlocker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.example.barrier_unlocker.db.UserEntity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_main.*


private const val requestSendSms: Int = 2
private const val requestCallPhone: Int = 2

class MainActivity : AppCompatActivity(), RecyclerViewAdapter.RowClickListener {

    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkpermissons()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerViewAdapter = RecyclerViewAdapter(this@MainActivity)
            adapter = recyclerViewAdapter
            val divider = DividerItemDecoration(applicationContext, VERTICAL)
            addItemDecoration(divider)
        }

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.getAllUsersObservers().observe(this, Observer {
            recyclerViewAdapter.setListData(ArrayList(it))
            recyclerViewAdapter.notifyDataSetChanged()
        })

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCpMmKm03l8SrNj-fayVpi9d6xvn4AnsqU")
        }

        srch_btn.setOnClickListener {
            val fields = listOf(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
            startActivityForResult(intent, 100)
        }

        saveButton.setOnClickListener {
            if (nameInput.text.toString() == "" || phoneInput.text.toString() == "" || place_text.text.toString() == "") {
                Toast.makeText(
                        applicationContext,
                        "One or more fields are empty!",
                        Toast.LENGTH_SHORT
                ).show()
            } else {
                val name = nameInput.text.toString()
                val phone = phoneInput.text.toString()
                val location = place_text.text.toString()
                if (saveButton.text.equals("Save")) {
                    val user = UserEntity(0, name, phone, location)
                    viewModel.insertUserInfo(user)
                } else {
                    val user = UserEntity(
                            nameInput.getTag(nameInput.id).toString().toInt(),
                            name,
                            phone,
                            location
                    )
                    viewModel.updateUserInfo(user)
                    saveButton.text = "Save"
                }
                nameInput.setText("")
                phoneInput.setText("")
                place_text.setText("")
            }
        }

    }

    private fun checkpermissons() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    requestSendSms
            )
        }

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CALL_PHONE),
                    requestCallPhone
            )
        }

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    102
            )
        }

        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
            )
        }
    }

    override fun onMapPathClickListener(latlng: String) {
        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra("Coords", latlng)
        startActivity(intent)
    }
    override fun onDeleteUserClickListener(user: UserEntity) {
        viewModel.deleteUserInfo(user)
    }

    override fun onCallUserClickListener(user: UserEntity) {

        if (SMS.isChecked) {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:${user.phone}")
            this.startActivity(intent)
        } else {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(user.phone, null, "Texting you)", null, null)
            Toast.makeText(this, user.phone, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClickListener(user: UserEntity) {
        nameInput.setText(user.name)
        phoneInput.setText(user.phone)
        nameInput.setTag(nameInput.id, user.id)
        place_text.text = user.location
        saveButton.text = "Update"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        place_text.text = place.latLng.toString().drop(10).dropLast(1)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                }
                Activity.RESULT_CANCELED -> {
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}