/**
 * Copyright 2020 - 2021
 *
 * Matthew Rafuse <matthew.rafuse@uwaterloo.ca>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.uwaterloo.mrafuse.context.framework.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.math.roundToInt

class Database(context: Context) : SQLiteOpenHelper(context, "frameworkdb", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE alphas (id INTEGER PRIMARY KEY AUTOINCREMENT, location TEXT, alpha INTEGER, seen INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS alphas");

        onCreate(db)
    }

    fun addLocation(location: String, alpha: Int): DbLocation {
        val vals = ContentValues()
        vals.put("location", location)
        vals.put("alpha", alpha)
        val seen = System.currentTimeMillis() / 1000 / 60 / 60 / 6;
        vals.put("seen", seen)

        writableDatabase.insert("alphas", null, vals)

        return DbLocation(location, alpha, seen.toInt())
    }

    fun hasLocation(location: String): Boolean {
        val cursor = readableDatabase.query("alphas", arrayOf("location"), "location=?", arrayOf(location), null, null, null, "1")
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    fun updateLocation(location: DbLocation, alpha: Int = location.alpha) {
        val vals = ContentValues()
        vals.put("location", location.latLng)
        vals.put("alpha", alpha)
        vals.put("seen", System.currentTimeMillis() / 1000 / 60 / 60 / 6)
        writableDatabase.update("alphas", vals, "location=?", arrayOf(location.toString()))
    }

    fun getLocation(location: String): DbLocation {
        val cursor = readableDatabase.query("alphas", arrayOf("location", "alpha", "seen"), "location=?", arrayOf(location), null, null, null, "1")
        cursor.moveToFirst()
        val result = DbLocation(
                cursor.getString(cursor.getColumnIndex("location")),
                cursor.getInt(cursor.getColumnIndex("alpha")),
                cursor.getInt(cursor.getColumnIndex("seen"))
        )
        cursor.close()

        return result
    }

    fun createKey(lat: Double, lng: Double): String {
        val latitude = ((lat * 10000.0).roundToInt().toDouble() / 10000.0).toString()
        val longitude = ((lng * 10000.0).roundToInt().toDouble() / 10000.0).toString()

        return "$latitude,$longitude"
    }

    inner class DbLocation(val latLng: String, val alpha: Int, val seen: Int) {

    }
}