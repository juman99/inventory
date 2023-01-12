package com.example.inventorymanager
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.lang.IllegalArgumentException
import java.util.HashMap

class InventoryProvider : ContentProvider(){

    companion object {
        val PROVIDER_NAME = "com.example.InventoryManager.InventoryProvider"
        val URL = "content://" + PROVIDER_NAME + "/Inventory"
        val CONTENT_URI = Uri.parse(URL)

        //Columns
        val _id="id";
        val name="name";
        val buy_price="buy_price";
        val sell_price="sell_price";
        val supplier="supplier";
        val quantity="quantity";

        private val INVENTORY_PROJECTION_MAP: HashMap<String, String>? = null
        val INVENTORY = 1
        val ITEM_ID = 2
        val uriMatcher: UriMatcher? = null
        val DATABASE_NAME = "store"
        val INVENTORY_TABLE_NAME = "inventory"
        val DATABASE_VERSION = 1
        val CREATE_DB_TABLE = buildString {
        append("CREATE TABLE \"inventory\" (")
        append("\"${_id}\"INTEGER,\n")
        append("\"${name}\"TEXT NOT NULL,\n")
        append("\"${buy_price}\"REAL NOT NULL,\n")
        append("\"${sell_price}\"REAL NOT NULL,\n")
        append("\"${supplier}\"TEXT,\n")
        append("\"${quantity}\"INTEGER NOT NULL,\n")
        append("PRIMARY KEY(\"${_id}\")\n")
        append(");")
    }
    }

    private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH);
    init
    {
        sUriMatcher.addURI(PROVIDER_NAME, "students", INVENTORY);
        sUriMatcher.addURI(PROVIDER_NAME, "students/#", ITEM_ID);
    }

    private var db: SQLiteDatabase? = null

    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            TODO("I don't think there's anything to do here but just in case")
        }
    }

        override fun onCreate(): Boolean {
    val context = context
    val dbHelper = DatabaseHelper(context)

    db = dbHelper.writableDatabase
    return db != null

}


    override fun query(
        uri: Uri,
        projection : Array<out String>?,
        selection : String?,
        selectionArgs : Array<out String>?,
        sortOrder : String?
    ): Cursor? {
        var sOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = INVENTORY_TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            ITEM_ID -> qb.appendWhere(_id + "=" + uri.pathSegments[1])
            else -> { null
            }
        }
        if (sortOrder == null || sortOrder === "") {
            /*** By default sort on student names*/
            sOrder = name
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        /**
         * register to watch a content URI for changes  */
        c.setNotificationUri(context!!.contentResolver, uri)
        return c

    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(INVENTORY_TABLE_NAME, "", values)
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }
        throw SQLException("Failed to add a record into $uri")

    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        var count: Int = 0
        when (uriMatcher!!.match(uri)) {
             INVENTORY -> count = db!!.delete(
                INVENTORY_TABLE_NAME, selection,
                selectionArgs
            )
            ITEM_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    INVENTORY_TABLE_NAME,
                    _id + " = " + id +
                            if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count

    }

    override fun update(uri: Uri,
                        values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<out String>?): Int {
        var count : Int = 0
        when (uriMatcher!!.match(uri)) {
            INVENTORY -> count = db!!.update(
                INVENTORY_TABLE_NAME, values, selection,
                selectionArgs
            )
            ITEM_ID -> count = db!!.update(
                INVENTORY_TABLE_NAME,
                values,
                _id + " = " + uri.pathSegments[1] +
                        (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else ""),
                selectionArgs
            )
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count

    }

}