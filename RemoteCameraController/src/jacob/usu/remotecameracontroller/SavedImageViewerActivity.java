package jacob.usu.remotecameracontroller;

import java.io.File;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SavedImageViewerActivity extends Activity
{
	private ImageView _savedImageView = null;
	
	private Button _nextButton = null;
	private Button _previousButton = null;
	
	private File[] _imageFiles = null;
	private int _imageNumber = 0;
	
	private Resources _resources = null;
	
	private static final String _directory = "/RemoteCamera";
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		_resources = this.getResources();
		
		if(isLandscapeOrientation())
		{
			setContentView(R.layout.activity_saved_image_viewer_land);
		}
		else
		{
			setContentView(R.layout.activity_saved_images_viewer);
		}
		
		_savedImageView = (ImageView)this.findViewById(R.id.saved_image_view);
		
		loadImageFiles();
		
		setImageView();
		
		_nextButton = (Button)this.findViewById(R.id.next_image_button);
		_nextButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View view) 
				{
					_imageNumber++;
					
					if(_imageNumber >= _imageFiles.length)
					{
						_imageNumber = 0;
					}
					
					setImageView();
				}
			}
		);
		
		_previousButton = (Button)this.findViewById(R.id.previous_image_button);
		_previousButton.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View view) 
				{
					_imageNumber--;
					
					if(_imageNumber < 0)
					{
						_imageNumber = _imageFiles.length - 1;
					}
					
					setImageView();
				}
			}
		);
		
	}
	
	private void loadImageFiles()
	{
		String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + _directory;
		
		File imageDirectory = new File(path);
		
		_imageFiles = imageDirectory.listFiles();
		
		for(File f : _imageFiles)
		{
			Log.d("TAG", "Files: " + f.getPath());
		}
	}
	
	private void setImageView()
	{
		Uri uri = Uri.fromFile(_imageFiles[_imageNumber]);
		
		_savedImageView.setImageURI(uri);
	}
	
	private boolean isLandscapeOrientation()
	{
		return _resources.getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
	}
	
}