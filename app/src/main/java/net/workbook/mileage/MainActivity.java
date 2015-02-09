package net.workbook.mileage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import net.workbook.mileage.interfaces.Finals;
import net.workbook.mileage.model.Car;
import net.workbook.mileage.model.CarRegNo;
import net.workbook.mileage.model.Creditor;
import net.workbook.mileage.model.DriveLocation;
import net.workbook.mileage.model.Job;
import net.workbook.mileage.model.MileageType;
import net.workbook.mileage.support.DoubleAdapter;
import net.workbook.mileage.support.JSONParser;
import net.workbook.mileage.support.ListDoubleRow;
import net.workbook.mileage.support.ListRow;
import net.workbook.mileage.support.SingleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends Activity 
implements OnClickListener,OnItemClickListener,OnItemSelectedListener,OnSeekBarChangeListener,OnMarkerClickListener,Finals
{
	// Connection-related Fields
	private ConnectionChangeReceiver conReceiver;
	private static String server;
	private static int id;
	public static Vector<Activity> allactivities;

	// Map-related and Tracking Fields
	private FragmentManager fManager;
	private GoogleMap hiddenMap;
	private CameraPosition cameraPosition;
	private TrackingTask task;
	private InitializeUI initialize;
	private LocationManager manager;
	private LocationClient client;
	private boolean isTracking;
	private boolean isMapHidden;
	private boolean startPending;
	private Location pendingStartLocation;
	private boolean stopPending;
	private Location pendingStopLocation;
	private MapFragment map;
	
	// Settings
	private float startY;
	private Spinner distanceUnit;
	private SeekBar intervalBar;
	private TextView intervalValue;
	private Button logoutButton;
	private static int interval;
	private static boolean kmDefault;
	private boolean toSettings;
	private SharedPreferences preferences;
	
	// Inputs
	private ListView listLocation;
	private Button startTracking;
	private Button stopTracking;
	private Button pauseTracking;
	private Button plus;
	private Button minus;
	private TextView resultInfo;
	private ListView listDetails;
	private MenuItem menuMap;
	private MenuItem menuRefresh;
	private MenuItem menuLogout;
	private View plusMinus;
	
	
	// Animations
	private View trackingStatus;
	private View appView;
	private TextView statusMessage;
	
	// ListViews
	private ArrayList<ListRow> rows;
	private ArrayList<ListDoubleRow> doubleRows;
	private SingleAdapter singleAdapter;
	private DoubleAdapter doubleAdapter;
	
	// Fields for Trip
	private String date;
	private Date tripDate;
	private Job job;
	private MileageType mileageType;
	private Car car;
	private CarRegNo carRegNo;
	private Creditor creditor;
	private String comments;
	private DriveLocation startLocation;
	private DriveLocation stopLocation;
	private int googleDistance;
	private int googleDuration;
	private double distance;
	private long startMillis;
	private ArrayList<Long> pauseMillis;
	private ArrayList<Long> resumeMillis;
	private long stopMillis;
	
	
	/**
	 * Start of the Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		allactivities = new Vector<Activity>();
		
		preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		kmDefault = preferences.getBoolean("kmDefault", true);
		interval = preferences.getInt("interval", 30);
		
		server = getIntent().getStringExtra(SERVER);
		id = getIntent().getIntExtra(ID, 0);
		
		initialize = new InitializeUI();
		initialize.execute();
	}
	
	/**
	 * Creation of the Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		menuMap = menu.findItem(R.id.showMap);
		menuRefresh = menu.findItem(R.id.refresh);
		menuLogout = menu.findItem(R.id.logout);
		registerReceivers();
		return super.onCreateOptionsMenu(menu);
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
				switch(requestCode)
				{
				case DATE:
					date = data.getStringExtra(CHANGED_DATE);
					updateDate(date);
					break;
				case JOB:
					job = (Job) data.getParcelableExtra(SELECTED_ITEM);
					updateDetailsView(JOB, job);
					break;
				case MILEAGE_TYPE:
					mileageType = (MileageType) data.getParcelableExtra(SELECTED_ITEM);
					updateDetailsView(MILEAGE_TYPE, mileageType);
					break;
				case CAR:
					car = (Car) data.getParcelableExtra(SELECTED_ITEM);
					updateDetailsView(CAR, car);
					break;
				case CAR_REG_NO:
					carRegNo = (CarRegNo) data.getParcelableExtra(SELECTED_ITEM);
					updateDetailsView(CAR_REG_NO, carRegNo);
					break;
				case CREDITOR:
					creditor = (Creditor) data.getParcelableExtra(SELECTED_ITEM);
					updateDetailsView(CREDITOR, creditor);
					break;
				case COMMENTS:
					comments = data.getStringExtra(ADDED_COMMENTS);
					if (!comments.trim().equals(NOTHING))
					{
						updateComments(comments);
					}
					else
					{
						updateComments(COMMENTS_DEFAULT);
					}
					break;
				case START_LOCATION:
					clearTimes();
					hiddenMap.clear();
					startLocation = (DriveLocation) data.getParcelableExtra(SELECTED_ADDRESS);
					if (stopLocation!=null)
					{
						new DistanceTask().execute(startLocation.getAddress(), stopLocation.getAddress(), START_LOCATION+NOTHING);
					}
					else
					{
						updateLocationView(START_LOCATION-7, startLocation);
					}
					cameraPosition = (CameraPosition) data.getParcelableExtra(SAVED_MAP);	
					break;
				case STOP_LOCATION:
					clearTimes();
					hiddenMap.clear();
					stopLocation = (DriveLocation) data.getParcelableExtra(SELECTED_ADDRESS);
					new DistanceTask().execute(startLocation.getAddress(), stopLocation.getAddress(), STOP_LOCATION+NOTHING);
					cameraPosition = (CameraPosition) data.getParcelableExtra(SAVED_MAP);
					break;
				}
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
		catch(Exception e)
		{
			super.onActivityResult(requestCode, resultCode, data);
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() 
	{
		try
		{
			if (menuMap.getTitle().equals(HIDE_MAP))
			{
				hideMap();
				menuMap.setTitle(SHOW_MAP);
			}
			super.onPause();
		}
		catch (Exception e)
		{
			super.onPause();
		}
	}

	@Override
	protected void onResume() 
	{
		try
		{
			if (!isMapHidden)
			{
				hideMap();
			}
			super.onResume();
		}
		catch (Exception e)
		{
			super.onResume();
		}
	}

	@Override
	protected void onDestroy() 
	{
		try
		{
			Editor edit = preferences.edit();
			edit.putBoolean("kmDefault",kmDefault);
			edit.putInt("interval", interval);
			edit.commit();
			unregisterReceiver(conReceiver);
			super.onDestroy();
		}
		catch (Exception e)
		{
			unregisterReceiver(conReceiver);
			super.onDestroy();
		}
	}
	
	/*
	 * Additional Methods
	 */
	private void showProgress(final boolean show) 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) 
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			trackingStatus.setVisibility(View.VISIBLE);
			trackingStatus.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							trackingStatus.setVisibility(show ? View.VISIBLE : View.GONE);
						}
			});

			appView.setVisibility(View.VISIBLE);
			appView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							appView.setVisibility(show ? View.GONE : View.VISIBLE);
							try
							{
								menuMap.setVisible(show ? false : true);
								menuRefresh.setVisible(show ? false : true);
								menuLogout.setVisible(show ? false : true);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
			});
		} 
		else 
		{
			trackingStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			appView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	private void registerReceivers()
	{
		conReceiver = new ConnectionChangeReceiver(new Handler());
		registerReceiver(conReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	private boolean isNetworkAvailable()
	{
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
		return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
	}
	
	private DoubleAdapter populateLocationList()
	{
		doubleRows = new ArrayList<ListDoubleRow>();
		
		doubleRows.add(START_LOCATION-7, new ListDoubleRow(START_LOCATION_LABEL,NOTHING,NOTHING,NOTHING));
		doubleRows.add(STOP_LOCATION-7, new ListDoubleRow(STOP_LOCATION_LABEL,NOTHING,NOTHING,NOTHING));
		
		doubleAdapter = new DoubleAdapter(this, doubleRows);
		
		return doubleAdapter;
	}
	
	private SingleAdapter populateDetailsList()
	{
		rows = new ArrayList<ListRow>();
		
			rows.add(DATE, new ListRow(DATE_LABEL, initializeDate()));
			rows.add(JOB, new ListRow(JOB_LABEL, JOB_DEFAULT));
			
			try
			{
				rows.add(MILEAGE_TYPE, new ListRow(MILEAGE_TYPE_LABEL, mileageType.toString()));
			}
			catch (NullPointerException e)
			{
				rows.add(MILEAGE_TYPE, new ListRow(MILEAGE_TYPE_LABEL, MILEAGE_TYPE_DEFAULT));
			}
			
			try
			{
				rows.add(CAR, new ListRow(CAR_LABEL, car.toString()));
			}
			catch (NullPointerException e)
			{
				rows.add(CAR, new ListRow(CAR_LABEL, CAR_DEFAULT));
			}
			
			try
			{
				rows.add(CAR_REG_NO, new ListRow(CAR_REG_NO_LABEL, carRegNo.toString()));
			}
			catch (NullPointerException e)
			{
				rows.add(CAR_REG_NO, new ListRow(CAR_REG_NO_LABEL, CAR_REG_NO_DEFAULT));	
			}
			
			rows.add(CREDITOR, new ListRow(CREDITOR_LABEL, CREDITOR_DEFAULT));
			rows.add(COMMENTS, new ListRow(COMMENTS_LABEL, COMMENTS_DEFAULT));
		
		singleAdapter = new SingleAdapter(this, rows);
		
		return singleAdapter;
	}
	
	private String initializeDate()
	{
		Calendar calendar = Calendar.getInstance();
		
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH)+1;
		int year = calendar.get(Calendar.YEAR);
		
		tripDate = new Date();
		
		
		date = day+"/"+month+"/"+year;
		return date;
	}
	
	private String initializeTime(int index)
	{
		Calendar calendar = Calendar.getInstance();
		
		switch(index)
		{
		case START_LOCATION-7:
			startMillis = calendar.getTimeInMillis();
			break;
		case STOP_LOCATION-7:
			stopMillis = calendar.getTimeInMillis();
			break;
		}
		
		int hours = calendar.get(Calendar.HOUR);
		if (calendar.get(Calendar.AM_PM) == 1)
		{
			if (hours != 12)
			{
				hours += 12;
			}
		}
		else
		{
			if (hours == 12)
			{
				hours = 0;
			}
		}
		int mins = calendar.get(Calendar.MINUTE);
		String minutes = mins+"";
		if (mins<10)
		{
			minutes = "0"+mins;
		}
		
		return hours+":"+minutes;
	}
	
	private void updateDetailsView(int index,Object o)
	{
		if (o.toString().length()>25)
		{
			rows.get(index).setItem(o.toString().substring(0, 24)+THREE_DOTS);
		}
		else
		{
			rows.get(index).setItem(o.toString());
		}
		singleAdapter.notifyDataSetChanged();
	}
	
	private void updateDate(String date)
	{
		rows.get(DATE).setItem(date);
		
		singleAdapter.notifyDataSetChanged();
	}
	
	private void updateComments(String comments)
	{
		if (comments.length()>26)
		{
			rows.get(COMMENTS).setItem(comments.substring(0, 25)+THREE_DOTS);
		}
		else
		{
			rows.get(COMMENTS).setItem(comments);
		}
		singleAdapter.notifyDataSetChanged();
	}
	
	private void updateLocationView(int index,DriveLocation driveLocation)
	{
		try
		{
			String address = driveLocation.getAddress();

			String[] addresses = address.split(",  ");
			doubleRows.get(index).setFirstRow(addresses[0]+",");
			if (addresses[1].length()>32)
			{
				String[] lastLineTerms = addresses[1].split(", ");
				String lastLine = NOTHING;
				for (int i=0; i<lastLineTerms.length; i++)
				{
					if (i!=lastLineTerms.length-1)
					{
						if (i==lastLineTerms.length-2)
						{
							lastLine += lastLineTerms[i];
						}
						else
						{
							lastLine += lastLineTerms[i]+", ";
						}
					}
				}
				doubleRows.get(index).setSecondRow(lastLine);
			}
			else
			{
				doubleRows.get(index).setSecondRow(addresses[1]);
			}
			doubleAdapter.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void updateTimeLocationView(int index)
	{
		switch(index)
		{
		case START_LOCATION-7:
			doubleRows.get(index).setTime(initializeTime(index));
			break;
		case STOP_LOCATION-7:
			doubleRows.get(index).setTime(initializeTime(index));
			break;
		}
		doubleAdapter.notifyDataSetChanged();
	}
	
	private String convertDistance(String distance)
	{
		if (distance.contains(","))
		{
			distance = distance.replace(",", NOTHING);
		}
		if (kmDefault)
		{
			if (distance.contains(" km"))
			{
				distance = distance.replace(" km", NOTHING);
				double inKm = Double.parseDouble(distance);
				if (inKm>=1000)
				{
					int first = (int) inKm/1000;
					int second = (int) inKm - (first*1000);
					String secondPart = NOTHING+second;
					if (second<100)
					{
						secondPart = "0"+second;
						if (second<10)
						{
							secondPart = "00"+second;
						}
					}
					if (kmDefault)
					{
						return first+","+secondPart+" km";
					}
					else
					{
						return first+","+secondPart+" miles";
					}
				}
				return Double.valueOf(new DecimalFormat("#.#").format(inKm))+" km";
			}
			distance = distance.replace(" miles", NOTHING);
//			double inMiles = Double.parseDouble(distance);
			double inMiles = Double.valueOf(new DecimalFormat("#.#").format(1.609344*Double.parseDouble(distance)));
			if (inMiles>=1000)
			{
				int first = (int) inMiles/1000;
				int second = (int) inMiles - (first*1000);
				String secondPart = NOTHING+second;
				if (second<100)
				{
					secondPart = "0"+second;
					if (second<10)
					{
						secondPart = "00"+second;
					}
				}
				if (kmDefault)
				{
					return first+","+secondPart+" km";
				}
				else
				{
					return first+","+secondPart+" miles";
				}
			}
			return inMiles+" km";
		}
		else
		{
			if (distance.contains(" miles"))
			{
				distance = distance.replace(" miles", NOTHING);
				double inMiles = Double.parseDouble(distance);
				if (inMiles>=1000)
				{
					int first = (int) inMiles/1000;
					int second = (int) inMiles - (first*1000);
					String secondPart = NOTHING+second;
					if (second<100)
					{
						secondPart = "0"+second;
						if (second<10)
						{
							secondPart = "00"+second;
						}
					}
					if (kmDefault)
					{
						return first+","+secondPart+" km";
					}
					else
					{
						return first+","+secondPart+" miles";
					}
				}
				return Double.valueOf(new DecimalFormat("#.#").format(inMiles))+" miles";
			}
			distance = distance.replace(" km", NOTHING);
			double inKm = Double.valueOf(new DecimalFormat("#.#").format(0.62137119*Double.parseDouble(distance)));
			if (inKm>=1000)
			{
				int first = (int) inKm/1000;
				int second = (int) inKm - (first*1000);
				String secondPart = NOTHING+second;
				if (second<100)
				{
					secondPart = "0"+second;
					if (second<10)
					{
						secondPart = "00"+second;
					}
				}
				if (kmDefault)
				{
					return first+","+secondPart+" km";
				}
				else
				{
					return first+","+secondPart+" miles";
				}
			}
			return inKm+" miles";
		}
	}
	
	private void clearTimes()
	{
		doubleRows.get(START_LOCATION-7).setTime(NOTHING);
		doubleRows.get(STOP_LOCATION-7).setTime(NOTHING);
		doubleAdapter.notifyDataSetChanged();
	}
	
	public static String getServer()
	{
		return server;
	}
	
	public static int getId()
	{
		return id;
	}

	/*
	 * 	Animations:
	 */
	private void showMap()
	{
		try
		{
			FragmentTransaction transaction = fManager.beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_left);
			transaction.show(map);
			transaction.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void hideMap()
	{
		try
		{
			FragmentTransaction transaction = fManager.beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_out_right, R.anim.slide_out_right);
			transaction.hide(map);
			transaction.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * Location Updates:
	 */
	private boolean startService()
	{
		try
		{
			manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(NO_GPS)
				       .setCancelable(false)
				       .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
				    	   @Override
				    	   public void onClick(DialogInterface dialog, int which) {
				    		   	dialog.dismiss();
				    	   }
				       })
				       .setPositiveButton(GPS_SWITCH, new DialogInterface.OnClickListener() {
				           @Override
				    	   public void onClick(DialogInterface dialog, int id) {
				        	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				        	   startActivity(intent);			                
				               dialog.dismiss();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				TextView messageView = (TextView)alert.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
				return false;
			}
			isTracking = true;
			task = new TrackingTask();
			task.execute();
			
			pauseMillis = new ArrayList<Long>();
			resumeMillis = new ArrayList<Long>();
			logoutButton.setEnabled(false);
			intervalBar.setEnabled(false);
			menuRefresh.setEnabled(false);
			menuLogout.setEnabled(false);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	private boolean pauseService()
	{
		Calendar calendar = Calendar.getInstance();
		pauseMillis.add(Long.valueOf(calendar.getTimeInMillis()));
		isTracking = false;
		return true;
	}
	
	private boolean resumeService()
	{
		Calendar calendar = Calendar.getInstance();
		resumeMillis.add(Long.valueOf(calendar.getTimeInMillis()));
		isTracking = true;
		return true;
	}
	
	private boolean stopService()
	{
		try
		{
			if (!isTracking)
			{
				Calendar calendar = Calendar.getInstance();
				resumeMillis.add(Long.valueOf(calendar.getTimeInMillis()));
			}
			if(task != null)
			{
				statusMessage.setVisibility(View.VISIBLE);
				task.cancel(true);
				task = null;
			}
			isTracking = false;
			logoutButton.setEnabled(true);
			intervalBar.setEnabled(true);
			menuRefresh.setEnabled(true);
			menuLogout.setEnabled(true);
			return true;
		}
		catch(Exception e)
		{
			isTracking = false;
			task = null;
			return false;
		}
	}
	
	private Address getAddress(Location location)
	{
		try
		{
			Geocoder geocoder = new Geocoder(MainActivity.this);
			
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
		
			Address address = addresses.get(0);
			return address;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	private String getAddressName(Address address)
	{
		String addressName = address.getAddressLine(0)+",  "+address.getAddressLine(1);
		return addressName;
	}
	
//**************************************************************************************************************************
//	 										UI Listeners
//**************************************************************************************************************************	 
	
	/**
	 * ListView Listeners:
	 */
	@Override
	public void onItemClick(AdapterView<?> list, View line, int position, long id) 
	{
		try
		{
			switch(list.getId())
			{
			case R.id.listLocation:
				switch(position)
				{
				case START_LOCATION-7:
					if (isTracking || task!=null)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage(QUIT_TRACKING)
						       .setCancelable(false)
						       .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
						    	   @Override
						    	   public void onClick(DialogInterface dialog, int which) {
						    		   	dialog.dismiss();
						    	   }
						       })
						       .setPositiveButton(YES, new DialogInterface.OnClickListener() {
						           @Override
						    	   public void onClick(DialogInterface dialog, int id) {	                
						        	   if (pauseTracking.getText().equals(RESUME_TRACKING_LABEL))
										{
											//Resume
											pauseTracking.setText(PAUSE_TRACKING_LABEL);
											pauseTracking.setBackgroundResource(R.drawable.btn_yellow_holo_dark);
										}
										
										if (stopService())
										{
											stopTracking.setVisibility(View.GONE);
											pauseTracking.setVisibility(View.GONE);
											startTracking.setVisibility(View.VISIBLE);
											startTracking.setText(SAVE_TRACKING_LABEL);
											startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload, 0, 0, 0);
											startTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
											updateTimeLocationView(STOP_LOCATION-7);
										}
						        	   dialog.dismiss();
						               Intent pickStart = new Intent(MainActivity.this,MapActivity.class);
									   pickStart.putExtra(PREVIOUSLY, cameraPosition);
									   pickStart.putExtra(CATEGORY, START_LOCATION);
									   startActivityForResult(pickStart, START_LOCATION);
						           }
						       });
						AlertDialog alert = builder.create();
						alert.show();
						TextView messageView = (TextView)alert.findViewById(android.R.id.message);
						messageView.setGravity(Gravity.CENTER);
					}
					else
					{
						Intent pickStart = new Intent(MainActivity.this,MapActivity.class);
						pickStart.putExtra(PREVIOUSLY, cameraPosition);
						pickStart.putExtra(CATEGORY, START_LOCATION);
						startActivityForResult(pickStart, START_LOCATION);
					}
					break;
				case STOP_LOCATION-7:
					if (isTracking || task!=null)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage(QUIT_TRACKING)
						       .setCancelable(false)
						       .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
						    	   @Override
						    	   public void onClick(DialogInterface dialog, int which) {
						    		   	dialog.dismiss();
						    	   }
						       })
						       .setPositiveButton(YES, new DialogInterface.OnClickListener() {
						           @Override
						    	   public void onClick(DialogInterface dialog, int id) {
						               if (pauseTracking.getText().equals(RESUME_TRACKING_LABEL))
									   {
											//Resume
											pauseTracking.setText(PAUSE_TRACKING_LABEL);
											pauseTracking.setBackgroundResource(R.drawable.btn_yellow_holo_dark);
									   }
										
										if (stopService())
										{
											stopTracking.setVisibility(View.GONE);
											pauseTracking.setVisibility(View.GONE);
											startTracking.setVisibility(View.VISIBLE);
											startTracking.setText(SAVE_TRACKING_LABEL);
											startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload, 0, 0, 0);
											startTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
											updateTimeLocationView(STOP_LOCATION-7);
									   }
									   dialog.dismiss();
						               Intent pickEnd = new Intent(MainActivity.this,MapActivity.class);
						               pickEnd.putExtra(CATEGORY, STOP_LOCATION);
						               pickEnd.putExtra(PREVIOUSLY, cameraPosition);
						               startActivityForResult(pickEnd, STOP_LOCATION);
						           }
						       });
						AlertDialog alert = builder.create();
						alert.show();
						TextView messageView = (TextView)alert.findViewById(android.R.id.message);
						messageView.setGravity(Gravity.CENTER);
					}
					else
					{
						Intent pickEnd = new Intent(MainActivity.this,MapActivity.class);
						pickEnd.putExtra(CATEGORY, STOP_LOCATION);
						pickEnd.putExtra(PREVIOUSLY, cameraPosition);
						startActivityForResult(pickEnd, STOP_LOCATION);
					}
					break;
				}
				break;
			case R.id.listDetails:
				switch(position)
				{
				case DATE:
					Intent pickDate = new Intent(MainActivity.this,DateActivity.class);
					startActivityForResult(pickDate, DATE);
					break;
				case JOB:
					Intent pickJob = new Intent(MainActivity.this,SelectionActivity.class);
					pickJob.putExtra(CATEGORY, JOB);
					startActivityForResult(pickJob, JOB);
					break;
				case MILEAGE_TYPE:
					Intent pickMileageType = new Intent(MainActivity.this, SelectionActivity.class);
					pickMileageType.putExtra(CATEGORY, MILEAGE_TYPE);
					startActivityForResult(pickMileageType, MILEAGE_TYPE);
					break;
				case CAR:
					Intent pickCar = new Intent(MainActivity.this,SelectionActivity.class);
					pickCar.putExtra(CATEGORY, CAR);
					startActivityForResult(pickCar, CAR);
					break;
				case CAR_REG_NO:
					Intent pickCarRegNo = new Intent(MainActivity.this,SelectionActivity.class);
					pickCarRegNo.putExtra(CATEGORY, CAR_REG_NO);
					startActivityForResult(pickCarRegNo, CAR_REG_NO);
					break;
				case CREDITOR:
					Intent pickCreditor = new Intent(MainActivity.this, SelectionActivity.class);
					pickCreditor.putExtra(CATEGORY, CREDITOR);
					startActivityForResult(pickCreditor, CREDITOR);
					break;
				case COMMENTS:
					Intent addComments = new Intent(MainActivity.this,CommentsActivity.class);
					if (comments!=null)
					{
						addComments.putExtra(ADDED_COMMENTS, comments);
					}
					startActivityForResult(addComments, COMMENTS);
					break;
				}
				break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Button Listeners:
	 */
	@Override
	public void onClick(View v) 
	{
		try
		{
			switch(v.getId())
			{
			case R.id.startTracking:
				if (startTracking.getText().equals(START_TRACKING_LABEL))
				{
					//Start Tracking
					if (startService())
					{
						startTracking.setVisibility(View.GONE);
						pauseTracking.setVisibility(View.VISIBLE);
						stopTracking.setVisibility(View.VISIBLE);
						
						updateTimeLocationView(START_LOCATION-7);
					}
				}
				else
				{
					//Save Track
					startTracking.setText(START_TRACKING_LABEL);
					startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.traffic_lights, 0, 0, 0);
					startTracking.setBackgroundResource(R.drawable.btn_blue_holo_dark);
					new SaveTrip().execute();
					if (kmDefault)
					{
						resultInfo.setText(NOT_DEFINED_DISTANCE_KM);
					}
					else
					{
						resultInfo.setText(NOT_DEFINED_DISTANCE_MILES);
					}
				}
				break;
			case R.id.pauseTracking:
				if (pauseTracking.getText().equals(PAUSE_TRACKING_LABEL))
				{
					//Pause
					if (pauseService())
					{
						pauseTracking.setText(RESUME_TRACKING_LABEL);
						pauseTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
					}
				}
				else
				{
					//Resume
					if (resumeService())
					{
						pauseTracking.setText(PAUSE_TRACKING_LABEL);
						pauseTracking.setBackgroundResource(R.drawable.btn_yellow_holo_dark);
					}
				}
				break;
			case R.id.stopTracking:
				//Stop Tracking
				if (pauseTracking.getText().equals(RESUME_TRACKING_LABEL))
				{
					//Resume
					pauseTracking.setText(PAUSE_TRACKING_LABEL);
					pauseTracking.setBackgroundResource(R.drawable.btn_yellow_holo_dark);
				}
				
				if (stopService())
				{
					showProgress(true);
					stopTracking.setVisibility(View.GONE);
					pauseTracking.setVisibility(View.GONE);
					startTracking.setVisibility(View.VISIBLE);
					startTracking.setText(SAVE_TRACKING_LABEL);
					startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload, 0, 0, 0);
					startTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
					updateTimeLocationView(STOP_LOCATION-7);
				}
				break;
			case R.id.plus:
				String plusString = (String) resultInfo.getText();
				String[] plusLines = plusString.split("\n");
				if (plusLines[0].contains(","))
				{
					plusLines[0] = plusLines[0].replace(",",NOTHING);
				}
				
				if (kmDefault)
				{
					plusLines[0] = plusLines[0].replace(" km", NOTHING);
				}
				else
				{
					plusLines[0] = plusLines[0].replace(" miles", NOTHING);
				}
				
				if (plusLines[0].contains("."))
				{
					double plusResult = Double.parseDouble(plusLines[0]);
					
					if (plusLines[0].contains(".0"))
					{
						plusResult += 1;
					}
					else
					{
						double rounded = (double) Math.round(plusResult);
						rounded = plusResult - rounded;
						if (rounded>0)
						{
							rounded -= 1;
						}
						rounded *= -1;
					
						plusResult += rounded;
					}
					
					if (kmDefault)
					{
						distance = plusResult;
					}
					else
					{
						distance = Double.valueOf(new DecimalFormat("#.#").format(1.609344*plusResult));
					}
					
					if (plusResult>=1000)
					{
						int first = (int) plusResult/1000;
						int second = (int) plusResult - (first*1000);
						String secondPart = NOTHING+second;
						if (second<100)
						{
							secondPart = "0"+second;
							if (second<10)
							{
								secondPart = "00"+second;
							}
						}
						if (kmDefault)
						{
							resultInfo.setText(first+","+secondPart+" km\n"+plusLines[1]);
						}
						else
						{
							resultInfo.setText(first+","+secondPart+" miles\n"+plusLines[1]);
						}
					}
					else
					{
						if (kmDefault)
						{
							resultInfo.setText(plusResult+" km\n"+plusLines[1]);
						}
						else
						{
							resultInfo.setText(plusResult+" miles\n"+plusLines[1]);
						}
					}
				}
				else
				{
					int plusResult = Integer.parseInt(plusLines[0]);
					plusResult += 1;
					
					if (kmDefault)
					{
						distance = plusResult;
					}
					else
					{
						distance = Double.valueOf(new DecimalFormat("#.#").format(1.609344*plusResult));
					}
					
					if (plusResult>=1000)
					{
						int first = (int) plusResult/1000;
						int second = (int) plusResult - (first*1000);
						String secondPart = NOTHING+second;
						if (second<100)
						{
							secondPart = "0"+second;
							if (second<10)
							{
								secondPart = "00"+second;
							}
						}
						if (kmDefault)
						{
							resultInfo.setText(first+","+secondPart+" km\n"+plusLines[1]);
						}
						else
						{
							resultInfo.setText(first+","+secondPart+" miles\n"+plusLines[1]);
						}
					}
					else
					{
						if (kmDefault)
						{
							resultInfo.setText(plusResult+" km\n"+plusLines[1]);
						}
						else
						{
							resultInfo.setText(plusResult+" miles\n"+plusLines[1]);
						}
					}
				}
				googleDistance = (int) distance;
				break;
			case R.id.minus:
				String minusString = (String) resultInfo.getText();
				String[] minusLines = minusString.split("\n");
				if (minusLines[0].contains(","))
				{
					minusLines[0] = minusLines[0].replace(",", NOTHING);
				}
				
				if (kmDefault)
				{
					minusLines[0] = minusLines[0].replace(" km", NOTHING);
				}
				else
				{
					minusLines[0] = minusLines[0].replace(" miles", NOTHING);
				}
				
				
				if (Double.parseDouble(minusLines[0]) > 0)
				{
					if (minusLines[0].contains("."))
					{
						double minusResult = Double.parseDouble(minusLines[0]);
						
						if (minusLines[0].contains(".0"))
						{
							minusResult -= 1;
						}
						else
						{
							double rounded = (double) Math.round(minusResult);
							rounded = minusResult - rounded;
							if (rounded<0)
							{
								rounded += 1;
							}
						
							minusResult -= rounded;
						}
						
						if (kmDefault)
						{
							distance = minusResult;
						}
						else
						{
							distance = Double.valueOf(new DecimalFormat("#.#").format(1.609344*minusResult));
						}
						
						if (minusResult>=1000)
						{
							int first = (int) minusResult/1000;
							int second = (int) minusResult - (first*1000);
							String secondPart = NOTHING+second;
							if (second<100)
							{
								secondPart = "0"+second;
								if (second<10)
								{
									secondPart = "00"+second;
								}
							}
							if (kmDefault)
							{
								resultInfo.setText(first+","+secondPart+" km\n"+minusLines[1]);
							}
							else
							{
								resultInfo.setText(first+","+secondPart+" miles\n"+minusLines[1]);
							}
						}
						else
						{
							if (kmDefault)
							{
								resultInfo.setText(minusResult+" km\n"+minusLines[1]);
							}
							else
							{
								resultInfo.setText(minusResult+" miles\n"+minusLines[1]);
							}
						}
					}
					else
					{
						int minusResult = Integer.parseInt(minusLines[0]);
						minusResult -= 1;
						
						if (kmDefault)
						{
							distance = minusResult;
						}
						else
						{
							distance = Double.valueOf(new DecimalFormat("#.#").format(1.609344*minusResult));
						}
						
						if (minusResult>=1000)
						{
							int first = (int) minusResult/1000;
							int second = (int) minusResult - (first*1000);
							String secondPart = NOTHING+second;
							if (second<100)
							{
								secondPart = "0"+second;
								if (second<10)
								{
									secondPart = "00"+second;
								}
							}
							if (kmDefault)
							{
								resultInfo.setText(first+","+secondPart+" km\n"+minusLines[1]);
							}
							else
							{
								resultInfo.setText(first+","+secondPart+" miles\n"+minusLines[1]);
							}
						}
						else
						{
							if (kmDefault)
							{
								resultInfo.setText(minusResult+" km\n"+minusLines[1]);
							}
							else
							{
								resultInfo.setText(minusResult+" miles\n"+minusLines[1]);
							}
						}
					}
				}
				googleDistance = (int) distance;
				
				break;
			case R.id.logoutButton:
				Editor edit = preferences.edit();
				edit.putBoolean("saveLogin", false);
				edit.putBoolean("logout", true);
				edit.commit();
				Intent logout = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(logout);
				finish();
				break;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * ActionBar Listeners:
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		try
		{
			switch (item.getItemId())
			{
			case R.id.showMap:
				if (item.getTitle().equals(SHOW_MAP))
				{
					showMap();
					item.setTitle(HIDE_MAP);
				}
				else
				{
					hideMap();
					item.setTitle(SHOW_MAP);
				}
				return true;
			case R.id.refresh:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(RESET_QUESTION)
				       .setCancelable(false)
				       .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
				    	   @Override
				    	   public void onClick(DialogInterface dialog, int which) {
				    		   	dialog.dismiss();
				    	   }
				       })
				       .setPositiveButton(OK, new DialogInterface.OnClickListener() {
				           @Override
				    	   public void onClick(DialogInterface dialog, int id) {
				                dialog.dismiss();
				                initialize =new InitializeUI();
				                initialize.execute();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				TextView messageView = (TextView)alert.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
				return true;
			case R.id.settings:
				final View settings = findViewById(R.id.settings);
				final View layoutT = findViewById(R.id.layoutT);
				final View mapsLayout = findViewById(R.id.mapsLayout);
				if (settings.getVisibility()!=View.VISIBLE)
    			{
    				Animation bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_up);
    				bottomUp.setAnimationListener(new AnimationListener() 
    				{	
    					@Override
    					public void onAnimationStart(Animation animation) 
    					{
    						layoutT.setVisibility(View.GONE);
    						mapsLayout.setVisibility(View.GONE);
    						menuMap.setVisible(false);
    						menuRefresh.setVisible(false);
    						settings.setVisibility(View.VISIBLE);
    					}
    					
    					@Override
    					public void onAnimationRepeat(Animation animation) {}
    					
    					@Override
    					public void onAnimationEnd(Animation animation) {}
    				});
    				settings.startAnimation(bottomUp);
    				listLocation.setOnItemClickListener(null);
    			}
				else 
				{
					if (settings.getVisibility()==View.VISIBLE)
					{
						Animation bottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_down);
						bottomDown.setAnimationListener(new AnimationListener() 
						{
							@Override
							public void onAnimationStart(Animation animation) 
							{
								settings.setVisibility(View.GONE);	
							}
					
							@Override
							public void onAnimationRepeat(Animation animation) {}
					
							@Override
							public void onAnimationEnd(Animation animation) 
							{
								menuMap.setVisible(true);
								menuRefresh.setVisible(true);
								layoutT.setVisibility(View.VISIBLE);
								mapsLayout.setVisibility(View.VISIBLE);
							}
						});
	            		settings.startAnimation(bottomDown);
	    				listLocation.setOnItemClickListener(MainActivity.this);
					}
				}
				return true;
			case R.id.logout:
				Editor edit = preferences.edit();
				edit.putBoolean("saveLogin", false);
				edit.putBoolean("logout", true);
				edit.commit();
				Intent logout = new Intent(MainActivity.this, LoginActivity.class);
				startActivity(logout);
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) 
	{
		final View settings = findViewById(R.id.settings);
		final View layoutT = findViewById(R.id.layoutT);
		final View mapsLayout = findViewById(R.id.mapsLayout);
		
            switch (event.getAction()) 
            {
            case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    break;
            case MotionEvent.ACTION_UP: 
                    float endY = event.getY();
                    
                    if (endY < startY) 
                    {
                    	if (settings.getVisibility()!=View.VISIBLE)
            			{
            				Animation bottomUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_up);
            				bottomUp.setAnimationListener(new AnimationListener() 
            				{	
            					@Override
            					public void onAnimationStart(Animation animation) 
            					{
            						layoutT.setVisibility(View.GONE);
            						mapsLayout.setVisibility(View.GONE);
            						menuMap.setVisible(false);
            						menuRefresh.setVisible(false);
            						settings.setVisibility(View.VISIBLE);
            					}
            					
            					@Override
            					public void onAnimationRepeat(Animation animation) {}
            					
            					@Override
            					public void onAnimationEnd(Animation animation) {}
            				});
            				settings.startAnimation(bottomUp);
            				listLocation.setOnItemClickListener(null);
            			}
                    }
                    else 
                    {
                    	if (settings.getVisibility()==View.VISIBLE)
                    	{
                    		Animation bottomDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.bottom_down);
                    		bottomDown.setAnimationListener(new AnimationListener() 
                    		{
                    			@Override
                    			public void onAnimationStart(Animation animation) 
                    			{
                    				settings.setVisibility(View.GONE);
                    			}
        					
                    			@Override
                    			public void onAnimationRepeat(Animation animation) {}
        					
                    			@Override
                    			public void onAnimationEnd(Animation animation) 
                    			{
                    				menuMap.setVisible(true);
                    				menuRefresh.setVisible(true);
                    				layoutT.setVisibility(View.VISIBLE);
                    				mapsLayout.setVisibility(View.VISIBLE);
        						}
                    		});
        				
                    		settings.startAnimation(bottomDown);
            				listLocation.setOnItemClickListener(MainActivity.this);
                    	}
                    }
            }
            return true;
    }
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		intervalValue.setText(progress+" "+getResources().getString(R.string.tracking_interval_value));
		interval = progress;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}
	
	@Override
	public void onItemSelected(AdapterView<?> list, View line, int position, long id) 
	{
		switch(position)
		{
		case 0:
			kmDefault = true;
			if (!resultInfo.getText().toString().contains("-.-") && !resultInfo.getText().toString().contains("N/A"))
			{
				String[] lines = resultInfo.getText().toString().split("\n");
				String result = convertDistance(lines[0]);
				if (lines.length>1)
				{
					for (int i=1; i<lines.length; i++)
					{
						result += "\n"+lines[i];
					}
				}
				resultInfo.setText(result);
			}
			else
			{
				String replacement = resultInfo.getText().toString().replace("miles", "km");
				resultInfo.setText(replacement);
			}
			break;
		case 1:
			kmDefault = false;
			if (!resultInfo.getText().toString().contains("-.-") && !resultInfo.getText().toString().contains("N/A"))
			{
				String[] lines = resultInfo.getText().toString().split("\n");
				System.out.println(lines[0]);
				String result = convertDistance(lines[0]);
				if (lines.length>1)
				{
					for (int i=1; i<lines.length; i++)
					{
						result += "\n"+lines[i];
					}
				}
				resultInfo.setText(result);
			}
			else
			{
				System.out.println("damn, nothing");
				String replacement = resultInfo.getText().toString().replace("km", "miles");
				resultInfo.setText(replacement);
			}
			break;
		}
		
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}

	@Override
	public boolean onMarkerClick(Marker marker) 
	{
		hiddenMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
		return true;
	}

	
	
//	*************************************************************************************************************************
//										ASYNC TASK IMPLEMENTATIONS:
//	*************************************************************************************************************************
	
	/**
	 * Function to save the Trip details into the Database
	 */
	private class SaveTrip extends AsyncTask<Void,Integer,Boolean>
	{
		private String result;
		
		@Override
		protected void onPreExecute() 
		{
			if (comments == null)
			{
				comments = NOTHING;
			}
			
			try
			{
				job.getJobId();
			}
			catch (NullPointerException e)
			{
				job = new Job(0, null, 0, null);
			}
			
			try
			{
				mileageType.getMileageRateId();
			}
			catch (NullPointerException e)
			{
				mileageType = new MileageType(0, 0, null, false);
			}
			
			try
			{
				System.out.println(car.getId());
			}
			catch (NullPointerException e)
			{
				car = new Car(0, null);
			}
			
			try
			{
				carRegNo.toString();
			}
			catch (NullPointerException e)
			{
				carRegNo = new CarRegNo(0, 0, null, null, null, false);
			}
			
			try
			{
				creditor.getArpAccId();
			}
			catch (NullPointerException e)
			{
				creditor = new Creditor(0, 0, 0, false, null, null);
			}
			
			result = (String) resultInfo.getText();
		}

		@Override
		protected Boolean doInBackground(Void... params) 
		{
			JSONParser jParser = new JSONParser();
			try
			{
				jParser.getResponse(server,request());
				return true;
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				return false;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
	
		
		
		@Override
		protected void onPostExecute(Boolean success) 
		{
			try
			{
				if (success)
				{
					TextView titleView = new TextView(MainActivity.this);
					titleView.setText(SAVE_SUCCESS_TITLE);
					titleView.setGravity(Gravity.CENTER_HORIZONTAL);
					titleView.setTextSize(20);
					titleView.setTextColor(Color.GREEN);
				
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setCustomTitle(titleView)
					   		.setMessage(SAVE_SUCCESS)
					   		.setCancelable(true);
					final AlertDialog alert = builder.create();
					alert.show();
				
					TextView messageView = (TextView)alert.findViewById(android.R.id.message);
					messageView.setGravity(Gravity.CENTER);
					messageView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload_checkmark, 0, 0, 0);
				
					final Timer t = new Timer();
				
					t.schedule(new TimerTask() {
						public void run()
						{
							alert.dismiss();
							t.cancel();
						}
					}, 3000);
					
					try
					{
						if (car.getId() == 0)
						{
							throw new NullPointerException();
						}
					}
					catch (NullPointerException e)
					{
						car = null;
					}
					
					try
					{
						if (carRegNo.getCarName().equals(null) && carRegNo.getCarRegNo().equals(null) && carRegNo.getRecordId() == 0)
						{
							throw new NullPointerException();
						}
					}
					catch (NullPointerException e)
					{
						carRegNo = null;
					}
					
					try
					{
						if (mileageType.getMileageRateId() == 0)
						{
							throw new NullPointerException();
						}
					}
					catch (NullPointerException e)
					{
						mileageType = null;
					}
					new InitializeUI().execute();
				}
				else
				{
					TextView titleView = new TextView(MainActivity.this);
					titleView.setText(NO_CONNECTION_TITLE);
					titleView.setGravity(Gravity.CENTER_HORIZONTAL);
					titleView.setTextSize(20);
					titleView.setTextColor(Color.RED);
				
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setCustomTitle(titleView)
					   		.setMessage(NO_CONNECTION)
					   		.setCancelable(true);
					final AlertDialog alert = builder.create();
					alert.show();
				
					TextView messageView = (TextView)alert.findViewById(android.R.id.message);
					messageView.setGravity(Gravity.CENTER);
					messageView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload_close, 0, 0, 0);
				
					final Timer t = new Timer();
				
					startTracking.setText("Save");
					startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload, 0, 0, 0);
					startTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
					resultInfo.setText(result);
				
					t.schedule(new TimerTask() {
						public void run()
						{
							alert.dismiss();
							t.cancel();
						}
					}, 3000);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		private String request()
		{
			try
			{
				return "/api/mileage?"+
					   "&ToAddress="+URLEncoder.encode(stopLocation.getAddress().replace("  ", " "),"UTF-8")+
					   "&FromAddress="+URLEncoder.encode (startLocation.getAddress().replace("  ", " "),"UTF-8")+
					   "&TripDate="+convertDate(tripDate)+
					   "&GoogleDistance="+googleDistance+
					   "&GoogleDuration="+googleDuration+
					   "&JobId="+job.getJobId()+
					   "&MileageRateId="+mileageType.getMileageRateId()+
					   "&CarRegNo="+URLEncoder.encode(carRegNo.toString(),"UTF-8")+
					   "&CreditorId="+creditor.getArpAccId()+
					   "&Comment="+URLEncoder.encode(comments,"UTF-8")+
					   "&Distance="+distance+
					   "&EmployeePaymentSettlementType="+car.getId()+
					   "&ApprovalStatus="+0+
					   "&DistanceCalculationStatus="+0+
					   "&DistanceUnitType="+0;
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		private String convertDate(Date date)
		{
			long millis = date.getTime();
			return "/Date("+millis+"-0000)/";
		}
		
	}
	
	/**
	 * Manual Distance Calculation via JSON file request to Google API
	 */
	private class DistanceTask extends AsyncTask<String,Integer,String>
	{
		private static final String TAG_ROUTES = "routes";
		private static final String TAG_LEGS = "legs";
		private static final String TAG_DISTANCE = "distance";
		private static final String TAG_DURATION = "duration";
		private static final String TAG_STEPS = "steps";
		private static final String TAG_POLYLINE = "polyline";
		private static final String TAG_START_LOCATION = "start_location";
		private static final String TAG_END_LOCATION = "end_location";
		private List<List<HashMap<String,String>>> polyRoutes;
		private int category;
		private boolean isDrawing;
	    private	LatLng start;
		private LatLng stop;
		
		@Override
		protected void onPreExecute() 
		{
			statusMessage.setVisibility(View.VISIBLE);
			showProgress(true);
			start = null;
			stop = null;
		}

		@Override
		protected String doInBackground(String... params) 
		{
			category = Integer.parseInt(params[2]);
			JSONParser jParser = new JSONParser();
			String dist = NOTHING;
			polyRoutes = new ArrayList<List<HashMap<String,String>>>();
			try
			{
				JSONObject json = jParser.getJSONFromUrl(request(params[0], params[1]));
				ArrayList<HashMap<String,String>> path = new ArrayList<HashMap<String,String>>();
				JSONArray routes = json.getJSONArray(TAG_ROUTES);
				JSONObject route = routes.getJSONObject(0);
				
				JSONArray legs = route.getJSONArray(TAG_LEGS);
				JSONObject leg = legs.getJSONObject(0);
				
				JSONObject distance = leg.getJSONObject(TAG_DISTANCE);
				dist = distance.getString("text");
				String dis = dist.replace(" km", NOTHING);
				dis = dis.replace(",", NOTHING);
				if (dis.contains("mi"))
				{
					dis = dis.replace(" mi", NOTHING);
					MainActivity.this.distance = Double.valueOf(new DecimalFormat("#.#").format(Double.parseDouble(dis)*1.609344));
					double milesinMiles = Double.valueOf(new DecimalFormat("#.#").format(MainActivity.this.distance));
					if (milesinMiles>=1000)
					{
						int first = (int) milesinMiles/1000;
						int second = (int) milesinMiles - (first*1000);
						String secondPart = NOTHING+second;
						if (second<100)
						{
							secondPart = "0"+second;
							if (second<10)
							{
								secondPart = "00"+second;
							}
						}
						dist = first+","+secondPart+" miles";
					}
					dist = MainActivity.this.distance+" km";
				}
				else
				{
					MainActivity.this.distance = Double.parseDouble(dis);
				}
				
				if (!kmDefault)
				{
					double inMiles = Double.valueOf(new DecimalFormat("#.#").format(0.62137119*MainActivity.this.distance));
					dist = inMiles +" miles";
					if (inMiles>=1000)
					{
						int first = (int) inMiles/1000;
						int second = (int) inMiles - (first*1000);
						String secondPart = NOTHING+second;
						if (second<100)
						{
							secondPart = "0"+second;
							if (second<10)
							{
								secondPart = "00"+second;
							}
						}
						dist = first+","+secondPart+" miles";
					}
				}
				googleDistance = (int)MainActivity.this.distance;
				
				if (googleDistance>1000)
				{
					isDrawing = false;
				}
				else
				{
					isDrawing = true;
				}
				
				if (isDrawing)
				{
					JSONArray steps = leg.getJSONArray(TAG_STEPS);
					JSONObject step;
					for (int i=0; i<steps.length(); i++)
					{
						step = steps.getJSONObject(i);
						JSONObject polyline = step.getJSONObject(TAG_POLYLINE);
						String points = polyline.getString("points");
						List<LatLng> list = decodePoly(points);
						for (int j=0; j<list.size(); j++)
						{
							HashMap<String,String> hm = new HashMap<String, String>();
							hm.put("lat", Double.toString(list.get(j).latitude));
							hm.put("lng", Double.toString(list.get(j).longitude));
							path.add(hm);
						}
					}
					polyRoutes.add(path);
				}
				else
				{
					JSONObject startLocation = leg.getJSONObject(TAG_START_LOCATION);
					start = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
					
					JSONObject endLocation = leg.getJSONObject(TAG_END_LOCATION);
					stop = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
				}
				
				JSONObject duration = leg.getJSONObject(TAG_DURATION);
				String dura = duration.getString("text");
				googleDuration = parseDuration(dura);
				return dist + "\n"+resultTime(dura);
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				return "N/A";
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				if (kmDefault)
				{
					return "0.0 km\n0:00 h";
				}
				return "0.0 miles\n0:00 h";
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return "N/A";
			}
		}
		
		@Override
		protected void onPostExecute(String result) 
		{
			try
			{
				ArrayList<LatLng> points = null;
				PolylineOptions lineOptions = null;
			
				startTracking.setText("Save");
				startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.upload, 0, 0, 0);
				startTracking.setBackgroundResource(R.drawable.btn_green_holo_dark);
				resultInfo.setText(result);
				plusMinus.setVisibility(View.VISIBLE);
				switch(category)
				{
				case START_LOCATION:
					updateLocationView(START_LOCATION-7, startLocation);
					break;
				case STOP_LOCATION:
					updateLocationView(STOP_LOCATION-7, stopLocation);
					break;
				}
			
				if (isDrawing)
				{
					for (int i=0;i<polyRoutes.size();i++)
					{
						points = new ArrayList<LatLng>();
						lineOptions = new PolylineOptions();
				
						List<HashMap<String,String>> path = polyRoutes.get(i);
				
						for (int j=0; j<path.size(); j++)
						{
							HashMap<String,String> point = path.get(j);
					
							double lat = Double.parseDouble(point.get("lat"));
							double lng = Double.parseDouble(point.get("lng"));
							LatLng position = new LatLng(lat,lng);
							if (j==0 && i==0)
							{
								start = position;
							}
							if (j==path.size()-1 && i==polyRoutes.size()-1)
							{
								stop = position;
							}
					
							points.add(position);
						}
				
						lineOptions.addAll(points);
						lineOptions.width(5);
						lineOptions.color(Color.rgb(130,130,250));
					}
					hiddenMap.addPolyline(lineOptions);
				}
			
				hiddenMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				hiddenMap.addMarker(new MarkerOptions().position(stop).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			
				LatLngBounds.Builder builder = new Builder();
			
				builder.include(start);
				builder.include(stop);
			
				hiddenMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
			
				showProgress(false);
			}
			catch (Exception e)
			{
				showProgress(false);
			}
		}
		
		private String request(String origin,String destination)
		{
			try
			{
				return "https://maps.googleapis.com/maps/api/directions/json?origin="+URLEncoder.encode(convertForRequest(origin), "UTF-8")+"&destination="+URLEncoder.encode(convertForRequest(destination),"UTF-8")+"&sensor=false";
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		private String convertForRequest(String request)
		{
			String convertedRequest = request.replace(" ", "+");
			System.out.println(convertedRequest);
			return convertedRequest;
		}
		
		private int parseDuration(String duration)
		{
			if (duration.contains("mins") || duration.contains("min"))
			{
				if (duration.contains("hours") || duration.contains("hour"))
				{
					duration = duration.replace(" mins", NOTHING);
					duration = duration.replace(" min", NOTHING);
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					String[] durations = duration.split(" ");
					return (60*Integer.parseInt(durations[0]))+Integer.parseInt(durations[1]);
				}
				else
				{
					duration = duration.replace(" mins", NOTHING);
					duration = duration.replace(" min", NOTHING);
					return Integer.parseInt(duration);
				}
			}
			else
			{
				if (duration.contains("days") || duration.contains("day"))
				{
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					duration = duration.replace(" days", NOTHING);
					duration = duration.replace(" day", NOTHING);
					String[] durations = duration.split(" ");
					return (24*Integer.parseInt(durations[0]))+Integer.parseInt(durations[1]);
				}
				else
				{
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					return 24*Integer.parseInt(duration);
				}
			}
		}
		
		private String resultTime(String duration)
		{
			if (duration.contains("mins") || duration.contains("min"))
			{
				if (duration.contains("hours") || duration.contains("hour"))
				{
					duration = duration.replace(" mins", NOTHING);
					duration = duration.replace(" min", NOTHING);
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					String[] durations = duration.split(" ");
					if (Integer.parseInt(durations[1])<10)
					{
						return durations[0]+":0"+durations[1]+" h";
					}
					return durations[0]+":"+durations[1] +" h";
				}
				else
				{
					duration = duration.replace(" mins", "");
					duration = duration.replace(" min", NOTHING);
					if (Integer.parseInt(duration)<10)
					{
						return "0:0"+duration+" h";
					}
					return "0:"+duration+" h";
				}
			}
			else
			{
				if (duration.contains("days") || duration.contains("day"))
				{
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					duration = duration.replace(" days", NOTHING);
					duration = duration.replace(" day", NOTHING);
					String[] durations = duration.split(" ");
					duration = durations[0]+" days "+durations[1]+":00 h";
					return duration;
				}
				else
				{
					duration = duration.replace(" hours", NOTHING);
					duration = duration.replace(" hour", NOTHING);
					duration = duration+":00 h";
					return duration;
				}
			}
		}
		
		/*
		 * Method to decode polyline points
		 */
		private List<LatLng> decodePoly(String encoded) 
		{ 
			List<LatLng> poly = new ArrayList<LatLng>();
		    int index = 0, len = encoded.length();
		    int lat = 0, lng = 0;
		 
		    while (index < len) 
		    {
		    	int b, shift = 0, result = 0;
		        
		    	do 
		        {
		        	b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        
		        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lat += dlat;
		 
		        shift = 0;
		        result = 0;
		        
		        do 
		        {
		        	b = encoded.charAt(index++) - 63;
		            result |= (b & 0x1f) << shift;
		            shift += 5;
		        } while (b >= 0x20);
		        
		        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		        lng += dlng;
		 
		        LatLng p = new LatLng((((double) lat / 1E5)),
		                             (((double) lng / 1E5)));
		        poly.add(p);
		    }
		    return poly;
		}
	}

	/**
	 * Task for off-line (GPS) tracking, could be paused or resumed
	 */
	private class TrackingTask extends AsyncTask<Void,String,Integer> implements LocationListener
	{	
		private double latitude;
		private double longitude;
		
		private Location location;
		
		private LatLng start;
		private LatLng end;
		
		private ArrayList<Location> trackedLocations;
		
		private int currentInterval;
	
		@Override
		protected void onPreExecute() 
		{
			try
			{
				hiddenMap.clear();
				start = null;
				end = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				start = null;
				end = null;
			}
		}

		@Override
		protected Integer doInBackground(Void... arg0) 
		{
			try
			{
				currentInterval = interval*1000;
				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currentInterval, 0, this, getMainLooper());
				trackedLocations = new ArrayList<Location>();
				boolean justOnce = false;
				while(true)
				{
					while (isTracking)
					{
						justOnce = true;
						Thread.sleep(currentInterval);
						if (latitude!=0 && longitude!=0)
						{
							trackedLocations.add(location);
						}
						if (isCancelled())
						{
							return null;
						}
					}
					if (justOnce)
					{
						Location nullLocation = new Location("nullLocation");
						nullLocation.setLatitude(0);
						nullLocation.setLongitude(0);
						trackedLocations.add(nullLocation);
						justOnce = false;
					}
					if (isCancelled())
					{
						return null;
					}
				}
			}
			catch(Exception e)
			{
				return null;
			}
		}
		
		public double calculateDistance(ArrayList<Location> locations)
		{
			double greaterSum = 0;
			double sum = 0;
			for (int i=0; i<locations.size()-1; i++)
			{
				if ((locations.get(i).getLatitude()==0 && locations.get(i).getLongitude()==0) || (locations.get(i+1).getLatitude()==0 && locations.get(i+1).getLongitude()==0))
				{
					greaterSum += sum;
					sum = 0;
				}
				else
				{
						Location a = locations.get(i);
						Location b = locations.get(i+1);
				
						sum += a.distanceTo(b);
				}
			}
			greaterSum += sum;
			greaterSum /= 1000;
			return Double.valueOf(new DecimalFormat("#.#").format(greaterSum));
		}
		
		@Override
		protected void onCancelled() 
		{
			try
			{
				manager.removeUpdates(this);
				if (trackedLocations != null)
				{
					boolean hasLocations = false;
					
					if (trackedLocations.size()>0)
					{
						for (int i=0; i<trackedLocations.size(); i++)
						{
							if (trackedLocations.get(i).getLatitude() != 0 && trackedLocations.get(i).getLongitude() != 0)
							{
								hasLocations = true;
								break;
							}
						}
					}
					
					if (hasLocations)
					{
						double trackedDistance = calculateDistance(trackedLocations);
						if (!kmDefault)
						{
							MainActivity.this.distance = trackedDistance*0.62137119;
						}
						else
						{
							MainActivity.this.distance = trackedDistance;
						}
						if (trackedDistance<0.1)
						{
							if (kmDefault)
							{
								resultInfo.setText("0.0 km\n"+resultTime()+" h");
							}
							else
							{
								resultInfo.setText("0.0 miles\n"+resultTime()+" h");
							}
						}
						else
						{
							if (trackedDistance>1000)
							{
								if (!kmDefault)
								{
									trackedDistance *= 0.62137119;
								}
								int first = (int) trackedDistance/1000;
								int second = (int) trackedDistance - (first*1000);
								String secondPart = NOTHING+second;
								if (second<100)
								{
									secondPart = "0"+second;
									if (second<10)
									{
										secondPart = "00"+second;
									}
								}
								if (kmDefault)
								{
									resultInfo.setText(first+","+secondPart+" km\n"+resultTime()+" h");
								}
								else
								{
									resultInfo.setText(first+","+secondPart+" miles\n"+resultTime()+" h");
								}
							}
							else
							{
								if (kmDefault)
								{
									resultInfo.setText(trackedDistance+" km\n"+resultTime()+" h");
								}
								else
								{
									resultInfo.setText((trackedDistance*0.62137119)+" miles\n"+resultTime()+" h");
								}
							}
						}
					
						Location stop = trackedLocations.get(trackedLocations.size()-1);
					
						try
						{
							stopLocation = new DriveLocation(null, getAddressName(getAddress(stop)));
							updateLocationView(STOP_LOCATION-7, stopLocation);
						}
						catch (Exception e)
						{
							stopPending = true;
							updateLocationView(STOP_LOCATION-7, new DriveLocation(null, "Location pending,  Check your Connection"));
							pendingStopLocation = stop;
						}
						start = new LatLng(trackedLocations.get(0).getLatitude(), trackedLocations.get(0).getLongitude());
						end = new LatLng(trackedLocations.get(trackedLocations.size()-1).getLatitude(), trackedLocations.get(trackedLocations.size()-1).getLongitude());
						trackedLocations = null;
					
						hiddenMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
						hiddenMap.addMarker(new MarkerOptions().position(end).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				
						LatLngBounds.Builder builder = new Builder();
				
						builder.include(start);
						builder.include(end);
				
						hiddenMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));
					
						plusMinus.setVisibility(View.VISIBLE);
					}
					else
					{
						if (kmDefault)
						{
							resultInfo.setText("0.0 km\n"+resultTime()+" h");
						}
						else
						{
							resultInfo.setText("0.0 miles\n"+resultTime()+" h");
						}
						MainActivity.this.distance = 0;
						trackedLocations = null;
						stopLocation = startLocation;
						updateLocationView(STOP_LOCATION-7, stopLocation);
						updateTimeLocationView(STOP_LOCATION-7);
						plusMinus.setVisibility(View.VISIBLE);
					}
				}
				else
				{
					resultInfo.setText("N/A\n"+"-:-- h");
					updateLocationView(STOP_LOCATION-7, new DriveLocation(null, "Tracking Failed,  Make sure GPS is on!"));
				}
				showProgress(false);
			}
			catch(Exception e)
			{
				trackedLocations = null;
				stopLocation = startLocation;
				updateLocationView(STOP_LOCATION-7, stopLocation);
				updateTimeLocationView(STOP_LOCATION-7);
				MainActivity.this.distance = 0;
				plusMinus.setVisibility(View.VISIBLE);
				showProgress(false);
				super.onCancelled();
			}
		}
		
		private Address getAddress(Location location)
		{
			try
			{
				Geocoder geocoder = new Geocoder(MainActivity.this);
				
				List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
			
				Address address = addresses.get(0);
				return address;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		private String getAddressName(Address address)
		{
			String addressName = address.getAddressLine(0)+",  "+address.getAddressLine(1);
			return addressName;
		}
		private String resultTime()
		{
			long result;
			if (pauseMillis.size()>0)
			{
				long pauseTime = 0;
				for (int i=0; i<pauseMillis.size(); i++)
				{
					pauseTime += (resumeMillis.get(i)-pauseMillis.get(i));
				}
				result = (stopMillis - pauseTime - startMillis)/1000;
			}
			else
			{
				result = (stopMillis - startMillis)/1000;
			}
			result /= 60;
			if (result<60)
			{
				if (result<10)
				{
					return "0:0"+result;
				}
				return "0:"+result;
			}
			int hours = (int) result/60;
			result -= (hours*60);
			if (result<10)
			{
				return hours+":0"+result;
			}
			return hours+":"+result;
		}
		
		
//		/*
//		 * LocationListener implementation:
//		 */
		@Override
		public void onLocationChanged(Location location) 
		{
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			this.location = location;
		}

		@Override
		public void onProviderDisabled(String arg0) {}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
	}

	/**
	 * Initialize UI, Hidden Map and Current Location for it as well
	 */
	private class InitializeUI extends AsyncTask<Void, Void, CameraUpdate> implements com.google.android.gms.location.LocationListener,OnConnectionFailedListener,ConnectionCallbacks
	{
		private Location currentLocation;
		
		@Override
		protected void onPreExecute() 
		{
			Handler handler = new Handler();
    		handler.postDelayed(new Runnable()
    		{
    		  @Override
    		  public void run() 
    		  {
    		      if (initialize.getStatus() == AsyncTask.Status.RUNNING)
    		      {
    		          AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
    					builder.setMessage(TIMEOUT_ERROR)
    					       .setCancelable(false)
    					       .setNegativeButton(CANCEL, new DialogInterface.OnClickListener() 
    					       {
    					           public void onClick(DialogInterface dialog, int id) 
    					           {
    					                dialog.dismiss();
    					                toSettings = false;
    					                initialize.cancel(true);
    					           }
    					       })
    					       .setNeutralButton(CONTINUE, new DialogInterface.OnClickListener() 
    					       {
    					    	   public void onClick(DialogInterface dialog, int id) 
    					    	   {
    					    		   dialog.dismiss();
    					    	   }
    					       })
    					       .setPositiveButton(GPS_SWITCH, new DialogInterface.OnClickListener() 
    					       {
    					    	   public void onClick(DialogInterface dialog, int id) 
    					    	   {                
    					               dialog.dismiss();
    					               toSettings = true;
    					               initialize.cancel(true);	
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
		protected CameraUpdate doInBackground(Void... arg0) 
		{
			try
			{
				date = null;
				tripDate = null;
				job = null;
				creditor = null;
				comments = null;
				startLocation = null;
				stopLocation = null;
				googleDistance = 0;
				googleDuration = 0;
				distance = 0;
				
				preferences.edit().putBoolean("kmDefault",kmDefault);
				preferences.edit().putInt("interval", interval);
				preferences.edit().commit();
				
				isTracking = false;
				startPending = false;
				stopPending = false;
				
				fManager = getFragmentManager();
				map = (MapFragment) fManager.findFragmentById(R.id.hiddenMap);
				
				trackingStatus = findViewById(R.id.tracking_status);
				appView = findViewById(R.id.app_view);
				statusMessage = (TextView) findViewById(R.id.track_status_message);
				plusMinus = findViewById(R.id.plusMinus);
				
				publishProgress();
				
				listLocation = (ListView) findViewById(R.id.listLocation);
				listDetails = (ListView) findViewById(R.id.listDetails);
			
				startTracking = (Button) findViewById(R.id.startTracking);
				stopTracking = (Button) findViewById(R.id.stopTracking);
				pauseTracking = (Button) findViewById(R.id.pauseTracking);
				plus = (Button) findViewById(R.id.plus);
				minus = (Button) findViewById(R.id.minus);
				resultInfo = (TextView) findViewById(R.id.resultView);
				
				distanceUnit = (Spinner) findViewById(R.id.unit_spinner);
				intervalBar = (SeekBar) findViewById(R.id.intervalBar);
				intervalValue = (TextView) findViewById(R.id.intervalValue);
				
				logoutButton = (Button) findViewById(R.id.logoutButton);
				
				// Listeners are located after the location services... before AsyncTasks and Broadcast Receiver
				startTracking.setOnClickListener(MainActivity.this);
				pauseTracking.setOnClickListener(MainActivity.this);
				stopTracking.setOnClickListener(MainActivity.this);
				plus.setOnClickListener(MainActivity.this);
				minus.setOnClickListener(MainActivity.this);
				intervalBar.setOnSeekBarChangeListener(MainActivity.this);
				distanceUnit.setOnItemSelectedListener(MainActivity.this);
				logoutButton.setOnClickListener(MainActivity.this);
				
				if (isNetworkAvailable())
				{
					listLocation.setOnItemClickListener(MainActivity.this);
					listDetails.setOnItemClickListener(MainActivity.this);
				
					hiddenMap = (GoogleMap) map.getMap();
					
					JSONParser jParser = new JSONParser();
					try
					{
						JSONArray dbObjects = jParser.getJSONArrayFromUrl(MainActivity.getServer(),"/api/mileage/creditors");
						if (dbObjects.length()<2 && dbObjects.length()>0)
						{
							JSONObject dbObject = dbObjects.getJSONObject(0);
							
							int empArpAccId = dbObject.getInt("EmpArpAccountId");
							int empId = dbObject.getInt("EmpId");
							int id = dbObject.getInt("ArpAccId");
							boolean useMileage = dbObject.getBoolean("UseMileage");
							String no = dbObject.getString("ArpAccNo");
							String name = dbObject.getString("ArpAccName");
							creditor = new Creditor(empArpAccId, empId, id, useMileage, no, name);
						}
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
				
				client = new LocationClient(MainActivity.this, this, this);
				client.connect();
				
				while (currentLocation == null)
				{
					Thread.sleep(1);
				}
				
				try
				{
					startLocation = new DriveLocation(null,getAddressName(getAddress(currentLocation)));
				}
				catch (Exception e)
				{
					e.printStackTrace();
					startPending = true;
					pendingStartLocation = client.getLastLocation();
				}
				
				client.disconnect();
				return CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15);

			}
			catch (Exception e)
			{
				e.printStackTrace();
				client.disconnect();
				return null;
			}
		}
		
		@Override
		protected void onProgressUpdate(Void... values) 
		{
			showProgress(true);
			statusMessage.setVisibility(View.INVISIBLE);
		}

		@Override
		protected void onPostExecute(CameraUpdate update) 
		{
			try
			{
				listLocation.setAdapter(populateLocationList());
				listDetails.setAdapter(populateDetailsList());
				
				startTracking.setText(START_TRACKING_LABEL);
				startTracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.traffic_lights, 0, 0, 0);
				startTracking.setBackgroundResource(R.drawable.btn_blue_holo_dark);
				
				ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.distance_unit_values, R.layout.spinner_item);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				distanceUnit.setAdapter(adapter);
				
				if (preferences.getBoolean("kmDefault", true))
				{
					kmDefault = true;
					distanceUnit.setSelection(0);
				}
				else
				{
					kmDefault = false;
					distanceUnit.setSelection(1);
				}
				
				if (kmDefault)
				{
					resultInfo.setText(NOT_DEFINED_DISTANCE_KM);
				}
				else
				{
					resultInfo.setText(NOT_DEFINED_DISTANCE_MILES);
				}
				
				interval = preferences.getInt("interval", 30);
				intervalBar.setProgress(interval);
				intervalValue.setText(interval+" "+getResources().getString(R.string.tracking_interval_value));
				
				if (creditor!=null)
				{
					updateDetailsView(CREDITOR, creditor);
				}
				
				if (startPending)
				{
					updateLocationView(START_LOCATION-7, new DriveLocation(null, "Location pending,  Check your Connection"));
				}
				else
				{
					updateLocationView(START_LOCATION-7, startLocation);
				}
				
				plusMinus.setVisibility(View.GONE);
				
				hiddenMap.setMyLocationEnabled(true);
				hiddenMap.clear();
				hiddenMap.moveCamera(update);
				fManager.beginTransaction().hide(map).commit();
				isMapHidden = true;
				if (isNetworkAvailable())
				{
					menuMap.setEnabled(true);
				}
				showProgress(false);
			}
			catch (Exception e)
			{
				try
				{
					fManager.beginTransaction().hide(map).commit();
					isMapHidden = true;
					showProgress(false);
				}
				catch (Exception er)
				{
					isMapHidden = false;
					showProgress(false);
				}
			}
		}

		@Override
		protected void onCancelled() 
		{
			Editor edit = preferences.edit();
			edit.putBoolean("saveLogin", false);
			edit.putBoolean("logout", true);
			if (toSettings)
			{
				edit.putBoolean("toSettings", true);
			}
			edit.commit();
			Intent logout = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(logout);
			finish();
		}

		@Override
		public void onConnected(Bundle bundle) 
		{
			currentLocation = client.getLastLocation();
		}

		@Override
		public void onDisconnected() {}

		@Override
		public void onConnectionFailed(ConnectionResult result) {}

		@Override
		public void onLocationChanged(Location arg0) {}
	}
	
//	**************************************************************************************************************************
//					Broadcast Receiver for Checking Internet Connection throughout the life of the application
//	**************************************************************************************************************************
	
	private class ConnectionChangeReceiver extends BroadcastReceiver
	{
		private final Handler handler;
		
		public ConnectionChangeReceiver(Handler handler)
		{
			this.handler = handler;
		}

		@Override
		public void onReceive(final Context context, Intent data) 
		{
			handler.post(new Runnable() 
			{	
				@Override
				public void run() 
				{
					if (!isNetworkAvailable())
					{
						listLocation.setOnItemClickListener(new OnItemClickListener() 
						{
							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {}
						});
						listDetails.setOnItemClickListener(new OnItemClickListener() 
						{
							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {}
						});
						
						if (menuMap.getTitle().equals("Hide Map"))
						{
							hideMap();
							menuMap.setTitle(R.string.showMap);
						}
						menuMap.setEnabled(false);
						if (!MainActivity.this.hasWindowFocus())
						{
							for (Activity a : allactivities)
							{
								a.finish();
							}
							allactivities.clear();
						}
					}
					else
					{
						try
						{
							if (startPending)
							{
								startLocation = new DriveLocation(null, getAddressName(getAddress(pendingStartLocation)));
								updateLocationView(START_LOCATION-7, startLocation);
								startPending = false;
							}
							if (stopPending)
							{
								stopLocation = new DriveLocation(null,getAddressName(getAddress(pendingStopLocation)));
								updateLocationView(STOP_LOCATION-7, stopLocation);
								stopPending = false;
							}
							menuMap.setEnabled(true);
							listLocation.setOnItemClickListener(MainActivity.this);
							listDetails.setOnItemClickListener(MainActivity.this);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			});
			
		}

	}
	
}
