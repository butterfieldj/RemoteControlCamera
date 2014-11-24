package jacob.usu.remotecameracontroller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RemoteCameraControllerMainActivity extends Activity
{
	private static final int PORT = 12345;
	private static final int VIEW_SAVED_IMAGES_REQ = 100;
	
	private RemoteCameraControllerMainActivity _thisApp;
	
	private Socket _socket;
	private Thread _commandThread = null;
	private Thread _saveThread = null;
	
	private boolean _cameraRunning = false;
	
	private Button _startButton = null;
	private Button _stopButton = null;
	private Button _saveButton = null;
	private Button _alarmButton = null;
	
	private WebView _cameraFeedWebView = null;
	private EditText _socketEditText = null;
	
	private Resources _resources = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_resources = this.getResources();
		
		if(isLandscapeOrientation())
		{
			Log.d("TAG", "LANDSCAPE");
			setContentView(R.layout.activity_remote_camera_controller_land);
		}
		else
		{
			setContentView(R.layout.activity_remote_camera_controller);
		}
		
		Log.d("TAG", "onCreate()");
		
		_thisApp = this;
		
		_cameraFeedWebView = (WebView)this.findViewById(R.id.camera_view_01);
		_socketEditText = (EditText)this.findViewById(R.id.server_ip_edit_text);
		_startButton = (Button)this.findViewById(R.id.start_button);
		_startButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					startServer();
				}
			}
		);
		
		_stopButton = (Button)this.findViewById(R.id.stop_button);
		_stopButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					stopServer();
				}
			}
		);
		
		_saveButton = (Button)this.findViewById(R.id.save_button);
		_saveButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(_cameraRunning)
					{
						saveImage();
						Toast.makeText(_thisApp.getApplicationContext(), "Image saved in gallery", Toast.LENGTH_LONG).show();
					}
					else
					{
						Toast.makeText(_thisApp.getApplicationContext(), "Cannot save image when server is not running", Toast.LENGTH_LONG).show();
					}
				}
			}
		);
		
		_alarmButton = (Button)this.findViewById(R.id.alarm_button);
		_alarmButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(_cameraRunning)
					{
						startCommandThread("alarm");
					}
					else
					{
						Toast.makeText(_thisApp.getApplicationContext(), "Cannot sound alarm when server is not running", Toast.LENGTH_LONG).show();
					}
				}
			}		
		);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.remote_camera_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.view_images)
		{
			blankUrl();
			
			_thisApp.startActivityForResult(new Intent(_thisApp, SavedImageViewerActivity.class), VIEW_SAVED_IMAGES_REQ);
			
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int reqCode, int resCode, Intent data)
	{
		super.onActivityResult(reqCode, resCode, data);
		
		if(reqCode == VIEW_SAVED_IMAGES_REQ)
		{
			Log.d("TAG", "onActivtyResult()");
			loadUrl();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{	
		super.onSaveInstanceState(outState);
		
		blankUrl();
		
		CharSequence ip = _socketEditText.getText();
		outState.putCharSequence("ipAddress", ip);
		outState.putBoolean("cameraRunning", _cameraRunning);
		
		Log.d("TAG", "onSave IP: " + ip);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState)
	{	
		Log.d("TAG", "onRestore");
		
		String ip = savedState.getCharSequence("ipAddress").toString();
		_socketEditText.setText("" + ip);
		
		Log.d("TAG", "IP " + ip);
		
		_cameraRunning = savedState.getBoolean("cameraRunning");
		
		if(_cameraRunning)
		{
			Log.d("TAG", "load URL");
			loadUrl();
		}
	}
	
	private void blankUrl()
	{
		_thisApp.runOnUiThread(new Runnable()
			{
				@Override
				public void run() 
				{
					_cameraFeedWebView.loadUrl("about:blank");
				}
			}	
		);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if(_cameraRunning)
		{
			blankUrl();
		}
		Log.d("TAG", "onPause()");
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(_cameraRunning)
		{
			loadUrl();
		}
		Log.d("TAG", "onResume()");
	}
	
	private void loadUrl()
	{
		_thisApp.runOnUiThread(new Runnable()
			{
				@Override
				public void run() 
				{
					_cameraFeedWebView.loadUrl("http://" + getIP() + ":8080/index.html");
				}
			}	
		);
	}
	
	private void saveImage()
	{
		_saveThread = new Thread()
		{
			@Override
			public void run()
			{
				ImageSaver.saveImage(_cameraFeedWebView, _thisApp.getApplicationContext());
			}
		};
		
		_saveThread.start();
	}
	
	private void startServer()
	{
		if(_cameraRunning == false)
		{
			_socketEditText.setEnabled(false);
			_cameraRunning = true;
			startCommandThread("start");
		}
	}
	
	private void stopServer()
	{		
		if(_cameraRunning == true)
		{
			_socketEditText.setEnabled(true);
			_cameraRunning = false;
			startCommandThread("stop");
		}
	}
	
	private String getIP()
	{
		return _socketEditText.getText().toString();
	}
	
	private void startCommandThread(final String command)
	{
		_commandThread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{	
					InetAddress serverAddress = InetAddress.getByName(getIP());
					_socket = new Socket(serverAddress, PORT);
					
					OutputStream out = _socket.getOutputStream();
					PrintWriter output = new PrintWriter(out);
					output.println(command);
					output.flush();
					
					_socket.close();
					
					if(command == "start")
					{
						loadUrl();
					}
					else if(command == "stop")
					{
						blankUrl();
					}
				}
				catch(UnknownHostException e)
				{
					showToast("Invalid IP Address");
					e.printStackTrace();
					Log.d("TAG", "UNKNOWN HOST ERROR");
				}
				catch(IOException e)
				{
					showToast("IO Error");
					Log.d("TAG", "IO ERROR");
				}
				catch(Exception e)
				{
					showToast("ERROR");
					e.printStackTrace();
					Log.d("TAG", "ERROR");
				}
			}
		};
		
		_commandThread.start();
	}
	
	private void showToast(final String text)
	{
		_thisApp.runOnUiThread(new Runnable()
			{
				@Override
				public void run() 
				{
					Toast.makeText(_thisApp, text, Toast.LENGTH_LONG).show();
				}
			}		
		);
	}
	
	private boolean isLandscapeOrientation()
	{
		return _resources.getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
	}
	
}
