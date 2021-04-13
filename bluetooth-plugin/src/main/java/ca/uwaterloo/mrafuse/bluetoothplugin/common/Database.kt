package ca.uwaterloo.mrafuse.bluetoothplugin.common

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, "bluetoothdb", null, 4) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE addresses (id INTEGER PRIMARY KEY AUTOINCREMENT, address TEXT, seen INTEGER, ignored INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS addresses");

        onCreate(db)
    }

    fun addAddress(address: String, ignored: Boolean = false) {
        val vals = ContentValues()
        vals.put("address", address)
        vals.put("seen", System.currentTimeMillis())
        vals.put("ignored", if (ignored) 1 else 0)

        writableDatabase.insert("addresses", null, vals)
    }

    fun ignoreAddress(address: String) {
        val vals = ContentValues()
        vals.put("ignored", 1)
        writableDatabase.update("addresses", vals, "address = ?", arrayOf(address))
    }

    fun hasAddress(address: String): Boolean {
        val cursor = readableDatabase.query("addresses", arrayOf("address"), "address=?", arrayOf(address), null, null, null, "1")
        val count = cursor.count
        cursor.close()
        return count > 0
    }

    fun getAddress(address: String): Array<Address> {
        return getAddresses().filter { it.address == address }.toTypedArray()
    }

    fun getAddresses(): Array<Address> {
        val cursor = readableDatabase.query("addresses", arrayOf("address", "seen"), "ignored=0", null, null, null, null)

        if (cursor.count == 0) {
            return arrayOf()
        }

        val result = arrayListOf<Address>()
        while (cursor.moveToNext()) {
            result.add(Address(
                    cursor.getString(cursor.getColumnIndex("address")),
                    cursor.getLong(cursor.getColumnIndex("seen"))
            ))
        }

        cursor.close()

        return result.toTypedArray()
    }

    inner class Address(val address: String, val seen: Long) {
    }
}