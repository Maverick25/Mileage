package net.workbook.mileage;

import net.workbook.mileage.interfaces.Finals;
import net.workbook.mileage.model.DriveLocation;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CommentsActivity extends Activity 
implements OnClickListener,Finals
{
	// Inputs
	private EditText addComments;
	private Button done;
	private Intent currentData;
	
	/**
	 * Start of the Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_comments);
		
		MainActivity.allactivities.add(this);
		
		addComments = (EditText) findViewById(R.id.addComments);
		done = (Button) findViewById(R.id.done_comments);
		
		done.setOnClickListener(this);
		
		if (getIntent().getStringExtra(ADDED_COMMENTS)!=null)
		{
			addComments.setText(getIntent().getStringExtra(ADDED_COMMENTS));
			addComments.setSelection(addComments.getText().length());
		}
	}
	
	/**
	 * onActivityResult
	 */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	try
    	{
    		if (Activity.RESULT_OK == resultCode)
    		{
    			DriveLocation driveLocation = (DriveLocation) data.getParcelableExtra(SELECTED_ADDRESS);
    			
    			currentData = new Intent();
    			currentData.putExtra(ADDED_COMMENTS, driveLocation.getAddress());
    			setResult(Activity.RESULT_OK, currentData);
    			finish();
    		}
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		super.onActivityResult(requestCode, resultCode, data);
    	}
	}
    
    @Override
	protected void onDestroy() 
    {
    	MainActivity.allactivities.remove(this);
		super.onDestroy();
	}


//************************************************************************************************************	
//											UI Listeners
//************************************************************************************************************	

	/**
	 * DoneButton Listener:
	 */
	@Override
	public void onClick(View arg0) 
	{
		try
		{
			String comments = addComments.getText().toString();
			
			if (getIntent().getBooleanExtra(CATEGORY, false))
			{
				Intent selectSuggestions = new Intent(CommentsActivity.this, SelectionActivity.class);
				selectSuggestions.putExtra(ADDED_COMMENTS, comments);
				selectSuggestions.putExtra(CATEGORY, MANUAL);
				startActivityForResult(selectSuggestions, MANUAL);
			}
			else
			{
				Intent data = new Intent();
				data.putExtra(ADDED_COMMENTS, comments);
				setResult(Activity.RESULT_OK, data);
				finish();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
