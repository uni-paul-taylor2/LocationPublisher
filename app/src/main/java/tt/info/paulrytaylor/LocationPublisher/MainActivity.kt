package tt.info.paulrytaylor.LocationPublisher
//change isPublisher on PermissionActivity AND MainActivity to switch between apps
//in logcat, set the filter to... package:mine tag:Publisher tag:Subscriber

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import tt.info.paulrytaylor.LocationPublisher.controllers.Publisher
import tt.info.paulrytaylor.LocationPublisher.controllers.Subscriber
import tt.info.paulrytaylor.LocationPublisher.models.LocationModel
import java.security.MessageDigest

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private val isPublisher: Boolean = false //yes, a flag to decide which app it will be (publisher or subscriber)
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null
    private var selectedStudentID: String? = null
    private var cachedLocations: List<LocationModel>? = null
    private lateinit var mMap: GoogleMap

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //also set the interval for updateMap here, perfect :D
        if(isPublisher) return;
        val handler = Handler(Looper.getMainLooper())
        val updateRunnable = object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                updateMap()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(!isPublisher){
            subscriber = Subscriber(this)
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
        updateUI()
    }

    private fun updateUI(){
        findViewById<ConstraintLayout>(R.id.publisher).visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.subscriber).visibility = View.GONE
        //above has constraintlayout views that are going to be set to invisible at first

        //now to set visibility for a specific constraintlayout view based on whatever logic
        if(isPublisher){
            findViewById<ConstraintLayout>(R.id.publisher).visibility = View.VISIBLE
            return //well yeah, publisher just has this one view, word
        }
        //subscriber logic would go below here I guess
        findViewById<ConstraintLayout>(R.id.subscriber).visibility = View.VISIBLE
        if(selectedStudentID==null) {
            //change the view to the default screen
        }
        else{
            //change the view to the view of an individual student
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMap(){
        //this function is to run in an interval
        //this would read from the database because subscriber takes from mqtt and writes to database
        //after checking the selectedStudentID, it will determine what data to get from the database to handle
        val locations: List<LocationModel> =
            if(selectedStudentID==null){subscriber?.database?.getAllRecentLocations()!!}
            else{subscriber?.database?.getLocations(selectedStudentID!!)!!}
        if(!locationListsEqual(locations)){
            cachedLocations = locations
            mMap.clear() //clear before redrawing...
            drawGraph(locations)
        }
    }
    private fun locationListsEqual(currentBatch: List<LocationModel>): Boolean {
        if(cachedLocations==null) return false;
        for(i in currentBatch.indices){
            if(!cachedLocations!![i].equals(currentBatch[i])) return false;
        }
        return true;
    }
    private fun drawGraph(locations: List<LocationModel>){
        //assign a colour for an id then append the next point then draw a line.. the steps in a sentence
        var prevLocation: LocationModel? = null
        val polyLineList: MutableList<LatLng> = mutableListOf() //for locations of a unique student
        val entirePolyLineList: MutableList<LatLng> = mutableListOf() //for all locations

        for(location in locations){
            val position: LatLng = LatLng(location.latitude,location.longitude)
            entirePolyLineList.add(position)
            //now to use the graph api with the location and colour and get the drawing party started
            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title("User ${location.student_id} @ ${location.time}")
            )
            if(prevLocation!==null && prevLocation.student_id!=location.student_id){
                //draw a polyline of the all the previous student_id locations here
                val digest: ByteArray = MessageDigest.getInstance("SHA-256")
                    .digest(prevLocation.student_id.toByteArray())
                val colour: String = "%02x".format(digest).substring(0,6)
                mMap.addPolyline(
                    PolylineOptions()
                        .addAll(polyLineList)
                        .color(Color.parseColor("#$colour"))
                        .width(5f).geodesic(true)
                )
                polyLineList.clear()
            }
            polyLineList.add(position)
            prevLocation = location
        }

        val bounds = LatLngBounds.builder()
        entirePolyLineList.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))

        if(polyLineList.size > 1){
            //draw the last polyline :D
            val digest: ByteArray = MessageDigest.getInstance("SHA-256")
                .digest(prevLocation!!.student_id.toByteArray())
            val colour: String = digest.joinToString("") { "%02x".format(it) }.substring(0,6)
            mMap.addPolyline(
                PolylineOptions()
                    .addAll(polyLineList)
                    .color(Color.parseColor("#$colour"))
                    .width(5f).geodesic(true)
            )
            polyLineList.clear()
        }
    }

    fun startPublishing(view: View){
        val studentID: String = findViewById<EditText>(R.id.studentID).text.toString()
        val validStudentID: Boolean = studentID.length == 9 && studentID.startsWith("81")
        if(!validStudentID){
            val toast: Toast = Toast.makeText(this,"Given Student ID is NOT valid",Toast.LENGTH_SHORT)
            toast.show()
            return
        }
        val toast: Toast = Toast.makeText(this,"Publishing Started",Toast.LENGTH_SHORT)
        toast.show()
        if(publisher==null || publisher?.isFailed()!!){
            publisher = null //explicitly set it to null to avoid memory leakage because I am a nerd
            publisher=Publisher(this,studentID)
            toast.show()
            return
        }
        publisher?.resume()
        publisher?.setStudentID(studentID)
        toast.show()
    }
    fun stopPublishing(view: View){
        publisher?.halt()
        val toast: Toast = Toast.makeText(this,"Publishing Stopped",Toast.LENGTH_SHORT)
        toast.show()
    }
}