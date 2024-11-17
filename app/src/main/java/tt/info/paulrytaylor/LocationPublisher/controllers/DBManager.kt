package tt.info.paulrytaylor.LocationPublisher.controllers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import androidx.annotation.RequiresApi
import tt.info.paulrytaylor.LocationPublisher.models.LocationModel

class DBManager(context: Context, factory: SQLiteDatabase.CursorFactory?): SQLiteOpenHelper(context, "MQTTLocationPublisher.db",factory, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createLocationsTable = """
        CREATE TABLE IF NOT EXISTS Locations (
            id INTEGER PRIMARY KEY AUTOINCREMENT, 
            ms_time LONG, 
            student_id TEXT,
            speed DOUBLE, 
            latitude DOUBLE, 
            longitude DOUBLE
            CHECK (LENGTH(student_id) = 9)
        );
        """
        db.execSQL(createLocationsTable)
    }
    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeListOf(cursor: Cursor): List<LocationModel> {
        val result: MutableList<LocationModel> = mutableListOf()
        if(!cursor.moveToFirst() || cursor.count==0) return result;
        do{
            result.add(
                LocationModel(
                    cursor.getLong( cursor.getColumnIndex("ms_time") ),
                    cursor.getString( cursor.getColumnIndex("student_id") ),
                    cursor.getDouble( cursor.getColumnIndex("speed") ),
                    cursor.getDouble( cursor.getColumnIndex("latitude") ),
                    cursor.getDouble( cursor.getColumnIndex("longitude") )
                )
            )
        }while(cursor.moveToNext());
        return result
    }


    fun insertLocation(location: LocationModel) {
        val row = ContentValues()
        row.put("student_id",location.student_id)
        row.put("ms_time",location.ms_time)
        row.put("speed",location.speed)
        row.put("latitude",location.latitude)
        row.put("longitude",location.longitude)
        val db=this.writableDatabase
        db.insert("Locations",null,row)
        db.close()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocations(student_id: String): List<LocationModel> {
        val db = this.readableDatabase
        val result = makeListOf(
            db.rawQuery("SELECT * from Locations WHERE student_id = ? ORDER BY ms_time asc", arrayOf(student_id))
        )
        db.close()
        return result
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllRecentLocations(): List<LocationModel> {
        val db = this.readableDatabase
        val fiveMinsAgo = System.currentTimeMillis()-(1000*60*5)
        val result = makeListOf(
            db.rawQuery("SELECT * from Locations WHERE ms_time >= ? ORDER BY student_id asc,ms_time asc", arrayOf(fiveMinsAgo.toString()))
        )
        db.close()
        return result
    }
}