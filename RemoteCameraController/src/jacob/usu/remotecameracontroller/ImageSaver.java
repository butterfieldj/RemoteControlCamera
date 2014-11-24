package jacob.usu.remotecameracontroller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;

public class ImageSaver
{
	private static String _directoryName = "/RemoteCamera";
	
	public static void saveImage(WebView webView, Context context)
	{
		Bitmap bmp = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Config.ARGB_8888);
		
		webView.draw(new Canvas(bmp));
		
		saveAndAddToGallery(bmp, context);
	}
	
	private static void saveAndAddToGallery(Bitmap bitmap, Context context)
	{
		String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
		File imageDirectory = new File(rootDir + _directoryName);
		
		if(imageDirectory.exists() == false)
		{
			if(imageDirectory.mkdirs() == false)
			{
				Log.v("TAG", "Failed to make directory");
				return;
			}
		}
		
		String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		File imageFile = new File(imageDirectory.getPath() + File.separator + "IMG_" + name + ".jpg");
		
		if(imageFile.exists())
		{
			imageFile.delete();
		}
		
		try
		{
			FileOutputStream output = new FileOutputStream(imageFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
			output.flush();
			output.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		scanMedia(context, imageFile);
	}
	
	private static void scanMedia(Context context, File imageFile)
	{
		MediaScannerConnection.scanFile(context, new String[] { imageFile.toString() }, null, new MediaScannerConnection.OnScanCompletedListener() 
			{
		            @Override    
					public void onScanCompleted(String path, Uri uri) 
		            {
		                    Log.d("ExternalStorage", "Scanned: " + path + ":");
		                    Log.d("ExternalStorage", "Uri: " + uri);
		            }
		    }
		);
	}
	
}
