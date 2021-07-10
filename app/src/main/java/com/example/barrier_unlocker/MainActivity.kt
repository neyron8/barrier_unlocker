package com.example.barrier_unlocker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.example.barrier_unlocker.permissions.TrackingPerm
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

const val REQUEST_CODE_APP_PERMISSION = 10


class MainActivity : AppCompatActivity(), RecyclerViewAdapter.RowClickListener, EasyPermissions.PermissionCallbacks {

    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    lateinit var viewModel: MainActivityViewModel

    //////////////////////////////////////////////////////////
    // PERMISSIONS///////////////////////////////////////////
    private fun requestPermissions() {
        if (TrackingPerm.hasUsefulPermissions(this)) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app",
            REQUEST_CODE_APP_PERMISSION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "All permissions requested", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
    //////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions()

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerViewAdapter = RecyclerViewAdapter(this@MainActivity)
            adapter = recyclerViewAdapter
        }

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.getAllUsersObservers().observe(this, {
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
            if (nameInput.text.toString() == "" || phoneInput.text.toString() == "") { //|| place_text.text.toString() == ""
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


    override fun onMapPathClickListener(latlng: String) {
        val intent = Intent(this, MapsActivity2::class.java)
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