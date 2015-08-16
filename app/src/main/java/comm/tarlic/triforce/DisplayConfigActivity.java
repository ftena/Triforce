package comm.tarlic.triforce;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import comm.tarlic.triforce.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class DisplayConfigActivity extends FragmentActivity 
	implements DatePickerFragment.DatePickerListener {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the View layer		
		setContentView(R.layout.config_view);
		
		Intent intent = getIntent();
		long timestamp = intent.getLongExtra(TriforceBatteryInfoList.SAVED_TIMESTAMP, 0);
	
		updateDate(timestamp);				
	}

	/*
	 * The update param is used to set if the method is called to
	 * create the list for the first time or to update it. 
	 */
	
	private void updateDate(long time) {
		Calendar cal = Calendar.getInstance();
		
		cal.setTimeInMillis(time);
		
		// Get and display the language		
		android.util.Log.i(getLocalClassName(), "Locale = " + Locale.getDefault().getLanguage());

		DateFormat df;

		// UK or US format
		//if (Locale.getDefault().getLanguage().equals("en"))

		df = SimpleDateFormat.getDateInstance(DateFormat.LONG, Locale.UK);

		// Spanish format
		/*else
			df = SimpleDateFormat.getDateInstance(DateFormat.LONG, new Locale("es", "ES"));*/

		TextView text = (TextView) findViewById(R.id.DateText);
		
		text.setText("Count call duration from:\n" + df.format(cal.getTime()).toString());
	}	
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getSupportFragmentManager(), "datePicker");
	}
	
	// The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDatePickerClick(DialogFragment dialog, int year, int month, int day) {
        // User touched the dialog's positive button

    	// Timestamp(int year, int month, int date, int hour, int minute, int second, int nano)
		final Timestamp timestamp = new Timestamp (year-1900, month, day, 0, 0, 0, 0);
		
		updateDate(timestamp.getTime());
		
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TriforceBatteryInfoList.SAVED_TIMESTAMP, timestamp.getTime());

        setResult(RESULT_OK, resultIntent);
        
        // Back to the main activity
        finish();
    }
	
}
