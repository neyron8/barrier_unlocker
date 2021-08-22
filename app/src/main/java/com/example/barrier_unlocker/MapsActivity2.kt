package com.example.barrier_unlocker

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request.Method.GET
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.barrier_unlocker.permissions.TrackingPerm
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class MapsActivity2 : AppCompatActivity(), OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps2)
        TrackingPerm.requestPermissions(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        this.googleMap?.isBuildingsEnabled = true
        getMyLocation()
    }

    private fun draw_path(myLoc: Location) {
        val coords = intent.getStringExtra("Coords")

        val originLat = myLoc.latitude
        val originLon = myLoc.longitude
        val latLngOrigin = LatLng(originLat, originLon)

        val destLat = coords?.split(",")?.get(0)
        val destLon = coords?.split(",")?.get(1)

        val latLngDestination =
            coords?.split(",")?.get(0)?.toDouble()
                ?.let { LatLng(it, coords.split(",")[1].toDouble()) }
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ay"))
        this.googleMap!!.addMarker(latLngDestination?.let {
            MarkerOptions().position(it).title("Place")
        })
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 15f))
        val path: MutableList<List<LatLng>> = ArrayList()
        Log.d("GoogleMap", "before isMyLocationEnabled")
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${originLat}" +
                    ",${originLon}&destination=${destLat}," +
                    "${destLon}&key=AIzaSyCpMmKm03l8SrNj-fayVpi9d6xvn4AnsqU"
        val directionsRequest =
            object : StringRequest(GET, urlDirections, Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                // Get routes
                val routes = jsonResponse.getJSONArray("routes")
                val legs = routes.getJSONObject(0).getJSONArray("legs")
                val steps = legs.getJSONObject(0).getJSONArray("steps")
                for (i in 0 until steps.length()) {
                    val points =
                        steps.getJSONObject(i).getJSONObject("polyline").getString("points")
                    path.add(PolyUtil.decode(points))
                }
                for (i in 0 until path.size) {
                    this.googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                }
            }, Response.ErrorListener {
            }) {}
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(directionsRequest)
    }

    @SuppressLint("MissingPermission")
    private fun getMyLocation() {

        var locationManager: LocationManager? = null
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000L,
            0f,
            locationListener
        )

    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            googleMap?.clear()
            draw_path(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "All permissions requested", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            TrackingPerm.requestPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
