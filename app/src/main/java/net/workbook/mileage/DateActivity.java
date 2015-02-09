package net.workbook.mileage;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;

public class DateActivity extends Activity implements OnClickListener
{
	// Inputs
	private Button done; 
	private DatePicker datePicker;
	
	/**
	 * Start of the Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date);
		
		MainActivity.allactivities.add(this);
		
		done = (Button) findViewById(R.id.done_date);
		datePicker = (DatePicker) findViewById(R.id.datePicker);
		
		done.setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() 
	{
		MainActivity.allactivities.remove(this);
		super.onDestroy();
	}

//***********************************************************************************************************************	
//											UI Listeners	
//***********************************************************************************************************************	

	/**
	 * DoneButton Listener:
	 */
	@Override
	public void onClick(View v) 
	{
		try
		{
			int day = datePicker.getDayOfMonth();
			int month = datePicker.getMonth()+1;
			int year = datePicker.getYear();
			
			String date = day+"/"+month+"/"+year;
			
			Intent data = new Intent();
			data.putExtra(MainActivity.CHANGED_DATE, date);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
