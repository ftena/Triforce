package comm.tarlic.triforce;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EnclosingDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EnclosingDb.db";

    private static final String INTEGER_TYPE = " INTEGER";    
    // private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    
    /*
	 * By implementing the BaseColumns interface, your inner
	 * class can inherit a primary key field called _ID that some
	 * Android classes such as cursor adaptors will expect it to have.
	 * It's not required, but this can help your database work harmoniously
	 * with the Android framework.
	*/
    
    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + EnclosingDb.Entry.TABLE_NAME + " (" +
        EnclosingDb.Entry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
        EnclosingDb.Entry.COLUMN_ID + INTEGER_TYPE + COMMA_SEP +      
        EnclosingDb.Entry.COLUMN_TIMESTAMP + INTEGER_TYPE +
        " )";

    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + EnclosingDb.Entry.TABLE_NAME;
    
    public EnclosingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    
    //API Level 11
    /*
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    */
}