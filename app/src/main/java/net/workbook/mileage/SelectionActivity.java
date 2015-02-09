package net.workbook.mileage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.workbook.mileage.interfaces.Finals;
import net.workbook.mileage.interfaces.Sectionizer;
import net.workbook.mileage.model.Car;
import net.workbook.mileage.model.CarRegNo;
import net.workbook.mileage.model.Creditor;
import net.workbook.mileage.model.DriveLocation;
import net.workbook.mileage.model.MileageType;
import net.workbook.mileage.model.Job;
import net.workbook.mileage.support.JSONParser;
import net.workbook.mileage.support.SectionAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectionActivity extends Activity 
implements OnItemClickListener,TextWatcher,Finals
{
	// Inputs
	private EditText inputSearch;
	
	// ListViews
	private ListView selectionList;
	private ArrayList<Object> finalObjects;
	private ArrayList<String> finalPlaces;
	private AdapterView<?> list;
	private ArrayAdapter<Object> adapter;
	private SectionAdapter<Job> sectionAdapter;
	private ArrayAdapter<String> stringAdapter;
	
	//DB
	private GetObjects getObjects;
	private GetPlaces getPlaces;
	
	// Animations
	private View selectionView;
	private View loadingStatus;
	
	// Activity-Transfer
	private DriveLocation driveLocation;
	private Intent currentData;
	private static int ACTIVE;
	
	/**
	 * Start of the Activity
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_selection);
       
        MainActivity.allactivities.add(this);
        
        selectionList = (ListView) findViewById(R.id.selectionList);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        
        selectionView = findViewById(R.id.selectionView);
        loadingStatus = findViewById(R.id.loading_status);
        
        try
        {
        	switch(getIntent().getIntExtra(CATEGORY,0))
        	{
        	case PICK:
        		getObjects = new GetObjects();
        		getObjects.execute(PICK);
        		break;
        	case PICK_ADDRESS:
        		driveLocation = getIntent().getExtras().getParcelable(DRIVE_LOCATION);
        		
        		getPlaces = new GetPlaces();
        		getPlaces.execute(driveLocation.getAddress());
        		break;
        	case MANUAL:
        		driveLocation = new DriveLocation(null, null);
        		String address = getIntent().getStringExtra(ADDED_COMMENTS);
        		
        		getPlaces = new GetPlaces();
        		getPlaces.execute(address);
        		break;
        	case JOB:
        		getObjects = new GetObjects();
        		getObjects.execute(JOB);
        		break;
        	case MILEAGE_TYPE:
        		getObjects = new GetObjects();
        		getObjects.execute(MILEAGE_TYPE);
        		break;
        	case CAR:
        		getObjects = new GetObjects();
        		getObjects.execute(CAR);
        		break;
        	case CAR_REG_NO:
        		getObjects = new GetObjects();
        		getObjects.execute(CAR_REG_NO);
        		break;
        	case CREDITOR:
        		getObjects = new GetObjects();
        		getObjects.execute(CREDITOR);
        		break;
        	}
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
        inputSearch.addTextChangedListener(this);
        selectionList.setOnItemClickListener(this);
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
    			DriveLocation driveLocation = (DriveLocation) data.getParcelableExtra(MainActivity.SELECTED_ADDRESS);
    		
    			currentData = new Intent();
    			currentData.putExtra(MainActivity.SELECTED_ADDRESS, driveLocation);
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

	/*
     * Additional Methods
     */
	private void showProgress(final boolean show) 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) 
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			loadingStatus.setVisibility(View.VISIBLE);
			loadingStatus.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							loadingStatus.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
			});

			selectionView.setVisibility(View.VISIBLE);
			selectionView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							selectionView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
			});
		} 
		else 
		{
			loadingStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			selectionView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
//******************************************************************************************************************	
//										UI Listeners
//******************************************************************************************************************
	
	/**
	 * SearchField Listeners:
	 */
	@Override
	public void afterTextChanged(Editable search) 
	{
		if (adapter == null)
		{
			if (search.toString().equals(NOTHING))
			{
				stringAdapter = new ArrayAdapter<String>(SelectionActivity.this, R.layout.row_simple, finalPlaces);
				selectionList.setAdapter(stringAdapter);
			}
			else
			{
				ArrayList<String> places = new ArrayList<String>();
				String toLower = search.toString().toLowerCase();
				for (String place : finalPlaces)
				{
					String lowerPlace = place.toLowerCase();
					if (lowerPlace.contains(toLower))
					{
						places.add(place);
					}
				}
				stringAdapter = new ArrayAdapter<String>(SelectionActivity.this, R.layout.row_simple, places);
				selectionList.setAdapter(stringAdapter);
			}

		}
		else
		{
			if (sectionAdapter == null)
			{
				if (search.toString().equals(NOTHING))
				{
					adapter = new ArrayAdapter<Object>(SelectionActivity.this, R.layout.row_simple,finalObjects);
					selectionList.setAdapter(adapter);
				}
				else
				{
					ArrayList<Object> objects = new ArrayList<Object>();
					String toLower = search.toString().toLowerCase();
					for (Object object : finalObjects)
					{
						String lowerObject = object.toString().toLowerCase();
						if (lowerObject.contains(toLower))
						{
							objects.add(object);
						}
					}
					adapter = new ArrayAdapter<Object>(SelectionActivity.this,R.layout.row_simple,objects);
					selectionList.setAdapter(adapter);
				}
			}
			else
			{
				ArrayList<Job> finalJobs = new ArrayList<Job>();
				for (int i=0; i<finalObjects.size(); i++)
				{
					finalJobs.add((Job)finalObjects.get(i));
				}
				selectionList.setAdapter(sectionAdapter.filterSections(finalJobs,search.toString()));
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	/**
	 * ListView Listener:
	 */
	@Override
	public void onItemClick(AdapterView<?> list, View line, int position, long id) 
	{
		try
		{
			SelectionActivity.this.list = list;
			currentData = new Intent();
			switch(getIntent().getIntExtra(MainActivity.CATEGORY, 0))
				{
				case PICK:
					new SetListener().execute(PICK,position);
					break;
				case PICK_ADDRESS:
					new SetListener().execute(PICK_ADDRESS,position);
					break;
				case MANUAL:
					new SetListener().execute(MANUAL,position);
					break;
				case JOB:
					new SetListener().execute(JOB,position);
					break;
				case MILEAGE_TYPE:
					new SetListener().execute(MILEAGE_TYPE,position);
					break;
				case CAR:
					new SetListener().execute(CAR,position);
					break;
				case CAR_REG_NO:
					new SetListener().execute(CAR_REG_NO,position);
					break;
				case CREDITOR:
					new SetListener().execute(CREDITOR,position);
					break;
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
    
//******************************************************************************************************************	
//								ASYNC TASK IMPLEMENTATIONS
//******************************************************************************************************************	
	
	/**
	 * Load of the objects from the Database according to the Category
	 */
	private class GetObjects extends AsyncTask<Integer, String, ArrayList<Object>>
	{	
		@Override
		protected void onPreExecute() 
		{
			adapter = null;
			sectionAdapter = null;
			stringAdapter = null;
			finalObjects = null;
			showProgress(true);
			Handler handler = new Handler();
    		handler.postDelayed(new Runnable()
    		{
    		  @Override
    		  public void run() 
    		  {
    		      if (getObjects.getStatus() == AsyncTask.Status.RUNNING)
    		      {
    		          AlertDialog.Builder builder = new AlertDialog.Builder(SelectionActivity.this);
    					builder.setMessage(TIMEOUT_LOADING_ERROR)
    					       .setCancelable(false)
    					       .setNeutralButton(CONTINUE, new DialogInterface.OnClickListener() 
    					       {
    					           public void onClick(DialogInterface dialog, int id) 
    					           {
    					                dialog.dismiss();
    					           }
    					       })
    					       .setPositiveButton(CLOSE, new DialogInterface.OnClickListener() 
    					       {
    					    	   public void onClick(DialogInterface dialog, int id) 
    					    	   {
    					    		   dialog.dismiss();
    					    		   getObjects.cancel(true);
    					    	   }
    					       });

    					AlertDialog alert = builder.create();
    					alert.show();
    					TextView messageView = (TextView)alert.findViewById(android.R.id.message);
    					messageView.setGravity(Gravity.CENTER);
    		      }
    		  }
    		}, 30000 );
		}

		@Override
		protected ArrayList<Object> doInBackground(Integer... categories)
		{
			ArrayList<Object> objects = new ArrayList<Object>();
			JSONParser jParser = new JSONParser();
			try
			{
				JSONArray dbObjects = jParser.getJSONArrayFromUrl(MainActivity.getServer(),request(categories[0]));
				JSONObject dbObject;
				switch(categories[0])
				{
				case PICK:
					publishProgress("Destination");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						String description = dbObject.getString("Description");
						try
						{
							String address = dbObject.getString("Address");
							objects.add(new DriveLocation(description,address));
						}
						catch (JSONException e)
						{
							String address = null;
							objects.add(new DriveLocation(description,address));
						}
					}
					break;
				case JOB:
					publishProgress("Job");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						int jobId = dbObject.getInt("JobID");
						String jobName = dbObject.getString("JobName");
						int statusId = dbObject.getInt("StatusId");
						String customerName = dbObject.getString("CustomerName");
						objects.add(new Job(jobId, jobName, statusId, customerName));
					}
					break;
				case MILEAGE_TYPE:
					publishProgress("Mileage Type");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						
						int mileageRateId = dbObject.getInt("MileageRateId");
						int compId = dbObject.getInt("CompId");
						String comment = dbObject.getString("Comment");
						boolean active = dbObject.getBoolean("Active");
						
						objects.add(new MileageType(mileageRateId, compId, comment, active));
					}
					break;
				case CAR:
					publishProgress("Car");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						
						int id = dbObject.getInt("Id");
						System.out.println(id);
						String name = dbObject.getString("Name");
						
						objects.add(new Car(id,name));
					}
					break;
				case CAR_REG_NO:
					publishProgress("Car Reg. No.");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						
						int recordId = dbObject.getInt("RecordId");
						int empId = dbObject.getInt("EmpId");
						
						String dateString = dbObject.getString("FromDate");
						dateString = dateString.substring(0, 19);
						dateString = dateString.replace("/Date(", "");
						long time = Long.parseLong(dateString);
						Date fromDate = new Date(time);
						
						String carName = dbObject.getString("CarName");
						String carRegNo = dbObject.getString("CarRegNo");
						boolean isActive = dbObject.getBoolean("IsActive");
						
						objects.add(new CarRegNo(recordId, empId, fromDate, carName, carRegNo, isActive));
					}
					break;
				case CREDITOR:
					publishProgress("Creditor");
					for (int i=0; i<dbObjects.length(); i++)
					{
						dbObject = dbObjects.getJSONObject(i);
						
						int empArpAccId = dbObject.getInt("EmpArpAccountId");
						int empId = dbObject.getInt("EmpId");
						int id = dbObject.getInt("ArpAccId");
						boolean useMileage = dbObject.getBoolean("UseMileage");
						String no = dbObject.getString("ArpAccNo");
						String name = dbObject.getString("ArpAccName");
						
						objects.add(new Creditor(empArpAccId, empId, id, useMileage, no, name));
					}
					break;
				}
		
				return objects;
			}
			catch(JSONException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onProgressUpdate(String... values) 
		{
			setTitle("Select "+values[0]);
    		inputSearch.setHint("Search "+values[0]+"..");
		}

		@Override
		protected void onPostExecute(ArrayList<Object> result) 
		{
			try
			{
				finalObjects = result;
				adapter = new ArrayAdapter<Object>(SelectionActivity.this,R.layout.row_simple,result);
				if (result.get(0) instanceof Job)
				{
					Sectionizer<Job> customerSectionizer = new Sectionizer<Job>() 
					{		
						@Override
						public String getSectionTitleForItem(Job instance) 
						{
							return instance.getCustomerName();
						}
					};
				
					sectionAdapter = new SectionAdapter<Job>(SelectionActivity.this, adapter, R.layout.section, R.id.list_item_section_text, customerSectionizer);
					selectionList.setAdapter(sectionAdapter);
				}
				else
				{
					selectionList.setAdapter(adapter);
				}
				showProgress(false);
			}
			catch(Exception e)
			{
				showProgress(false);
				e.printStackTrace();
			}
		}
		
		@Override
		protected void onCancelled() 
		{
			finish();
		}

		private String request(int category)
		{
	    	switch(category)
	    	{
	    	case PICK:
	    		return "/api/mileage/drivelocations/";
	    	case JOB:
	    		return "/api/jobs/mileage/";
	    	case MILEAGE_TYPE:
	    		return "/api/mileage/types/";
	    	case CAR:
	    		return "/api/mileage/cars/";
	    	case CAR_REG_NO:
	    		return "/api/mileage/carregno/";
	    	case CREDITOR:
	    		return "/api/mileage/creditors/";
	    	default:
	    		return null;
	    	}
		}
	}
	
	/**
	 * Set Listeners for All the rows in the ListView
	 */
	private class SetListener extends AsyncTask<Integer, String, Void>
	{
		@Override
		protected Void doInBackground(Integer... params) 
		{
			ACTIVE = 404;
			switch(params[0])
			{
			case PICK:
				ACTIVE = PICK;
				driveLocation = (DriveLocation) list.getItemAtPosition(params[1]);
			
				Intent finalPick = new Intent(SelectionActivity.this,SelectionActivity.class);
				finalPick.putExtra(CATEGORY,PICK_ADDRESS);
				finalPick.putExtra(DRIVE_LOCATION, driveLocation);
				startActivityForResult(finalPick, PICK);
				break;
			case PICK_ADDRESS:
				String address = (String) list.getItemAtPosition(params[1]);
				driveLocation.setAddress(address);
				
				currentData.putExtra(MainActivity.SELECTED_ADDRESS, driveLocation);
				break;
			case MANUAL:
				String manualAddress = (String) list.getItemAtPosition(params[1]);
				driveLocation.setAddress(manualAddress);
				
				currentData.putExtra(SELECTED_ADDRESS, driveLocation);
				break;
			case JOB:
				Job job = (Job) list.getItemAtPosition(params[1]);
				
				currentData.putExtra(MainActivity.SELECTED_ITEM, job);
				break;
			case MILEAGE_TYPE:
				MileageType mileageType = (MileageType) list.getItemAtPosition(params[1]);
				
				currentData.putExtra(MainActivity.SELECTED_ITEM, mileageType);
				break;
			case CAR:
				Car car = (Car) list.getItemAtPosition(params[1]);
				
				currentData.putExtra(MainActivity.SELECTED_ITEM, car);
				break;
			case CAR_REG_NO:
				CarRegNo carRegNo = (CarRegNo) list.getItemAtPosition(params[1]);
				
				currentData.putExtra(MainActivity.SELECTED_ITEM, carRegNo);
				break;
			case CREDITOR:
				Creditor creditor = (Creditor) list.getItemAtPosition(params[1]);
				
				currentData.putExtra(MainActivity.SELECTED_ITEM, creditor);
				break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) 
		{
			if (ACTIVE != PICK)
			{
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
				setResult(Activity.RESULT_OK,currentData);
				finish();
			}
		}	
		
	}
	
	/**
	 * Get Place Suggestions via JSON file request to Google API
	 */
	private class GetPlaces extends AsyncTask<String, String, ArrayList<String>>
	{
		@Override
		protected void onPreExecute() 
		{
			showProgress(true);
			setTitle(driveLocation.getDescription());
    		inputSearch.setHint("Search Address..");
    		stringAdapter = null;
    		adapter = null;
    		sectionAdapter = null;
    		
    		Handler handler = new Handler();
    		handler.postDelayed(new Runnable()
    		{
    		  @Override
    		  public void run() 
    		  {
    		      if (getPlaces.getStatus() == AsyncTask.Status.RUNNING)
    		      {
    		          AlertDialog.Builder builder = new AlertDialog.Builder(SelectionActivity.this);
    					builder.setMessage(TIMEOUT_LOADING_ERROR)
    					       .setCancelable(false)
    					       .setNeutralButton(CONTINUE, new DialogInterface.OnClickListener() 
    					       {
    					           public void onClick(DialogInterface dialog, int id) 
    					           {
    					                dialog.dismiss();
    					           }
    					       })
    					       .setPositiveButton(CLOSE, new DialogInterface.OnClickListener() 
    					       {
    					    	   public void onClick(DialogInterface dialog, int id) 
    					    	   {
    					    		   dialog.dismiss();
    					    		   getPlaces.cancel(true);
    					    	   }
    					       });

    					AlertDialog alert = builder.create();
    					alert.show();
    					TextView messageView = (TextView)alert.findViewById(android.R.id.message);
    					messageView.setGravity(Gravity.CENTER);
    		      }
    		  }
    		}, 30000 );
		}

		@Override
		protected ArrayList<String> doInBackground(String... params) 
		{
			JSONParser jParser = new JSONParser();
			ArrayList<String> places = new ArrayList<String>();
			try
			{	
				JSONObject json = jParser.getJSONFromUrl(request(params[0]));
				JSONArray predictions = json.getJSONArray("predictions");
				for (int i=0; i<predictions.length();i++)
				{
					JSONObject prediction = (JSONObject) predictions.get(i);
					String place = NOTHING;
					JSONArray terms = prediction.getJSONArray("terms");
					for (int j=0; j<terms.length();j++)
					{
						JSONObject term = (JSONObject) terms.get(j);
						String value = term.getString("value");
						if (j==0)
						{
							place += value+",  ";
						}
						else
						{
							if (j==terms.length()-1)
							{
								place += value;
							}
							else
							{
								place += value+", ";
							}
						}
					}
					places.add(place);
				}
				return places;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(ArrayList<String> result) 
		{   
			try
			{
				if (result.size()>0)
				{	
					finalPlaces = result;
					stringAdapter = new ArrayAdapter<String>(SelectionActivity.this,R.layout.row_simple,result);
					selectionList.setAdapter(stringAdapter);
					showProgress(false);
				}
				else
				{
					throw new NullPointerException();
				}
			}
			catch (NullPointerException e)
			{
				showProgress(false);
				AlertDialog.Builder builder = new AlertDialog.Builder(SelectionActivity.this);
				builder.setMessage(ZERO_RESULTS)
				       .setCancelable(false)
				       .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id)
				           {
				                dialog.dismiss();
				                finish();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				TextView messageView = (TextView)alert.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				showProgress(false);
			}
		}
		
		@Override
		protected void onCancelled() 
		{
			finish();
		}

		private String request(String place)
		{
			try 
			{	
				return "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+URLEncoder.encode(convertPlace(place), "UTF-8")+"&types=geocode&key=AIzaSyDuBKXgCf_o8xMq3qT_WIjtX7DsvxVWNmQ&sensor=false";
			} 
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		
		private String convertPlace(String place)
		{
			String[] convertedRequest = place.split("\n");
			for (int i=0; i<convertedRequest.length; i++)
			{
				convertedRequest[i] = convertedRequest[i].trim();
			}
			
			int index = -1;
			do
			{
				index++;
				System.out.println(convertedRequest[0]);
				if (index!=0)
				{
					convertedRequest[0] += " "+convertedRequest[index];
				}
				if (index==convertedRequest.length-1)
				{
					break;
				}
			} while (convertedRequest[index].matches(".*[a-zA-Z].*" ) == false);
			
			return convertedRequest[0];
		}
		
	}
	
}
