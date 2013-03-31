package comm.tarlic.triforce;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class TriforceBatteryInfoList extends ListActivity {

	final ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	
	public final static String SAVED_TIMESTAMP = "comm.tarlic.triforce.TIMESTAMP";
	
	// Timestamp(int year, int month, int date, int hour, int minute, int second, int nano)
	final Timestamp defaultTimestamp = new Timestamp (113, 0, 1, 0, 0, 0, 0);
	
	/*
	 * The timestamp configured by the user, or set to default timestamp if
	 * the db is created the first time.
	 */
	public Timestamp configuredTimestamp;
	
	// The code used to manage the configuration request
	static final int CONFIG_REQUEST = 1;

	// Unique id
	static final int id = 999;
	
	private TimestampDataSource mDataSource;
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.list_view);        

        // Access to database
        mDataSource = new TimestampDataSource(getBaseContext());
        
        // Gets the data repository in write mode
        mDataSource.open();
       
        // Upgrade?
        // mDataSource.upgrade();
        
        List<TimestampData> timestamps = mDataSource.getAllTimestamps();
       
        if (timestamps.isEmpty()) {
        	// The db is empty, so the timestamp is set to defaultTimestamp
        	android.util.Log.i(getLocalClassName(), "Db with zero element");
            android.util.Log.i(getLocalClassName(), "defaultTimestamp = " + defaultTimestamp.getTime() + 
            		" toString = " + defaultTimestamp.toString());
        	
        	mDataSource.insert(id, defaultTimestamp.getTime());
        	this.configuredTimestamp = this.defaultTimestamp;
        } else if (timestamps.size() == 1) {
        	// Get the configured timestamp
        	android.util.Log.i(getLocalClassName(), "Db with one element");
        	
        	this.configuredTimestamp = new Timestamp(timestamps.get(0).getTs());
        	
        	android.util.Log.i(getLocalClassName(), "configuredTimestamp = " + configuredTimestamp.getTime() + 
            		" toString = " + configuredTimestamp.toString());
        } else {
        	android.util.Log.e(getLocalClassName(), "There is more than one registry in db");
        }
      
        list.clear();
        
		this.registerReceiver(this.BatteryReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    
		// Get the call duration since configuredTimestamp
		getCallDuration(this.configuredTimestamp, false);
		
    }
    
    protected void onDestroy()
    {
    	super.onDestroy();
    	
    	// Unregister the previously registered BroadcastReceiver
    	this.unregisterReceiver(this.BatteryReceiver);
    	
    	android.util.Log.i(getLocalClassName(), "onDestroy()");
    }
    
    @Override
    protected void onResume() {
    	mDataSource.open();
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	mDataSource.close();
    	super.onPause();
    }
    
    // The next line avoid the dead code warning
    @SuppressWarnings("unused")
	private void getCallDuration(Timestamp timestamp, Boolean update) {
    	// Content Provider CallLog.Calls. Contains the recent calls.
    	/*
    	 * A content provider manages access to a central repository of data.
    	 * A provider is part of an Android application, which often provides its own
    	 * UI for working with the data. However, content providers are primarily
    	 * intended to be used by other applications, which access the provider
    	 * using a provider client object. Together, providers and provider clients
    	 * offer a consistent, standard interface to data that also handles
    	 * inter-process communication and secure data access.
    	 */
    	
    	String[] projection = {    	         
    	        android.provider.CallLog.Calls.TYPE,
    	        android.provider.CallLog.Calls.DATE,
    	        android.provider.CallLog.Calls.DURATION    	        
    	        };
    	
    	/*
    	 * The three next lines are to filter the selection.
    	 * We only get the lines greater than the timestampGivenByUser.
    	 */
    	
    	String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";    	
    	
    	String selection = android.provider.CallLog.Calls.DATE + ">=?"; 
    	
    	String[] selectionArgs = new String[] { String.valueOf(timestamp.getTime()) };
    	    	
    	/*
    	 *  public final Cursor query
    	 *  	(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    	 */
    	
    	Cursor mCursor = getContentResolver().query(
    	        android.provider.CallLog.Calls.CONTENT_URI,
    	        projection,
    	        selection,
    	        selectionArgs,
    	        sortOrder
    	        );    	
    	
    	/*Cursor mCursor = getContentResolver().query(
        android.provider.CallLog.Calls.CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
        );*/
    	
    	// Retrieve the column-indexes of type, date and duration
    	 
        // int typeColumn = mCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);

        int dateColumn = mCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);

        int durationColumn = mCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);
    	
        // It holds the total calls duration.
        
        int totalCallDuration = 0;
        
    	// Some providers return null if an error occurs, others throw an exception.
    	if (null == mCursor) {
    	    /*
    	     * Insert code here to handle the error. Be sure not to use the cursor! You may want to
    	     * call android.util.Log.e() to log this error.
    	     *
    	     */
    		
    		android.util.Log.e(getLocalClassName(), "Null cursor for CallLog.Calls");
    		
    	// If the Cursor is empty, the provider found no matches
    	} else if (mCursor.getCount() < 1) {

    	    /*
    	     * Insert code here to notify the user that the search was unsuccessful. This isn't necessarily
    	     * an error. You may want to offer the user the option to insert a new row, or re-type the
    	     * search term.
    	     */

    		android.util.Log.e("TriforceBatteryInfoList", "The search was unsuccessful " +
    				"for CallLog.Calls");
    		
    	} else {
    	    // Insert code here to do something with the results
    		
    		while (mCursor.moveToNext()) {

    	        // Gets the value from the column.
    			// The duration of the call in seconds.
    	        int duration = mCursor.getInt(durationColumn);
    	        // The date the call occurred, in milliseconds since the epoch.
    	        long date = mCursor.getLong(dateColumn);
    	        
    	        Timestamp ts = new Timestamp(date);
    	        
    	        android.util.Log.i(getLocalClassName(), "found timestamp = " + ts.getTime() + 
    	        		" toString = " + ts.toString());

    	        /* Using Calendar to get the date 
    	        final Calendar cal = Calendar.getInstance();
    	        cal.setTimeInMillis(date);
    	            	        
    	        android.util.Log.i(getLocalClassName(), "date = " + String.valueOf(date));
    	        */
    	        
    	        /* The actual Timestamp object is later than the configured
    	         * by user.
    	         * This "if" clause is not necessary if the selection was made using
    	         * the query above.  	         
    	        */
    	        // if (timestamp.after(timestampGivenByUser))    	        
    	        	
    	        totalCallDuration += duration;
    	        
    	        // end of while loop
    	    }

    	}
    	
    	android.util.Log.i(getLocalClassName(), "totalCallDuration = " + String.valueOf(totalCallDuration));
    	
    	// Format the string
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	
    	sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
    	
    	HashMap<String,String> temp = new HashMap<String,String>();
		temp.put("field","Total call duration");
        temp.put("value", sdf.format(totalCallDuration*1000));
        
    	if(update == false) {    
    		// Appends the specified element to the list 
			list.add(temp);
    	} else {
    		list.remove(0);
    		
    	    list.add(0, temp);
    	}
    	showList();
	}

	//@Override    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.triforce_menu, menu);
        return true;
    }
        
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_config:            
        	Intent intent = new Intent(this, DisplayConfigActivity.class);
        	//Intent intent = new Intent(this, Picker.class);
        	
        	intent.putExtra(SAVED_TIMESTAMP, configuredTimestamp.getTime());
        	
        	//startActivity(intent);
        	
        	startActivityForResult(intent, CONFIG_REQUEST);
        	
            return true;
        case R.id.menu_info:
            showInfo();
            return true;
        case R.id.menu_exit:
            showExit();
            return true; 
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void showList() {    	
    	
    	ListAdapter adapter = new SimpleAdapter(
                 this, // Context                 
                 list,                                            
                 R.layout.row_view,  // Specify the row template to use
                 new String[] { "field", "value" },           
                 new int[] {R.id.field, R.id.value});  // Parallel array of which template objects to bind to those columns.
         
    	ListView listView = (ListView) findViewById(android.R.id.list);
    	 
    	listView.setAdapter(adapter);        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        // Check which request we're responding to
        if (requestCode == CONFIG_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
            	
            	Timestamp ts = new Timestamp(data.getLongExtra(TriforceBatteryInfoList.SAVED_TIMESTAMP, 0));

            	// Set the new timestamp configured by the user.
            	updateTimestamp(ts);
            	
            	// Gets the data repository in write mode
                mDataSource.open();
            	
                // Update the db
            	mDataSource.update(id, ts.getTime());
            	
            	// Update the call duration using the value configured by the user
            	getCallDuration(ts, true);
       
            }
        }
    }
    
    private void updateTimestamp(Timestamp ts) {
		this.configuredTimestamp = ts;
	}

	private void showInfo() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(R.string.dialog_info)
               .setCancelable(true)
               .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dialog.cancel();
                   }
               });
    	
    	AlertDialog alert = builder.create();
    	
    	alert.show();
   }
    
    private void showExit() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(R.string.dialog_exit)
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   TriforceBatteryInfoList.this.finish();                	   
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });      
            
    	
    	AlertDialog alert = builder.create();
    	
    	alert.show();
   }
    
    private BroadcastReceiver BatteryReceiver
	= new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			HashMap<String,String> temp = new HashMap<String,String>();
			temp.put("field","Battery Level");
	        temp.put("value", String.valueOf(intent.getIntExtra("level", 0)) + "%");
	        list.add(temp);
			
	        temp = new HashMap<String,String>();
			temp.put("field","Battery Voltage");
	        temp.put("value", String.valueOf(intent.getIntExtra("voltage", 0)/1000) + " V");
	        list.add(temp);
	        
	        temp = new HashMap<String,String>();
			temp.put("field","Battery Temperature");
	        temp.put("value", String.valueOf(intent.getIntExtra("temperature", 0)/10) + " ºC");
	        list.add(temp);
	        
	        temp = new HashMap<String,String>();
			temp.put("field","Technology");
	        temp.put("value", intent.getStringExtra("technology"));
	        list.add(temp);


			int statusBatteryManager = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);         	
			String strStatus;

			switch (statusBatteryManager) {
			case BatteryManager.BATTERY_STATUS_CHARGING:
				strStatus = "Charging";
				break;
			case BatteryManager.BATTERY_STATUS_DISCHARGING:
				strStatus = "Dis-charging";
				break;
			case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				strStatus = "Not charging";
				break;
			case BatteryManager.BATTERY_STATUS_FULL:
				strStatus = "Full";
				break;
			default:
				strStatus = "Unknown";
			}

			temp = new HashMap<String,String>();
			temp.put("field","Status");
	        temp.put("value", strStatus);
	        list.add(temp);

			int health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
			String strHealth;

			if (health == BatteryManager.BATTERY_HEALTH_GOOD){
				strHealth = "Good";
			} else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT){
				strHealth = "Over Heat";
			} else if (health == BatteryManager.BATTERY_HEALTH_DEAD){
				strHealth = "Dead";
			} else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){
				strHealth = "Over Voltage";
			} else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE){
				strHealth = "Unspecified Failure";
			} else{
				strHealth = "Unknown";
			}
			
			temp = new HashMap<String,String>();
			temp.put("field","Health");
	        temp.put("value", strHealth);
	        list.add(temp);
		}
	};
};

