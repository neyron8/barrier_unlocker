package com.example.barrier_unlocker

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request.Method.GET
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.maps.android.PolyUtil
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
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
            coords?.split(",")?.get(0)?.toDouble()?.let { LatLng(it, coords.split(",")[1].toDouble()) } // SM City
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ay"))
        this.googleMap!!.addMarker(latLngDestination?.let { MarkerOptions().position(it).title("Place") })
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 15f))
        val path: MutableList<List<LatLng>> = ArrayList()
        Log.d("GoogleMap", "before isMyLocationEnabled")
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${originLat},${originLon}&destination=${destLat},${destLon}&key=AIzaSyCpMmKm03l8SrNj-fayVpi9d6xvn4AnsqU"
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

    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        }
        var locationManager : LocationManager? = null
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 0f, locationListener )

        /*LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnCompleteListener {
            if (it.isSuccessful){
                var task1 = "${it.result.latitude},${it.result.longitude}"
                draw_path(task1)
            }
        }*/

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
}
