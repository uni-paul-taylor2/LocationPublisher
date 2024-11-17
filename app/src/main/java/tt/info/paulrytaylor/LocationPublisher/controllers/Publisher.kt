package tt.info.paulrytaylor.LocationPublisher.controllers

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import tt.info.paulrytaylor.LocationPublisher.models.LocationModel
import java.util.UUID
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class Publisher {
    private var mqtt: Mqtt5BlockingClient? = null
    private var locationManager: LocationManager? = null
    private var student_id: String = ""
    private var doNotPublish: Boolean = false
    private var brokerConnectionFailed: Boolean = false
    private val locationListener = object : LocationListener {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationChanged(location: Location) {
            if(location.hasSpeed())
                Log.v("Publisher","Your device's location INCLUDES speed as a measure");
            else
                Log.v("Publisher","Your device's location does NOT have speed as a measure");
            publish(
                LocationModel(location.time,student_id,location.speed.toDouble(),location.latitude,location.longitude)
            )
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    constructor(context: Context, studentID: String){
        student_id = studentID
        mqtt = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.sundaebytestt.com")
            .serverPort(1883)
            .build().toBlocking()
        try{ mqtt?.connect() }
        catch(e: Exception){
            brokerConnectionFailed = true
            val toast: Toast = Toast.makeText(context,"Connection Error; Please Try Again",Toast.LENGTH_SHORT)
            toast.show()
            return
        }
        locationManager = ContextCompat.getSystemService(context,LocationManager::class.java) as LocationManager
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            5000L, // Update interval in milliseconds
            10f, // Minimum distance in meters between updates
            locationListener
        )
    }
    private fun publish(location: LocationModel) {
        if(doNotPublish) return;
        Log.v("Publisher","${location.student_id}\t${location.speed}\t${location.latitude}\t${location.longitude}")
        mqtt?.publishWith()?.topic("assignment/location")?.payload(location.bytes!!)?.send()
    }
    fun resume(){
        doNotPublish = false
    }
    fun halt(){
        doNotPublish = true
    }
    fun setStudentID(studentID: String){
        student_id = studentID
    }
    fun isFailed(): Boolean{
        return brokerConnectionFailed
    }
}