package ca.uwaterloo.mrafuse.locationplugin.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.android.gms.maps.model.LatLng

class Database(context: Context) : SQLiteOpenHelper(context, "locationdb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY AUTOINCREMENT, latitude REAL, longitude REAL)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS locations");

        onCreate(db)
    }

    fun addLocation(location: LatLng) {
        val vals = ContentValues()
        vals.put("latitude", location.latitude)
        vals.put("longitude", location.longitude)

        writableDatabase.insert("locations", null, vals)
    }

    fun removeLocations() {
        writableDatabase.execSQL("delete from locations")
    }

    fun getLocations(): Array<LatLng> {
        val cursor = readableDatabase.query("locations", arrayOf("latitude", "longitude"), null, null, null, null, null)

        if (cursor.count == 0) {
            return arrayOf(LatLng(0.0, 0.0))
        }

        val result = arrayListOf<LatLng>()
        while (cursor.moveToNext()) {
            result.add(LatLng(
                    cursor.getDouble(cursor.getColumnIndex("latitude")),
                    cursor.getDouble(cursor.getColumnIndex("longitude"))
            ))
        }

        cursor.close()

        return result.toTypedArray()
    }

    companion object {
        private val LOG_TAG = Database::class.java.simpleName
        private var field: Database? = null
        fun getInstance(context: Context): Database {
            if (field == null) {
                Log.i(LOG_TAG, "field is null")
                field = Database(context)
            }
            return field!!
        }
    }
}