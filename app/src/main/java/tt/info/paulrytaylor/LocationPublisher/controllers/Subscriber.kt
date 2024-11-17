package tt.info.paulrytaylor.LocationPublisher.controllers

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import tt.info.paulrytaylor.LocationPublisher.models.LocationModel
import java.util.UUID
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish

class Subscriber {
    private var mqtt: Mqtt5BlockingClient? = null
    private var brokerConnectionFailed: Boolean = false
    var database: DBManager? = null

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(context: Context){
        Log.v("Subscriber","pre database declaration")
        database = DBManager(context,null)
        Log.v("Subscriber","pre mqtt declaration")
        mqtt = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.sundaebytestt.com")
            .serverPort(1883)
            .build().toBlocking()
        Log.v("Subscriber","post mqtt declaration")
        try{ mqtt?.connect() }
        catch(e: Exception){
            brokerConnectionFailed = true
            val toast: Toast = Toast.makeText(context,"Connection Error; Please Try Again",Toast.LENGTH_SHORT)
            toast.show()
            return
        }
        Log.v("Subscriber","Subscriber ready")
        mqtt?.toAsync()?.subscribeWith()
            ?.topicFilter("assignment/location")
            ?.callback(this::onLocationReceived)
            ?.send()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun onLocationReceived(publish: Mqtt5Publish) {
        val location: LocationModel = LocationModel(publish.payloadAsBytes)
        Log.v("Subscriber", "${location.student_id}\t${location.speed}\t${location.latitude}\t${location.longitude}")
        database?.insertLocation(location)
    }
    fun isFailed(): Boolean{
        return brokerConnectionFailed
    }
}