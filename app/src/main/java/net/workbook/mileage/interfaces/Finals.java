package net.workbook.mileage.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface Finals 
{
	/*
	 * MainActivity:
	 */
	public static final int START_LOCATION = 7;
	public static final int STOP_LOCATION = 8;
	
	public static final int DATE = 0;
	public static final int JOB = 1;
	public static final int MILEAGE_TYPE = 2;
	public static final int CAR = 3;
	public static final int CAR_REG_NO = 4;
	public static final int CREDITOR = 5;
	public static final int COMMENTS = 6;
	
	public static final String START_LOCATION_LABEL = "Start:";
	public static final String STOP_LOCATION_LABEL = "Stop:";
	
	public static final String DATE_LABEL = "Date:";
	public static final String JOB_LABEL = "Job:";
	public static final String MILEAGE_TYPE_LABEL = "Mileage Type:";
	public static final String CAR_LABEL = "Car:";
	public static final String CAR_REG_NO_LABEL = "Car Reg. No.:";
	public static final String CREDITOR_LABEL = "Creditor:";
	public static final String COMMENTS_LABEL = "Comments:";
	
	public static final String JOB_DEFAULT = "Select Job";
	public static final String MILEAGE_TYPE_DEFAULT = "Select Mileage Type";
	public static final String CAR_DEFAULT = "Select Car";
	public static final String CAR_REG_NO_DEFAULT = "Select Car.Reg.No";
	public static final String CREDITOR_DEFAULT = "Select Creditor";
	public static final String COMMENTS_DEFAULT = "Add Comments";
	
	public static final String CATEGORY = "CATEGORY";
	public static final String PREVIOUSLY = "PREVIOUSLY";
	public static final String SAVED_MAP = "SAVED_MAP";
	
	public static final String SELECTED_ITEM = "SELECTED_ITEM";
	public static final String SELECTED_ADDRESS = "SELECTED_ADDRESS";
	public static final String ADDED_COMMENTS = "ADDED_COMMENTS";
	public static final String CHANGED_DATE = "CHANGED_DATE";
	
	public static final String TIMEOUT_ERROR = "Host is not responding. Check your Connection!\nYour current location is trying to be found. Please make sure you have enabled Network to determine Locations!";
	public static final String TIMEOUT_LOADING_ERROR = "Host is not responding or Connection is too slow. Would you like to close the current window?";
	public static final String TIMEOUT_LOGIN_ERROR = "Host is not responding or Connection is too slow. Would you like to cancel the process?";
	public static final String RESET_QUESTION = "Are you sure you want to refresh tracking?";
	public static final String NO_CONNECTION = "No Connection. Make sure you are connected to the Internet!";
	public static final String NO_CONNECTION_TITLE = "Trip was NOT Saved";
	public static final String SAVE_SUCCESS = "The mileage has been saved but couldn't be approved. Please enter missing data in the main application.";
	public static final String SAVE_SUCCESS_TITLE = "Success";
	public static final String NO_GPS = "The GPS has been turned off on your Device. Would you like to switch it on?";
	public static final String GPS_SWITCH = "Go to Settings";
	public static final String QUIT_TRACKING = "Application is currently in Tracking mode. Would you like to select locations manually instead?";
	public static final String YES = "Yes";
	public static final String CANCEL = "Cancel";
	public static final String OK = "OK";
	public static final String CONTINUE = "Continue";
	public static final String CLOSE = "Close";
	
	public static final String HIDE_MAP = "Hide Map";
	public static final String SHOW_MAP = "Show Map";
	
	public static final String START_TRACKING_LABEL = "Start";
	public static final String SAVE_TRACKING_LABEL = "Save";
	public static final String PAUSE_TRACKING_LABEL = "Pause";
	public static final String RESUME_TRACKING_LABEL = "Resume";
	public static final String NOT_DEFINED_DISTANCE_KM = "-.- km\n-:-- h";
	public static final String NOT_DEFINED_DISTANCE_MILES = "-.- miles\n-:-- h";
	
	public static final String NOTHING = "";
	public static final String THREE_DOTS = "...";
	
	/*
	 * MapActivity
	 */
	public static final int PICK = 9;
	public static final int MANUAL = 10;
	public static final boolean FORMAP = true;
	
	public static final LatLng COPENHAGEN = new LatLng(55.676, 12.566);
	
	public static final String PICKED_ADDRESS ="PICKED_ADDRESS";
	public static final String MANUAL_ADDRESS ="MANUAL_ADDRESS";
	
	/*
	 * SelectionActivity:
	 */
	public static final int PICK_ADDRESS = 11;
	
	public static final String DRIVE_LOCATION = "DRIVE_LOCATION";

	public static final String ZERO_RESULTS = "No results for this address";
	
	/*
	 * LoginActivity:
	 */
	public static final String SERVER = "SERVER";
	public static final String ID = "ID";
}
