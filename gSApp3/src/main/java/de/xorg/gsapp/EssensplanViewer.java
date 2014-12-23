package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import de.xorg.gsapp.R;

public class EssensplanViewer extends Activity { 
	
	
	//Variable für URL
	String URI;
	
	//Variable für Speiseplan Diese/nächste Woche
	String VIEW = "A";
	
	//Variable, ob mit dem Internet verbunden ist
	private boolean isConnected = true;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
		if(BeanUI) {
			setTheme(R.style.AppThemeBlack);
		} else {
			setTheme(R.style.AppTheme);
		}
		setContentView(R.layout.webviewer);
		
		Util.setTranscluent(this, BeanUI);
		
		//Variablen
		WebView Speisen = (WebView) findViewById(R.id.WebView);
		RelativeLayout widerrist = (RelativeLayout) findViewById(R.id.withers);
		final Activity MyActivity = this;
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		
		//Titel einstellen
		setTitle("Speiseplan (Diese Woche)");
		
		if(BeanUI) {
			widerrist.setBackgroundColor(Color.BLACK);
		} else {
			widerrist.setBackgroundColor(Color.parseColor("#fed21b"));
		}
 		
		Speisen.setWebViewClient(new MyWebViewClient() );
		
		Speisen.getSettings().setJavaScriptEnabled(false);
		Speisen.getSettings().setBuiltInZoomControls(true);
		
	    isConnected = cd.isConnectingToInternet();
		
		URI = "http://www.gymnasium-sonneberg.de/Informationen/Speiseplan/spA.html";
		
		getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		Speisen.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)   
            {
            	String realtitle = MyActivity.getTitle().toString();
                MyActivity.setTitle(realtitle);
                MyActivity.setProgress(progress * 100);

                if(progress == 100)
                    MyActivity.setTitle(realtitle);
                }
            });
		
		if(!(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "").equals(""))) {
			try {
				Speisen.setInitialScale(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "")) - 50);
			} catch(NumberFormatException ex) {
				Speisen.setInitialScale(100);
			} catch(Exception ex) {
				Speisen.setInitialScale(100);
			}
		} else {
			Speisen.setInitialScale(100);
		}
		
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			//Funktioniert erst ab Honeycomb (3.X)
			Speisen.getSettings().setDisplayZoomControls(false);
		}

	    Speisen.loadUrl(URI);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		getMenuInflater().inflate(R.menu.essen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
		switch (item.getItemId()) {
	    case R.id.refresh:
	         openUrl(URI);
	         return true;
	    case R.id.woche:
	    	 if(VIEW.equalsIgnoreCase("A")) {
	    			String URL;
	    			URL = "http://www.gymnasium-sonneberg.de/Informationen/Speiseplan/spA.html";
	    			URI = "http://www.gymnasium-sonneberg.de/Informationen/Speiseplan/spA.html";
	    		 VIEW = "B";
	    		 setTitle("Speiseplan (Nächste Woche)");
	    		 openUrl(URL);
	    	 } else {
	    		 String URL;
	    		 URL = "http://www.gymnasium-sonneberg.de/Informationen/Speiseplan/spB.html";
	    		 URI = "http://www.gymnasium-sonneberg.de/Informationen/Speiseplan/spB.html";
	    		 VIEW = "A";
	    		 setTitle("Speiseplan (Diese Woche)");
	    		 openUrl(URL);
	    	 }
	    default:
	         return super.onOptionsItemSelected(item);
	    }
	}
	
	public void openUrl(String url) {
		WebView Speisen = (WebView) findViewById(R.id.WebView);
		Speisen.loadUrl(url);
	}
	
    private class MyWebViewClient extends WebViewClient {
    	
    	final String OFFLINE = "<html><head></head><body bgcolor='#fed21b'><h2>Sie sind offline</h2><br><b>Es ist keine Internetverbindung verf&uuml;gbar! :(</b></body></html>";
    	final String TIMEOUT = "<html><head></head><body bgcolor='#fed21b'><h2>TIMEOUT</h2><br><b>Der Server braucht zu lange,<br>um eine Antwort zu senden :(<br><br></b></body></html>";
    	final String GENERIC = "<html><head></head><body bgcolor='#fed21b'><h2>Fehler</h2><br><b>Bei der Verbindung zum Server<br>ist ein allgemeiner Fehler aufgetr. :(<br><br></b></body></html>";
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	if (isConnected) {
        		view.loadUrl(url);
                return true;
            } else {
                view.loadData(OFFLINE, "text/html", "utf-8");
                return true;
            }
        }
        
        @Override
        public void onReceivedError (WebView view, int errorCode, 
            String description, String failingUrl) {
            if (errorCode == ERROR_TIMEOUT) {
            	view.stopLoading();
                view.loadData(TIMEOUT, "text/html", "utf-8");
            } else {
            	view.stopLoading();
                view.loadData(GENERIC, "text/html", "utf-8");
            }
        }
    }

}

