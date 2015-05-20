package com.naturecode.mockingbird;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class PictureTaken extends Activity{
	protected Button take_photo;
	protected Button translate;
	protected ImageView source_image;
	protected TextView initial_field;
	protected String folder_path;
	protected boolean _taken;
	protected String selected_lang;
	private Spinner language_list;
	private ProgressDialog mProgressDialog;

	
	private static final String PICTURE_FOLDER = "Mockingbird";
	private static final String download_link = "http://www.atfurniture.biz/tessdata/";
	protected static final String PHOTO_TAKEN = "photo_taken";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		source_image = (ImageView) findViewById(R.id.image);
		initial_field = (TextView) findViewById(R.id.field);
		take_photo = (Button) findViewById(R.id.take_photo);
		translate = (Button) findViewById(R.id.ocr);
		folder_path = getFilename();
		
		language_list = (Spinner)findViewById(R.id.lang_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.translated_language, R.layout.my_spinner_text);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    language_list.setAdapter(adapter);
	    language_list.setSelection(0);
	    
		take_photo.setOnClickListener(new Button.OnClickListener() { 
			public void onClick (View v){ 
				Log.i("PictureTaken", "Taking Picture");
				startCameraActivity();
			}
		});	
		
		translate.setOnClickListener(new Button.OnClickListener() { 
			public void onClick (View v){ 
				Log.i("PictureTaken", "Start OCR");
				startOCRProcess();
			}
		});	
		
		if(_taken){
			translate.setEnabled(true);
		}
		
//		mProgressDialog = new ProgressDialog(PictureTaken.this);
//		mProgressDialog.setMessage("Loading...It might take several minutes, for the first time only");
//		mProgressDialog.setCancelable(false);
//		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		mProgressDialog.setProgress(100);
//		mProgressDialog.setMax(100);
		
		mProgressDialog = new ProgressDialog(PictureTaken.this);
		mProgressDialog.setMessage("Loading...It might take several minutes, for the first time only");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		
		setupLanguageFolder();
	}
	
	protected void startOCRProcess(){
		String extracted_lang = (String)language_list.getSelectedItem();
		Intent myIntent = new Intent(PictureTaken.this, OCRProcess.class);
		myIntent.putExtra("extracted_lang", extracted_lang);
		startActivity(myIntent);
	}
	
	private String getFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, PICTURE_FOLDER);

		if(!file.exists()){
			file.mkdirs();
		}
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		String default_filename = month+""+day+""+year+""+hour+""+min+""+sec;
		String path = file.getAbsolutePath() + "/" + default_filename+".jpg";
		return path;
	}
	
	private void setupLanguageFolder(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		String datapath = filepath+"/"+PICTURE_FOLDER+"/tessdata";
		File tessdata = new File(datapath);
		String[] lang_list = getResources().getStringArray(R.array.language_list);
//		AssetManager assetManager = getAssets();
		if(!tessdata.exists()){
			tessdata.mkdirs();
			DownloadWebPageTask task = new DownloadWebPageTask(datapath, lang_list);
			task.execute(new String[] {download_link});
		}
		else{
			File[] lang_files = tessdata.listFiles();
			if(lang_files==null || lang_files.length==0){
				DownloadWebPageTask task = new DownloadWebPageTask(datapath, lang_list);
				task.execute(new String[] {download_link});
			}
//			else{
//				List<String> langs = Arrays.asList(lang_list);
//				for(File itr:lang_files){
//					String lang_name = itr.getName().substring(0, itr.getName().lastIndexOf('.'));
//					if(!langs.contains(lang_name)){
//						downloadLanguageFile(datapath, lang_name);
//					}
//				}
//			}
		}
	}
	
//	private void downloadLanguageFile(String dir, String file_name){
//		try {
//			URL link = new URL(download_link+file_name+".traineddata");
//			File file = new File(dir, file_name+".traineddata");
//			URLConnection ucon = link.openConnection();
//
//			InputStream is = ucon.getInputStream();
//			BufferedInputStream bis = new BufferedInputStream(is);
//			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
//			int current = 0;
//			while ((current = bis.read()) != -1) {
//				baf.append((byte) current);
//			}
//
//			FileOutputStream fos = new FileOutputStream(file);
//			fos.write(baf.toByteArray());
//			fos.flush();
//			fos.close();
//		} catch (IOException e) {
//			Log.d("downloadLanguageFile", "Error: " + e);
//		}
//	}

	protected void startCameraActivity(){
//		Log.i("PictureTaken", "startCameraActivity()");
		File file = new File(folder_path);
		((GlobalVariables)getApplication()).setKeep_path(folder_path);
		
		Uri outputFileUri = Uri.fromFile(file);
		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
//		Log.i( "PictureTaken", "resultCode: " + resultCode );
		switch( resultCode ){
		case 0:
//			Log.i( "PictureTaken", "User cancelled" );
			break;
		case -1:
			onPhotoTaken();
			break;
		}
	}

	protected void onPhotoTaken(){
		_taken = true;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;
	
		String keep_path = ((GlobalVariables) this.getApplication()).getKeep_path();
		Bitmap bitmap = BitmapFactory.decodeFile(keep_path, options);
		source_image.setImageBitmap(bitmap);
		initial_field.setVisibility(View.GONE);
		
		translate.setEnabled(true);
	}

	@Override 
	protected void onRestoreInstanceState(Bundle savedInstanceState){
//		Log.i( "PictureTaken", "onRestoreInstanceState()");
		if( savedInstanceState.getBoolean(PictureTaken.PHOTO_TAKEN)) {
			onPhotoTaken();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean( PictureTaken.PHOTO_TAKEN, _taken);
	}
	
	private class DownloadWebPageTask extends AsyncTask<String, Integer, String> {		
		private String[] lang_list;
		private String datapath;

		public DownloadWebPageTask(String datapath, String[]lang_list){
			this.datapath = datapath;
			this.lang_list = lang_list;
		}
		
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
	        mProgressDialog.show();
        }
		@Override
		protected String doInBackground(String... urls) {
			String result="";
			int count=0;
			try {
				for(String lang:lang_list){
					publishProgress((int) (count * 100 / lang_list.length));
					URL link = new URL(download_link+lang+".traineddata");
					File file = new File(datapath, lang+".traineddata");
					URLConnection ucon = link.openConnection();
	
					InputStream is = ucon.getInputStream();
					BufferedInputStream bis = new BufferedInputStream(is);
					ByteArrayBuffer baf = new ByteArrayBuffer(5000);
					int current = 0;
					while ((current = bis.read()) != -1) {
						baf.append((byte) current);
					}
	
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(baf.toByteArray());
					fos.flush();
					fos.close();
					count++;
				}
			} catch (IOException e) {
				Log.d("downloadLanguageFile", "Error: " + e);
			}
			return result;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			mProgressDialog.setProgress(progress[0]);
	    }


		@Override
		protected void onPostExecute(String result) {
			mProgressDialog.dismiss();
		}
	}
}