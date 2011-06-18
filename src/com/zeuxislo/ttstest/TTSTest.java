package com.zeuxislo.ttstest;

import java.util.ArrayList;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TTSTest extends Activity implements TextToSpeech.OnInitListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
	private TextToSpeech textToSpeech;
	private Object[] language = { 
		"普通話", Locale.CHINA, 
		"粵語", new Locale("zh", "HK"), 
		"英文 US", Locale.US
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Init Text to Speech
		this.textToSpeech = new TextToSpeech(getApplicationContext(), this);

		// Mount widgets
		Button button_speak = (Button)findViewById(R.id.button_speak);
		button_speak.setOnClickListener(this);

		Button button_stop = (Button)findViewById(R.id.button_stop);
		button_stop.setOnClickListener(this);

		SeekBar speak_rate = (SeekBar)findViewById(R.id.speak_rate);
		speak_rate.setProgress(75);
		speak_rate.setOnSeekBarChangeListener(this);

		SeekBar speak_pitch = (SeekBar)findViewById(R.id.speak_pitch);
		speak_pitch.setProgress(100);
		speak_pitch.setOnSeekBarChangeListener(this);

		TextView speak_rate_label = (TextView)findViewById(R.id.speak_rate_label);
		speak_rate_label.setText("音速: " + speak_rate.getProgress() / 100.0F);

		TextView speak_pitch_label = (TextView)findViewById(R.id.speak_pitch_label);
		speak_pitch_label.setText("音調: " + speak_pitch.getProgress() / 100.0F);

		// Setup languages
		String[] language_names = new String[this.language.length / 2];
		for (int i = 0; i < language_names.length; i++) {
			language_names[i] = this.language[(i * 2)].toString();
		}
		
		ArrayAdapter<String> languageNameArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, language_names);
		languageNameArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner speak_language = (Spinner)findViewById(R.id.speak_language);
		speak_language.setAdapter(languageNameArrayAdapter);

		// Setup articles
		ArrayList<ArticleEntry> articleList = new ArrayList<ArticleEntry>();
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = documentBuilder.parse(getResources().openRawResource(R.raw.article));

			document.getDocumentElement().normalize();

			NodeList artileList = document.getElementsByTagName("article");

			for (int i = 0; i < artileList.getLength(); i++) {
				Element element = (Element) artileList.item(i);
				
				NodeList nameList = element.getElementsByTagName("name");
				NodeList contentList = element.getElementsByTagName("content");

				articleList.add(new ArticleEntry(nameList.item(0).getTextContent(), contentList.item(0).getTextContent()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String[] article_names = new String[articleList.size()];
		for (int i = 0; i < article_names.length; i++) {
			article_names[i] = ((ArticleEntry)articleList.get(i)).name;
		}

		// Display first article
		TextView textView = (TextView)findViewById(R.id.speak_area);
		textView.setText(((ArticleEntry)articleList.get(0)).content);

		// Contine setup articles
		ArrayAdapter<String> articleNamesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, article_names);
		articleNamesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner speak_article = (Spinner)findViewById(R.id.speak_article);
		speak_article.setAdapter(articleNamesArrayAdapter);
		speak_article.setOnItemSelectedListener(new SpeakArticleOnItemSelectedListener(textView, articleList));
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, 1, 0, "Clean Text").setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, 2, 0, "About Me").setIcon(android.R.drawable.ic_menu_info_details);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case 1:
				TextView textView = (TextView)findViewById(R.id.speak_area);
				textView.setText("");
				break;
			case 2:
				Toast.makeText(this, "Author: Zeuxis.Lo", 1).show();
				break;
  		}
		return true;
	}

	protected void onDestory() {
		super.onDestroy();
		this.textToSpeech.shutdown();
	}

	public void onInit(int status) {
		if (status == 0) {
			String[] engines = { "com.svox.pico", "com.svox.classic" };

			for (int i = 0; i < engines.length; i++) {
				if (!Utility.isPackageAvailable(this, engines[i])) {
					Toast.makeText(getApplicationContext(), "Engine " + engines[i] + " not Loaded", 1).show();
				} else {
					Toast.makeText(getApplicationContext(), "Engine " + engines[i] + " Loaded", 0).show();
				}
			}
		}else{
			System.out.println("Oops!!!");
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_speak:
				Spinner speak_language = (Spinner)findViewById(R.id.speak_language);

				TextView textView = (TextView)findViewById(R.id.speak_area);
				SeekBar speak_rate = (SeekBar)findViewById(R.id.speak_rate);
				SeekBar speak_pitch = (SeekBar)findViewById(R.id.speak_pitch);

				Locale locale = (Locale)this.language[(int)(speak_language.getSelectedItemId() * 2L + 1L)];

				if (locale == Locale.US) {
					Toast.makeText(this, "Use Build-In Engine", 0).show();
					this.textToSpeech.setEngineByPackageName("com.svox.pico");
				} else {
					Toast.makeText(this, "Use Svox Classic Engine", 0).show();
					this.textToSpeech.setEngineByPackageName("com.svox.classic");
				}

				this.textToSpeech.setLanguage(locale);
				this.textToSpeech.setSpeechRate(speak_rate.getProgress() / 100.0F);
				this.textToSpeech.setPitch(speak_pitch.getProgress() / 100.0F);
				this.textToSpeech.speak(textView.getText().toString(), 0, null);
				break;
			case R.id.button_stop:
				this.textToSpeech.stop();
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		TextView textView;
		switch (seekBar.getId()) {
			case R.id.speak_rate:
				textView = (TextView)findViewById(R.id.speak_rate_label);
				textView.setText("音速: " + String.valueOf(progress / 100.0F));
				break;
			case R.id.speak_pitch:
				textView = (TextView)findViewById(R.id.speak_pitch_label);
				textView.setText("音調: " + String.valueOf(progress / 100.0F));
				break;
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}
}

class SpeakArticleOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
	private TextView textView;
	private ArrayList<ArticleEntry> articleList;

	public SpeakArticleOnItemSelectedListener(TextView textView, ArrayList<ArticleEntry> articleList) {
		this.textView = textView;
		this.articleList = articleList;
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		this.textView.setText(((ArticleEntry)this.articleList.get(position)).content);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}
}

class Utility {
	public static boolean isPackageAvailable(Context context, String packageName) {
		try {
			String versionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
			return true; 
		} catch (Exception e) {
			return false;
 	   }
	}
}

class ArticleEntry {
	public String name;
	public String content;

	public ArticleEntry(String name, String content) {
		this.name = name;
		this.content = content.trim().replaceAll("\t", "");
	}
}