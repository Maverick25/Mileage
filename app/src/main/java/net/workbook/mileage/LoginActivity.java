package net.workbook.mileage;


import java.security.MessageDigest;
import org.json.JSONObject;
import net.workbook.mileage.interfaces.Finals;
import net.workbook.mileage.support.JSONParser;
import net.workbook.mileage.support.LoginClient;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity
implements OnEditorActionListener,OnClickListener,Finals
{
	// AsyncTasks
	private UserLoginTask loginTask;
	
	// Animations
	private View loginForm;
	private View loginStatus;
	private TextView loginStatusMessage;
	
	// Inputs
	private EditText serverView;
	private EditText usernameView;
	private EditText passwordView;
	private Switch rememberSwitch;
	private Button signInButton;
	
	// Values for server, user name and password at the time of the login attempt
	private String server;
	private String username;
	private String password;
	
	// Phone memory-related options
	private SharedPreferences preferences;
	
	/**
	 * Start of the Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_login);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		loginForm = findViewById(R.id.login_form);
		loginStatus = findViewById(R.id.login_status);
		loginStatusMessage = (TextView) findViewById(R.id.login_status_message);
		
		if (preferences.getBoolean("saveLogin", false))
		{
			showProgress(true);
			
			server = preferences.getString("server", "");
			username = preferences.getString("username", "");
			password = preferences.getString("password", "");
			
			loginStatusMessage.setText(R.string.login_progress_signing_in);
			loginTask = new UserLoginTask();
			loginTask.execute(server,username,password);
		}
		
		// Set up the login form.
		serverView = (EditText) findViewById(R.id.server);
		usernameView = (EditText) findViewById(R.id.username);
		passwordView = (EditText) findViewById(R.id.password);
		rememberSwitch = (Switch) findViewById(R.id.remember_switch);
		signInButton = (Button) findViewById(R.id.sign_in_button);
		
		passwordView.setOnEditorActionListener(this);
		signInButton.setOnClickListener(this);
		
		if (preferences.getBoolean("logout", false))
		{
			LoginClient.clearCookies();
			serverView.setText(preferences.getString("server", ""));
			usernameView.setText(preferences.getString("username", ""));
			passwordView.setText(preferences.getString("password", ""));
			if (preferences.getBoolean("toSettings", false))
			{
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        	startActivity(intent);		
			}
			preferences.edit().clear().commit();
		}
	}
	
	/*
	 * Additional Methods
	 */
	private void attemptLogin()
	{
		// If Login is in process then cancel another attempt.
		if (loginTask != null)
		{
			return;
		}
		
		// Reset errors
		serverView.setError(null);
		usernameView.setError(null);
		passwordView.setError(null);
		
		// Store values at the time of the login attempt
		server = serverView.getText().toString();
		username = usernameView.getText().toString();
		password = passwordView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;
		
		// Check for a valid server
		if (TextUtils.isEmpty(server))
		{
			serverView.setError(getString(R.string.error_field_required));
			focusView = serverView;
			cancel = true;
		}
		
		// Check for a valid user name
		if (TextUtils.isEmpty(username))
		{
			usernameView.setError(getString(R.string.error_field_required));
			focusView = usernameView;
			cancel = true;
		}
		else
		{
			if (username.length() < 2)
			{
				usernameView.setError(getString(R.string.error_invalid_username));
				focusView = usernameView;
				cancel = true;
			}
		}
		
		// Check for a valid password
		if (TextUtils.isEmpty(password))
		{
			passwordView.setError(getString(R.string.error_field_required));
			focusView = passwordView;
			cancel = true;
		}
		else
		{
			if (password.length() < 2)
			{
				passwordView.setError(getString(R.string.error_invalid_password));
				focusView = passwordView;
				cancel = true;
			}
		}
		
		if (cancel)
		{
			// There was an error; don't attempt login and focus the first
			// form field with an error
			focusView.requestFocus();
		}
		else
		{
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt
			loginStatusMessage.setText(R.string.login_progress_signing_in);
			showProgress(true);
			loginTask = new UserLoginTask();
			loginTask.execute(server,username,password);
		}
	}
	
	private void showProgress(final boolean show) 
	{
		// If available, use these APIs to fade-in the progress spinner
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) 
		{
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			loginStatus.setVisibility(View.VISIBLE);
			loginStatus.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							loginStatus.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
			});

			loginForm.setVisibility(View.VISIBLE);
			loginForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() 
			{
						@Override
						public void onAnimationEnd(Animator animation) 
						{
							loginForm.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
			});
		} 
		else 
		{
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components
			loginStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
//************************************************************************************************************	
//											UI Listeners
//************************************************************************************************************	
	
	/**
	 * Button Listener:
	 */
	@Override
	public void onClick(View v) 
	{
		attemptLogin();
	}

	/**
	 * PasswordField Editor Listener:
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == R.id.login || actionId == EditorInfo.IME_NULL)
		{
			attemptLogin();
			return true;
		}
		return false;
	}

//*************************************************************************************************************	
//										ASYNC TASK IMPLEMENTATION
//*************************************************************************************************************	
	
	/**
	 * Represents an asynchronous login task used to authenticate the user
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean>
	{
		private String errorMessage;
		private int id;
		
		@Override
		protected void onPreExecute() 
		{
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			if (!preferences.getBoolean("saveLogin", false))
			{
				imm.hideSoftInputFromWindow(passwordView.getWindowToken(), 0);
			}
			errorMessage = null;
			Handler handler = new Handler();
    		handler.postDelayed(new Runnable()
    		{
    		  @Override
    		  public void run() 
    		  {
    			  try
    			  {
    				  if (loginTask.getStatus() == AsyncTask.Status.RUNNING)
    				  {
    					  AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
    					  builder.setMessage(TIMEOUT_LOGIN_ERROR)
    					       .setCancelable(false)
    					       .setNeutralButton(CONTINUE, new DialogInterface.OnClickListener() 
    					       {
    					           public void onClick(DialogInterface dialog, int id) 
    					           {
    					                dialog.dismiss();
    					           }
    					       })
    					       .setPositiveButton(CANCEL, new DialogInterface.OnClickListener() 
    					       {
    					    	   public void onClick(DialogInterface dialog, int id) 
    					    	   {
    					    		   dialog.dismiss();
    					    		   loginTask.cancel(true);
    					    	   }
    					       });

    					  AlertDialog alert = builder.create();
    					  alert.show();
    					  TextView messageView = (TextView)alert.findViewById(android.R.id.message);
    					  messageView.setGravity(Gravity.CENTER);
    				  }
    			  }
    			  catch (NullPointerException e)
    			  {
    				  e.printStackTrace();
    			  }
    		  }
    		}, 30000 );
		}

		@Override
		protected Boolean doInBackground(String... params) {
			JSONParser jParser = new JSONParser();
			try
			{
				String encrypted = jParser.getResponse(requestResponse(params[0], params[1])[0],requestResponse(params[0], params[1])[1]);
				System.out.println(encrypted);
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(params[2].getBytes());
				byte[] digest = md.digest();
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<digest.length; i++)
				{
					sb.append(Integer.toString((digest[i] & 0xff) + 0x100,16).substring(1));
				}
				String hashedStr = sb.toString();
				String combined = hashedStr+encrypted;
				md.update(combined.getBytes());
				digest = md.digest();
				sb = new StringBuffer();
				for (int i=0; i<digest.length; i++)
				{
					sb.append(Integer.toString((digest[i] & 0xff) + 0x100,16).substring(1));
				}
				hashedStr = sb.toString();
				
				JSONObject json = jParser.getLoginSessionFromUrl(requestLogin(params[0], params[1], hashedStr)[0],requestLogin(params[0], params[1], hashedStr)[1]);
				
				System.out.println(json.toString());
				try
				{
					id = (Integer) json.get("Id");
					return true;
				}
				catch (Exception e)
				{
					errorMessage = "Wrong username or password!";
					return false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				errorMessage = "Wrong Server name or there is no connection with the Server!";
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) 
		{
			loginTask = null;
			
			if (success)
			{
				if (!preferences.getBoolean("saveLogin", false))
				{
					Editor edit = preferences.edit();
					if (rememberSwitch.isChecked())
					{
						edit.putBoolean("saveLogin", true);
					}
					edit.putString("server", server);
					edit.putString("username", username);
					edit.putString("password", password);
					edit.commit();
				}
				
				Intent main = new Intent(LoginActivity.this,MainActivity.class);
				main.putExtra(SERVER, server);
				main.putExtra(ID, id);
				startActivity(main);
				finish();
			}
			else
			{
				showProgress(false);
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setMessage(errorMessage)
				       .setCancelable(false)
				       .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.dismiss();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				TextView messageView = (TextView)alert.findViewById(android.R.id.message);
				messageView.setGravity(Gravity.CENTER);
			}
		}

		@Override
		protected void onCancelled() 
		{
			loginTask = null;
			showProgress(false);
		}
		
		private String[] requestResponse(String server,String username)
		{
			if (server.contains("http://"))
			{
				if (server.charAt(server.length()-1) == '/')
				{
					String[] requests = {server,"api/auth/hello?username="+username}; 
					return requests;
				}
				else
				{
					String[] requests = {server,"/api/auth/hello?username="+username}; 
					return requests;
				}
			}
			else
			{
				if (server.charAt(server.length()-1) == '/')
				{
					String[] requests = {server,"api/auth/hello?username="+username}; 
					return requests;
				}
				else
				{
					String[] requests = {server,"/api/auth/hello?username="+username}; 
					return requests;
				}
				
			}
		}
		
		private String[] requestLogin(String server,String username,String hash)
		{
			char lastChar = server.charAt(server.length()-1);
			if (server.contains("http://"))
			{
				if (lastChar == '/')
				{
					String[] requests = {server,"api/auth?username="+username+"&password="+hash};
					return requests;
				}
				else
				{
					String[] requests = {server,"/api/auth?username="+username+"&password="+hash};
					return requests;
				}
			}
			else
			{
				if (lastChar == '/')
				{
					String[] requests = {server,"api/auth?username="+username+"&password="+hash};
					return requests;
				}
				else
				{
					String[] requests = {server,"/api/auth?username="+username+"&password="+hash};
					return requests;
				}
			}
		}
	}

}
