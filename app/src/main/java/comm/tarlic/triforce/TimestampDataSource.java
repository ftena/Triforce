package comm.tarlic.triforce;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

class TimestampDataSource {

	// Database fields
	  private SQLiteDatabase db;
	  private final EnclosingDbHelper mDbHelper;
	  
	  public TimestampDataSource(Context context) {
		  mDbHelper = new EnclosingDbHelper(context);
	  }

	  public void open() throws SQLException {
		  db = mDbHelper.getWritableDatabase();
	  }

	  public void upgrade() {
		  mDbHelper.onUpgrade(db, 1, 2);
	  }
	  
	  public void close() {
		  mDbHelper.close();
	  }
	  
	  public void insert(long ts) {
		// Create a new map of values, where column names are the keys
		  ContentValues values = new ContentValues();
		  values.put(EnclosingDb.Entry.COLUMN_ID, TriforceBatteryInfoList.id);
		  values.put(EnclosingDb.Entry.COLUMN_TIMESTAMP, ts);
		  
		  // Insert the new row, returning the primary key value of the new row
		  long newRowId;
		  newRowId = db.insert(
		           EnclosingDb.Entry.TABLE_NAME,
		           null,
		           values);
	  }
	  
	  public void update(long ts) {
		  // New value for one column
		  ContentValues values = new ContentValues();
		  values.put(EnclosingDb.Entry.COLUMN_TIMESTAMP, ts);


		  // Which row to update, based on the ID
		  String selection = EnclosingDb.Entry.COLUMN_ID + " LIKE ?";
		  String[] selectionArgs = { String.valueOf(TriforceBatteryInfoList.id) };

		  db.update(
				  EnclosingDb.Entry.TABLE_NAME,
				  values,
				  selection,
				  selectionArgs);
	  }
	  
	  public List<TimestampData> getAllTimestamps() {
		    List<TimestampData> timestamps = new ArrayList<>();

		 // Define a projection that specifies which columns from the database
		 // you will actually use after this query.
		 String[] projection = {
				 EnclosingDb.Entry._ID,
				 EnclosingDb.Entry.COLUMN_ID,
				 EnclosingDb.Entry.COLUMN_TIMESTAMP
		     };
		    
		// How we want the results sorted in the resulting Cursor
		 String sortOrder =
				 EnclosingDb.Entry.COLUMN_ID + " DESC";
		 
		 Cursor c = db.query(
				 	EnclosingDb.Entry.TABLE_NAME,  // The table to query
				    projection,                               // The columns to return
				    null,                                // The columns for the WHERE clause
				    null,                            // The values for the WHERE clause
				    null,                                     // don't group the rows
				    null,                                     // don't filter by row groups
				    sortOrder                                 // The sort order
				    );

		    c.moveToFirst();
		    while (!c.isAfterLast()) {
		    	TimestampData ts = new TimestampData();

		    	// Column 1 is the id
		    	// Column 2 is the timestamp
		    	ts.setId(c.getInt(1));
		    	ts.setTs(c.getLong(2));
		    	
		    	timestamps.add(ts);
		    	c.moveToNext();
		    }
		    // Make sure to close the cursor
		    c.close();
		    return timestamps;
		  }
}
