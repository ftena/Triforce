package comm.tarlic.triforce;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.support.v4.content.ContextCompat;

public class TriforceBatteryInfoList extends ListActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

	private final ArrayList<HashMap<String,String>> list = new ArrayList<>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.list_view);        

		this.registerReceiver(this.BatteryReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }

	//@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.triforce_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
        
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
    
	private final BroadcastReceiver BatteryReceiver
	= new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			list.clear();

	    	HashMap<String,String> temp = new HashMap<>();
			temp.put("field","Battery Level");
	        temp.put("value", String.valueOf(intent.getIntExtra("level", 0)) + "%");
	        list.add(temp);
			
	        temp = new HashMap<>();
			temp.put("field","Battery Voltage");
	        temp.put("value", String.valueOf(intent.getIntExtra("voltage", 0)/1000) + " V");
	        list.add(temp);
	        
	        temp = new HashMap<>();
			temp.put("field","Battery Temperature");
	        temp.put("value", String.valueOf(intent.getIntExtra("temperature", 0)/10) + " ºC");
	        list.add(temp);
	        
	        temp = new HashMap<>();
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

			temp = new HashMap<>();
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
			
			temp = new HashMap<>();
			temp.put("field","Health");
	        temp.put("value", strHealth);
	        list.add(temp);
	        
	        showList();
		}
	};
}

