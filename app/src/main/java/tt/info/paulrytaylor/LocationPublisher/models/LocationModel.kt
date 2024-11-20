package tt.info.paulrytaylor.LocationPublisher.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocationModel {
    /*
        publisher usage: send( LocationModel(student_id, speed, latitude, longitude).bytes )
        subscriber usage: val datapoint = LocationModel( received_byte_array )
    */
    /*
        the format of a published payload is:
        id,ms_time,speed,latitude,longitude
        where each name above represents a placeholder of a set amount of bytes as shown below respectively
        first 4 bytes, next 8 bytes, next 8 bytes, next 8 bytes, last 8 bytes

        there are more variables below (that are just interpreted from the ones described above
        these are to take some of the above variables and store a more desired form of them
        like time, the string, human readable form of ms_time
        or student_id, the string, human readable form of id
        or kmph, the number, the kilometers per hour version of the meters per second speed

        AND, the bytes variable is the encoded byte array form of data (to send as publisher)
    */

    var id: UInt = 0u
    var ms_time: Long = 0
    var speed: Double = 0.0
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.O)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault())
    var time: String = ""
    var student_id: String = ""
    var kmph: Double = 0.0
    var bytes: ByteArray? = null

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(MS: Long, ID_: String, SPEED: Double, LATITUDE: Double, LONGITUDE: Double){
        //pulling from database and would run this constructor
        val ID = ID_.toInt().toUInt()
        val buffer = ByteBuffer.allocate(4+8+8+8+8)
        buffer.putInt(ID.toInt())
        buffer.putLong(MS)
        buffer.putDouble(SPEED)
        buffer.putDouble(LATITUDE)
        buffer.putDouble(LONGITUDE)
        bytes = buffer.array()
        load()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(ID_: String, SPEED: Double, LATITUDE: Double, LONGITUDE: Double){
        //publisher or pushing to broker would run this constructor
        val MS = System.currentTimeMillis() //gets current date
        val ID = ID_.toInt().toUInt()
        val buffer = ByteBuffer.allocate(4+8+8+8+8)
        buffer.putInt(ID.toInt())
        buffer.putLong(MS)
        buffer.putDouble(SPEED)
        buffer.putDouble(LATITUDE)
        buffer.putDouble(LONGITUDE)
        bytes = buffer.array()
        load()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    constructor(BYTES: ByteArray){
        //subscriber or pulling from broker would run this constructor
        bytes = BYTES
        load()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(){
        val buffer = ByteBuffer.wrap(bytes!!)
        id = buffer.getInt().toUInt()
        ms_time = buffer.getLong()
        speed = buffer.getDouble()
        latitude = buffer.getDouble()
        longitude = buffer.getDouble()
        //now to convert some that need more desirable forms of data
        time = formatter.format( Instant.ofEpochMilli(ms_time) )
        student_id = id.toString()
        kmph = speed * 3.6
    }
    fun equals(location: LocationModel): Boolean {
        return this.id==location.id && this.ms_time==location.ms_time && this.speed==location.speed
                && this.latitude==location.latitude && this.longitude==location.longitude
    }
}