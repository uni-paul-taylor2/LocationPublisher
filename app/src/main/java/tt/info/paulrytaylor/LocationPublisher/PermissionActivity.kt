package tt.info.paulrytaylor.LocationPublisher
//change isPublisher on PermissionActivity AND MainActivity to switch between apps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PermissionActivity : AppCompatActivity() {
    /// The [requestCode] variable acts as an identifier for the app that's requesting the permissions.
    private val requestCode = 1234
    private val isPublisher: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_permission)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // If we do not have our permissions, we need to request them. Create an array of the permissions we want,
        // then send a request to the android OS
        if (isPublisher || !hasAllPermissions()){
            val perm = arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, perm, requestCode)

        } else {
            navigateToNextPage()
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAllPermissions()){
            navigateToNextPage()
        }
    }

    private fun hasAllPermissions():Boolean{
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
    }

    /// This function is called by the OS itself after the user interacts with the permissions popups.
    /// We need to iterate through each of the permissions we requested and make sure that ALL are granted.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            this.requestCode -> {
                if (hasAllPermissions()){
                    navigateToNextPage()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun navigateToNextPage(){
        val i = Intent(this,MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(i)
    }


    fun goToSettings(view: View) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

}