package net.workbook.mileage;

import java.util.List;

import org.json.JSONObject;

import net.workbook.mileage.interfaces.Finals;
import net.workbook.mileage.model.DriveLocation;
import net.workbook.mileage.support.JSONParser;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MapActivity extends FragmentActivity 
implements OnMapClickListener,OnMarkerDragListener,OnInfoWindowClickListener,LocationListener,Finals
{
	// Activity-Transfer
	private DriveLocation driveLocation;
	private Intent currentData;
	
	// Map-related Fields
	private GoogleMap map;
	private Marker pin;
	private LocationManager manager;
	private String pinText;
	
	/**
	 * Start of the Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_map);
		
		try
		{
			MainActivity.allactivities.add(this);
		
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		
			manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, getMainLooper());
		
			map.setMyLocationEnabled(true);
		
			switch (getIntent().getIntExtra(CATEGORY, 0))
			{
			case START_LOCATION:
				setTitle("Select Starting Location");
				pinText = "Start";
				break;
			case STOP_LOCATION:
				setTitle("Select Final Destination");
				pinText = "Stop";
				break;
			}
		
		
		
			map.setOnMapClickListener(this);
		
			map.setOnMarkerDragListener(this);
		
			map.setOnInfoWindowClickListener(this);
		}
		catch (Exception e)
		{
			MainActivity.allactivities.add(this);
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Creation of the Menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map, menu);
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
				currentData = new Intent();
				switch(requestCode)
				{
				case PICK:
					driveLocation = (DriveLocation) data.getParcelableExtra(MainActivity.SELECTED_ADDRESS);
				
					currentData.putExtra(SELECTED_ADDRESS, driveLocation);
					currentData.putExtra(SAVED_MAP,map.getCameraPosition());
					setResult(Activity.RESULT_OK, currentData);
					finish();
					break;
				case MANUAL:
					String address = data.getStringExtra(ADDED_COMMENTS);
					
					driveLocation = new DriveLocation(null,  address);
					currentData.putExtra(MainActivity.SELECTED_ADDRESS, driveLocation);
					currentData.putExtra(MainActivity.SAVED_MAP, map.getCameraPosition());
					setResult(Activity.RESULT_OK, currentData);
					finish();
					break;
				}
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
	private Address getAddress(LatLng pointer)
	{
		try
		{
			Geocoder geocoder = new Geocoder(this);
			
			List<Address> addresses = geocoder.getFromLocation(pointer.latitude,pointer.longitude,1);
		
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
	
	/*
	 * Location Updates:
	 */
	@Override
	public void onLocationChanged(Location location) 
	{
		try
		{
			if (getIntent().getExtras().getParcelable(MainActivity.PREVIOUSLY) == null)
			{
				LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
				map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
			}
			else
			{
				CameraPosition cameraPosition = (CameraPosition)getIntent().getExtras().getParcelable(MainActivity.PREVIOUSLY);
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target, cameraPosition.zoom));
			}
		}
		catch (Exception e)
		{
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(COPENHAGEN, 15));
			map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
//***********************************************************************************************************	
//										UI Listeners
//***********************************************************************************************************	
	
	/**
	 * ActionBar Listeners:
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		try
		{
			switch(item.getItemId())
			{
			case R.id.useCurrent:
				Location current = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
				Address currentAdd = getAddress(new LatLng(current.getLatitude(),current.getLongitude()));
				String currentAddToString = getAddressName(currentAdd);
			
				driveLocation = new DriveLocation(null,currentAddToString);
			
				currentData = new Intent();
				currentData.putExtra(MainActivity.SELECTED_ADDRESS, driveLocation);
				currentData.putExtra(MainActivity.SAVED_MAP,map.getCameraPosition());
				setResult(Activity.RESULT_OK, currentData);
				finish();
				return true;
			case R.id.fromList:
				Intent pickDescription = new Intent(this, SelectionActivity.class);
				pickDescription.putExtra(MainActivity.CATEGORY, PICK);
				startActivityForResult(pickDescription, PICK);
				return true;
			case R.id.manually:
				Intent addManually = new Intent(this, CommentsActivity.class);
				addManually.putExtra(CATEGORY, FORMAP);
				startActivityForResult(addManually,MANUAL);
				return true;
			case R.id.home:
				new GetHomeorOffice().execute(true);
				return true;
			case R.id.office:
				new GetHomeorOffice().execute(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * MapClick Listener:
	 */
	@Override
	public void onMapClick(LatLng point) 
	{
		try
		{
			if (pin == null)
			{
				pin = map.addMarker(new MarkerOptions().position(point));
				pin.setDraggable(true);
			}
			else
			{
				pin.setPosition(point);
			}
			pin.setTitle(pinText);
			pin.setSnippet(getAddressName(getAddress(point)));
			pin.showInfoWindow();
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * MarkerDrag Listeners:
	 */
	@Override
	public void onMarkerDrag(Marker arg0) {}

	@Override
	public void onMarkerDragEnd(Marker marker) 
	{
		try
		{
			marker.setTitle(pinText);
			marker.setSnippet(getAddressName(getAddress(marker.getPosition())));
			marker.showInfoWindow();
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onMarkerDragStart(Marker marker) 
	{
		try
		{
			marker.setTitle(pinText);
			marker.setSnippet(getAddressName(getAddress(marker.getPosition())));
			marker.showInfoWindow();
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * InfoWindowClick Listener:
	 */
	@Override
	public void onInfoWindowClick(Marker marker) 
	{
		try
		{
			Address address = (Address) getAddress(marker.getPosition());
			String addressToString = getAddressName(address);
		
			driveLocation = new DriveLocation(null,addressToString);
		
			currentData = new Intent();
			currentData.putExtra(MainActivity.SELECTED_ADDRESS, driveLocation);
			currentData.putExtra(MainActivity.SAVED_MAP,map.getCameraPosition());
			setResult(Activity.RESULT_OK, currentData);
			finish();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
//***********************************************************************************************************	
//									ASYNC TASK IMLEMENTATIONS:
//***********************************************************************************************************
	
	private class GetHomeorOffice extends AsyncTask<Boolean, Void, String>
	{

		@Override
		protected String doInBackground(Boolean... home) 
		{
			JSONParser jParser = new JSONParser();
			try
			{
				JSONObject profile = jParser.getJSONbyGet(MainActivity.getServer(), "/api/mileage/EmployeeAddresses/"+MainActivity.getId());
				String address;
				if(home[0])
				{
					address = (String) profile.get("HomeAddress");
				}
				else
				{
					address = (String) profile.get("OfficeAddress");
				}
				return address;
			}
			catch (Exception e)
			{
				return null;
			}
			
		}

		@Override
		protected void onPostExecute(String address) 
		{
			try
			{
				if (!address.equals(null))
				{
					driveLocation = new DriveLocation(null,  convertAddress(address));
					
					currentData = new Intent();
					currentData.putExtra(SELECTED_ADDRESS, driveLocation);
					currentData.putExtra(SAVED_MAP, map.getCameraPosition());
					setResult(Activity.RESULT_OK, currentData);
					finish();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		private String convertAddress(String address)
		{	
			if (address.contains("\r\n"))
			{
				address = address.replace("\r\n", ", ");
			}
			
			String[] parts = address.split(", ");
			parts[parts.length-1] = parts[parts.length-1].trim();
			if (!parts[0].contains(" "))
			{
				
			}
			
			for (int i=0; i<parts.length; i++)
			{
				parts[i] = parts[i].trim();
				if (i==0)
				{
					address = parts[i]+",  ";
				}
				else
				{
					if (i==parts.length-1)
					{
						address += parts[i];
					}
					else
					{
						address += parts[i]+", ";
					}
				}
			}
			
			return address;
		}
		
	}
}
