package com.naturecode.mockingbird;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

public class OCRProcess extends Activity{
	protected Button take_photo;
	protected Button translate;
	protected EditText translated_text;
	protected EditText ocr_text;
	protected String folder_path;
	protected TextView extracted_text_label;
	protected TextView translated_text_label;
	private Spinner translated_language_list;
	String lang="eng";
	
	private static final String PICTURE_FOLDER = "Mockingbird";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ocr_process);
		
		ocr_text = (EditText) findViewById(R.id.ocr_text);
		translated_text = (EditText) findViewById(R.id.translated_text);
		take_photo = (Button) findViewById(R.id.take_photo);
		translate = (Button) findViewById(R.id.translate);
		extracted_text_label = (TextView) findViewById(R.id.ocr_text_label);
		translated_text_label = (TextView) findViewById(R.id.translated_text_label);
		
		translated_language_list = (Spinner)findViewById(R.id.translated_lang_spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.translated_language, R.layout.my_spinner_text);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    translated_language_list.setAdapter(adapter);
	    translated_language_list.setSelection(1);
	    
		translate.setOnClickListener(new Button.OnClickListener() { 
			public void onClick (View v){ 
//				Log.i("OCRProcess", "Start Translating");
				startTranslating();
			}
		});	
		
		startOCRProcess();
	}
		
	protected void startOCRProcess(){
		String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/"+PICTURE_FOLDER;
		String keep_path = ((GlobalVariables) this.getApplication()).getKeep_path();
		
		Intent data = getIntent();
		String language="English";
		if(data!=null){
			Bundle extras = data.getExtras();
			if (extras != null) {
				language = extras.getString("extracted_lang");
			}
		}
		if(language != null){
			if(language.equalsIgnoreCase("English"))
				lang="eng";
			else if(language.equalsIgnoreCase("French"))
				lang="fra";
			else if(language.equalsIgnoreCase("German"))
				lang="deu-frak";
			else if(language.equalsIgnoreCase("Italian"))
				lang="ita";
			else if(language.equalsIgnoreCase("Spanish"))
				lang="spa";
			else
				lang="vie";
		}
//		Log.i( "extracted_lang: ", lang);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;		
		Bitmap bitmap = BitmapFactory.decodeFile(keep_path, options);
		bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
		
//		Log.i( "OCRProcess", "Before baseApi");
		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

//		You now have the text in recognizedText var, you can do anything with it.
//		We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
//		so that garbage doesn't make it to the display.
//		Log.i( "OCRProcess", "OCRED TEXT: "+ recognizedText);
		if (lang.equalsIgnoreCase("eng")) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		recognizedText = recognizedText.trim();

		extracted_text_label.setText("Extracted text: ("+language+")");
		if ( recognizedText.length() != 0 ) {
			ocr_text.setText(recognizedText);
		}
	}
	
	protected void startTranslating(){
		String unicode_string = ocr_text.getText().toString();
		unicode_string = unicode_string.replaceAll(" ", "%20");
		String source_lang_short="en";
		String translated_lang_short = (String)translated_language_list.getSelectedItem();
//		if(lang.equalsIgnoreCase("eng")){
//			
//		}
		
		if(lang.equalsIgnoreCase("eng"))
			source_lang_short="en";
		else if(lang.equalsIgnoreCase("fra"))
			source_lang_short="fr";
		else if(lang.equalsIgnoreCase("deu-frak"))
			source_lang_short="de";
		else if(lang.equalsIgnoreCase("ita"))
			source_lang_short="it";
		else if(lang.equalsIgnoreCase("spa"))
			source_lang_short="es";
		else
			source_lang_short="vi";
		
		if(translated_lang_short !=null){
			if(translated_lang_short.equalsIgnoreCase("English"))
				translated_lang_short="en";
			else if(translated_lang_short.equalsIgnoreCase("French"))
				translated_lang_short="fr";
			else if(translated_lang_short.equalsIgnoreCase("German"))
				translated_lang_short="de";
			else if(translated_lang_short.equalsIgnoreCase("Italian"))
				translated_lang_short="it";
			else if(translated_lang_short.equalsIgnoreCase("Spanish"))
				translated_lang_short="es";
			else
				translated_lang_short="vi";
		}
		else{
			translated_lang_short="vi";
		}
		Log.i( "unicode_string: ", unicode_string);
			
		String URL = "http://mymemory.translated.net/api/get?q="+unicode_string+"&langpair="+source_lang_short+"%7C"+translated_lang_short;
		DownloadWebPageTask task = new DownloadWebPageTask();
		task.execute(new String[] {URL});
	}
	
	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}

				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
			return response;
		}

		@Override
		protected void onPostExecute(String result) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				JSONObject translatedText = jsonObject.getJSONObject("responseData");
				String translated = translatedText.get("translatedText").toString();
				translated_text.setText(translated);
//				Log.i("OCRProcess", "Translated text: "+translated);
			} catch (JSONException e) {
//				e.printStackTrace();
			}
		}
	}
}