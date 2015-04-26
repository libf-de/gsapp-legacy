package de.xorg.gsapp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@ReportsCrashes(formKey = "", // will not be used
formUri = "http://xorg.ga/bugcentral/greport.php",
mode = ReportingInteractionMode.DIALOG,
resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
resDialogText = R.string.crash_dialog_text,
resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class MainActivity extends ActionBarActivity {
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
	public final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";

	@SuppressWarnings("unused")
	private Context c;
	public static MainActivity IBLAH;
	@SuppressWarnings("unused")
	private boolean isConnected = false;
	private CheckService alarm;
	private Boolean isNexus = false;

    private GALog l;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
		//App-Thema einstellen
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
        Util.setThemeUI(this);
		setContentView(R.layout.activity_main);

        l = new GALog(this);
	
		IBLAH = this;
		
		//Tablet-Oberfläche einstellen
        Util.setOrientation(this);

        if(BeanUI) {
            ImageView p = (ImageView) findViewById(R.id.imageView1);
            p.setImageDrawable(getResources().getDrawable(R.drawable.pistorblack));
        }
		
		alarm = new CheckService();
		
		Boolean VPChecker = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rideCheck", false);
		if(VPChecker) {
			startRepeatingTimer();
		} else {
			cancelRepeatingTimer();
		}
		
		c = this;
		
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		 
		Boolean isInternetPresent = cd.isConnectingToInternet();
		
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("devMode", false)) {
			isNexus = true;
		} else {
			isNexus = false;
		} 
		
	    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("frun", true)) {
	    	if(isInternetPresent) {
	    		ZeigeAGB();
	    	} else {
	    		AlertDialog.Builder alert = new AlertDialog.Builder(this);

				alert.setTitle("Fehler");
				alert.setMessage("Beim ersten Start der App ist eine Internetverbindung erforderlich!\nBitte stellen sie eine Verbindung her\nund öffnen sie die App dann erneut.");
				
				alert.setCancelable(false);

				alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
					  finish();
				  }
				});

				alert.show();
	    	}
		} else {
			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("frun2", true)) {
			    shouldEnableVPC();	
		    } else {
		    	if(!(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("anl", false))) {
			    	ZeigeANLEITUNG();
			    }
		    }
		}
	    
	    if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("anl", false)) {
	    	l.debug("MainAct: Hole Server-Nachrichten");
	    	new ServerMessageHandler(this).execute("");
	    } else {
	    }
	    
		Calendar c = Calendar.getInstance(); 
	    if(c.get(Calendar.DAY_OF_MONTH) == 31 && c.get(Calendar.MONTH) == Calendar.OCTOBER) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	ImageView imgv = new ImageView(this);
	    	imgv.setImageResource(R.drawable.halloween);
	        builder.setTitle("Happy Halloween!");
	        builder.setView(imgv);
	        builder.setNeutralButton("Süßes oder Saures!", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                dialog.dismiss();
	            }
	        });
	        builder.create();
	        builder.show();
	    } else if(c.get(Calendar.DAY_OF_MONTH) == 24 && c.get(Calendar.MONTH) == Calendar.DECEMBER) {
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	ImageView imgv = new ImageView(this);
	    	imgv.setImageResource(R.drawable.christmas);
	        builder.setTitle("Fröhliche Weihnachten!");
	        builder.setView(imgv);
	        builder.setNeutralButton("Ho Ho Ho!", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                dialog.dismiss();
	            }
	        });
	        builder.create();
	        builder.show();
	    } else {
	    	
	    }
	    
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Boolean TabletUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("tablet", false);
		if(isNexus) {
			getMenuInflater().inflate(R.menu.maind, menu);
		} else {
			if(TabletUI) {
				getMenuInflater().inflate(R.menu.maintab, menu);
			} else {
				getMenuInflater().inflate(R.menu.main, menu);
			}
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Boolean TabletUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("tablet", false);
		if(isNexus) {
			switch (item.getItemId()) {
		    case R.id.action_settings:
		    	 Intent intent = new Intent(this, Settings.class);
			     startActivity(intent);
		         return true;
		    case R.id.action_about:
		    	 Intent intent2 = new Intent(this, About.class);
			     startActivity(intent2);
		         return true;
		    case R.id.action_share:
		    	 shareAPP();
		    	 return true;
		    case R.id.action_fcheck:
		    	 ForceOT();
		    	 Toast.makeText(this, "Erzwinge Überprüfung..", Toast.LENGTH_SHORT).show();
		    	 return true;
            case R.id.action_devsetting:
                 Intent intentes = new Intent(this, DeveloperSettings.class);
                 startActivity(intentes);
                 return true;
		    default:
		         return super.onOptionsItemSelected(item);
		    }
		} else {
			if(TabletUI) {
				switch (item.getItemId()) {
			    case R.id.action_settings:
			    	 Intent intent = new Intent(this, Settings.class);
				     startActivity(intent);
			         return true;
			    case R.id.action_about:
			    	 Intent intent2 = new Intent(this, About.class);
				     startActivity(intent2);
			         return true;
			    case R.id.action_share:
			    	 shareAPP();
			    	 return true;
			    case R.id.kontakttab:
			    	 Intent intentER = new Intent(this, KontaktAnzeige.class);
				     startActivity(intentER);
			    	 return true;
			    default:
			         return super.onOptionsItemSelected(item);
				}
		    } else {
		    	switch (item.getItemId()) {
			    case R.id.action_settings:
			    	 Intent intent = new Intent(this, Settings.class);
				     startActivity(intent);
			         return true;
			    case R.id.action_about:
			    	 Intent intent2 = new Intent(this, About.class);
				     startActivity(intent2);
			         return true;
			    case R.id.action_share:
			    	 shareAPP();
			    	 return true;
			    default:
			         return super.onOptionsItemSelected(item);
		    	}
		    }
		}
	}
	
	@Override
	public void onBackPressed() {
		System.exit(0);
        
        //super.onBackPressed();
        return;
	}
	
	public void ForceOT() {
		Context context = this.getApplicationContext();
	     if(alarm != null){
	        	 alarm.ForceCheck(context);
	     }else{
	    	 Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: VPC-FCN)", Toast.LENGTH_SHORT).show();
	     }
	}
	
	public void checkNetwork(final boolean isRetry) {
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		 
		Boolean isInternetPresent = cd.isConnectingToInternet();
		
		if(!(isInternetPresent)) {
			if(isRetry) {
				Toast.makeText(this, "Es ist keine aktive Netzwerkverbindung verfügbar!", Toast.LENGTH_SHORT).show();
			}
			Button VP = (Button) findViewById(R.id.VertretungsplanButton);
			Button TM = (Button) findViewById(R.id.TerminButton);
			Button AK = (Button) findViewById(R.id.AktuellesButton);
			Button EP = (Button) findViewById(R.id.EssensplanButton);
			Button EB = (Button) findViewById(R.id.EssensbestellungButton);
			VP.setEnabled(false);
			TM.setEnabled(false);
			AK.setEnabled(false);
			EP.setEnabled(false);
			EB.setEnabled(false);
			TextView INFO = (TextView) findViewById(R.id.InfoAdapter);
			INFO.setText("Es ist keine Internetverbindung verfügbar! Einige Features sind nicht verfügbar!\n- Hier Tippen um es noch einmal zu versuchen -");
			INFO.setTextColor(Color.RED);
			INFO.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Toast.makeText(MainActivity.this, "Überprüfe Netzwerkverbindung...", Toast.LENGTH_SHORT).show();
					checkNetwork(true);
				}
				
			});
		} else {
			if(isRetry) {
				Toast.makeText(this, "Eine aktive Netzwerkverbindung wurde erkannt!", Toast.LENGTH_SHORT).show();
			}
			Button VP = (Button) findViewById(R.id.VertretungsplanButton);
			Button TM = (Button) findViewById(R.id.TerminButton);
			Button AK = (Button) findViewById(R.id.AktuellesButton);
			Button EP = (Button) findViewById(R.id.EssensplanButton);
			Button EB = (Button) findViewById(R.id.EssensbestellungButton);
			VP.setEnabled(true);
			TM.setEnabled(true);
			AK.setEnabled(true);
			EP.setEnabled(true);
			EB.setEnabled(true);
			TextView INFO = (TextView) findViewById(R.id.InfoAdapter);
			INFO.setText("");
			INFO.setTextColor(Color.GREEN);
			INFO.setOnClickListener(null);
		}
	}
	
	public void TabletEinstellen() {
				/*DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				int width=dm.widthPixels;
				int height=dm.heightPixels;
				int dens=dm.densityDpi;
				double wi=(double)width/(double)dens;
				double hi=(double)height/(double)dens;
				double x = Math.pow(wi,2);
				double y = Math.pow(hi,2);
				double si = Math.sqrt(x+y);
				si = Math.round(si * 100);
				si = si/100;
			    
			    if(si > 6.60) {
			    	Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
					editor.putBoolean("tablet", true);
					editor.commit();
			    } else {
			    	Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
					editor.putBoolean("tablet", false);
					editor.commit();
			    }*/
	}
	
	public void ZeigeAGB() {
		AlertDialog.Builder AGBDialog = new AlertDialog.Builder(this);
		
		AGBDialog.setTitle("AGBs");
		AGBDialog.setMessage("Um Konflikte zu vermeiden, lesen sie sich bitte folgende Informationen durch:\n\n\n-- I. Kosten --\nDie App und der Service den die App bereitstellt sind komplett kostenlos. Da die App jedoch eine Internetverbindung verwendet, kann es sein, das bei der Übertragung von Informationen Kosten entstehen. Dies ist je nach Tarif unterschiedlich. Mehr Informationen bekommen sie bei ihrem Anbieter.\n\n--II. Richtigkeit der Informationen --\nAlle Angaben sind ohne Gewähr. Änderungen behalten sich der Autor der App (»ich«), der Webmaster der Schul-Homepage und die Schulleitung vor!\n\n-- III. Sicherheit und Datenschutz --\nIch nehme Datenschutz sehr ernst, deswegen sind App und Webseite komplett werbefrei. Das einzige was gespeichert wird, sind die öffentliche IP-Adresse ihres Gerätes (das macht der Webserver automatisch) und, wenn Feedback/Fehler gesendet werden, die angegebene E-Mail und eine zufällige Nummer zur Wiedererkennung. Wenn sie für die Essensbestellung Benutzer und Passwort speichern, werden diese verschlüsselt auf ihrem Gerät gespeichert. Ich übernehme keinerlei Haftung bei Diebstahl dieser Daten, auch wenn diese verschlüsselt sind. Auch Unbefugte werden automatisch angemeldet, und können so alles in ihrem Bestellungsaccount tun. Mehr zur Sicherheit finden sie in den Online-AGBs. Ich gebe KEINE dieser Daten an Dritte weiter.\n\nWenn sie die AGBs ablehnen, wird die App geschlossen.");
		//AGBDialog.setMessage("=== Nutzungsbedingungen ===\nGSApp 3.99 Gino (RC)\nRelease Candidate 3\n\nIn Diesem Release:\n- Vertretungsplan-Verarbeitung vom Geraet\n- Moderne Oberfläche »Cards UI« (Konzept)\n- Vertretungen filterbar\n- allgemein alles :)\n\nNicht in diesem Release:\n- Bei Blackbean sind die Karten noch hell\n- Bei BlackBean wird das Logo nicht geändert\n\nWeitere Notizen:\n- Der Vertretungsplan ist ein Snapshot und entspricht nicht dem echten (wegen Ferien)!");
		
		AGBDialog.setCancelable(false);
		
		AGBDialog.setPositiveButton("Akzeptieren", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			      editor.putString("id", randomZ());
			      editor.commit();
			      TabletEinstellen();
			      Editor editor2 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			      editor2.putBoolean("frun", false);
			      editor2.commit();
			      restartApp();
			}
		});
		
		AGBDialog.setNeutralButton("Online-AGBs zeigen", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				Intent intent = new Intent(MainActivity.this, InternetViewer.class);
				intent.putExtra(EXTRA_URL, "http://gsapp.xorg.ga/agb.php?v=gino");
				intent.putExtra(EXTRA_NAME, "*AGBs");
			    startActivity(intent);		
			}
		});
		
		AGBDialog.setNegativeButton("Ablehnen", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			      Editor editor2 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			      editor2.putBoolean("frun", true);
			      editor2.commit();
				  finish();
			  }
			});
		
		AGBDialog.show();
	}
	
	public void shouldEnableVPC() {
		AlertDialog.Builder VPCDialog = new AlertDialog.Builder(this);
		
		VPCDialog.setTitle("Vertretungsplan-Überprüfung");
		VPCDialog.setMessage("Soll GSApp im Hintergrund automatisch nach einem neuen Vertretungsplan suchen? Wenn diese Funktion aktiviert ist, wird GSApp alle 30 Minuten überprüfen, ob der aktuell verfügbare Vertretungsplan neuer als der zuletzt angesehene Vertretungsplan ist. GSApp läuft hierbei nicht im Hintergrund.");
		
		VPCDialog.setCancelable(false);
		
		VPCDialog.setPositiveButton("Aktivieren", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  new GetDate2(MainActivity.this).execute();
				  Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			      editor.putBoolean("rideCheck", true);
			      editor.putBoolean("frun2", false);
			      editor.commit();
			      restartApp();
			}
		});
		
		VPCDialog.setNegativeButton("Nicht aktivieren", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
				  Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			      editor.putBoolean("frun2", false);
			      editor.commit();
				  
			      Toast.makeText(MainActivity.this, "Falls sie es später noch aktivieren wollen, finden sie die Option in den Einstellungen.", Toast.LENGTH_SHORT).show();
			      
			      restartApp();
			  }
			});
		
		VPCDialog.show();
	}
	
	public void ZeigeANLEITUNG() {
		AlertDialog.Builder ANLDialog = new AlertDialog.Builder(this);
		
		ANLDialog.setTitle("Hinweis");
		ANLDialog.setMessage("-- Klasse einstellen --\nSie können den Vertretungsplan auch nur für ihre Klasse anzeigen! Drücken sie hierfür die Menütaste ihres Handys, tippen sie auf Einstellungen und wählen sie im Auswahlfeld unter »Klasse« ihre Klasse aus!\n\n-- Autom. Suche--\nGSApp kann auch selbstständig im Hintergrund nach einem neuen Vertretungsplan suchen! Öffnen sie dazu die Einstellungen, und haken sie die Einstellung »Im Hintergrund nach neuem Vertretungsplan suchen« unter »Weitere Einstellungen« an! Die App überprüft dann im Abstand von 30 Minuten, ob ein neuer Vertretungsplan verfügbar ist.\n\n-- Zoom anp. --\nFalls der Vertretungsplan zu groß für ihr Display ist, passen sie einfach den Zoom in den Einstellungen an. Öffnen sie dazu die Einstellungen, setzen den Haken bei »Größe der Inhalte« und wählen mit dem Schieberegler die richtige Größe aus.\n\n-- Design wählen --\nWenn sie ein dunkles Design bevorzugen, aktivieren sie in den Einstellungen die Option »BlackBean-Design verwenden«.\n\n-- Für Tablets --\nWenn sie ein Tablet verwenden, können sie die Tabletoberfläche aktivieren (dies geschieht in der Regel automatisch!). Öffnen sie die Einstellungsseite und setzen das Häkchen bei »Tablet-Oberfläche verwenden«. ACHTUNG: Verwenden sie die Tablet-Oberfläche nur bei Geräten mit mind. 7 Zoll Displaygröße!\n\nFalls sie noch Fragen haben, senden sie eine E-Mail an xorg@thexorg.tk (auch zu finden unter »Kontakt«). \nViel Spaß mit der App!\n\nPS: Es macht viel Arbeit, so eine App zu programmieren. Sie würden mir sehr helfen, wenn sie die App weiterempfehlen würden. Verwenden sie hierfür den »Teilen«-Button im Hauptmenü. Das ist für mich genug ;)");
		
		ANLDialog.setCancelable(false);
		
		ANLDialog.setNeutralButton("Los gehts", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			    editor.putBoolean("anl", true);
			    editor.commit();
			    
			    restartApp();
			}
		});
		
		ANLDialog.show();
	}
	
	public void ZeigeAndroidGingerbread() {
		AlertDialog.Builder GBDialog = new AlertDialog.Builder(this);
		
		GBDialog.setTitle("Android-System");
		GBDialog.setMessage("Sie verwenden ein Android-System, welches älter als »Honeycomb« (3.0) ist. Diese alten Android-Versionen werden nicht mehr 100%ig unterstützt, u.a. wird die Zoom-Einstellungn nicht und das BlackBean-Design nicht vollständig funktionieren. Um diese Funktionen komplett nutzen zu können, müssen sie (soweit verfügbar) ihr Android-System aktualisieren. Trotsdem Viel Spaß mit der App!");
		
		GBDialog.setCancelable(false);
		
		GBDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			    editor.putBoolean("gb", true);
			    editor.commit();
			}
		});
		
		GBDialog.show();
	}
	
	public void ZeigeAndroidKitkat() {
		AlertDialog.Builder KKDialog = new AlertDialog.Builder(this);
		
		KKDialog.setTitle("Android-System");
		KKDialog.setMessage("Sie verwenden ein Android-System der Version »KitKat« (+4.4). Sie können sich hiermit zu den >5% der Android-Nutzer mit »KitKat« zählen. Sie können hiermit den »Transcluent Mode« verwenden, d.h. die Leisten oben und unten sind transparent ;). Viel Spaß mit der App");
		
		KKDialog.setCancelable(false);
		
		KKDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	
				Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
			    editor.putBoolean("kk", true);
			    editor.commit();
			}
		});
		
		KKDialog.show();
	}
		
	public void startRepeatingTimer() {
	     Context context = this.getApplicationContext();
	     if(alarm != null){
	         if(!(alarm.CheckAlarm(this))) {
	        	 alarm.SetAlarm(context);
	         }
	     }else{
	    	 Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: SRT-AIN)", Toast.LENGTH_SHORT).show();
	     }
	  }
	
	public void cancelRepeatingTimer(){
	     Context context = this.getApplicationContext();
	     if(alarm != null){
	       if(alarm.CheckAlarm(this)) {
	    	   alarm.CancelAlarm(context);
	       }
	     }else{
	       Toast.makeText(context, "Es besteht keine Verbindung zur Überprüfungs-Programmklasse (Code: CRT-AIN)", Toast.LENGTH_SHORT).show();
	     }
	 }
	
	public void zeigeVP(View view) {
		Intent intent = new Intent(this, VPlanViewer.class);
	    startActivity(intent);
	}
	
	public void zeigeKT(View view) {
		Intent intent = new Intent(this, KontaktAnzeige.class);
	    startActivity(intent);
	}
	
	public void zeigeEP(View view) {
		Intent intent = new Intent(this, EssensplanViewer.class);
	    startActivity(intent);
	}
	
	public void zeigeEB(View view) {
		Intent intent = new Intent(this, Essensbestellung.class);
	    startActivity(intent);
	}
	
	public void zeigeTM(View view) {
		Intent intent = new Intent(this, InternetViewer.class);
		intent.putExtra(EXTRA_URL, "http://www.gymnasium-sonneberg.de/Informationen/Term/ausgebenK.php5");
		intent.putExtra(EXTRA_NAME, "Termine");
	    startActivity(intent);		
	}
	
	public void zeigeAK(View view) {
		Intent intent = new Intent(this, InternetViewer.class);
		intent.putExtra(EXTRA_URL, "http://www.gymnasium-sonneberg.de/Informationen/Aktuell/ausgeben.php5?id=");
		intent.putExtra(EXTRA_NAME, ".Aktuelles");
	    startActivity(intent);		
	}
	
	public void zeigeAB(View view) {
		Intent intent = new Intent(this, About.class);
	    startActivity(intent);
	}

    public void crashTester(View view) {
        Integer i = null;
        i.byteValue();
    }
	
	public void shareAPP() {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey, ich empfehle dir die GSApp, die App des Gymnasiums Sonneberg! http://tslink.tk/gsapp");
		sendIntent.setType("text/plain");
		startActivity(sendIntent);
	}
	
	public static String randomZ() {
		Random r = new Random();
		int i1 = r.nextInt(10 - 1) + 1;
		int i2 = r.nextInt(10 - 1) + 1;
		int i3 = r.nextInt(10 - 1) + 1;
		int i4 = r.nextInt(10 - 1) + 1;
		int i5 = r.nextInt(10 - 1) + 1;
		return "0" + i1 + i2 + i3 + i4 + i5;
		
	}
	
	public String readIDFile() {
          return PreferenceManager.getDefaultSharedPreferences(this).getString("id", "X0X0X0X0X");
    }
	
	public void restartApp() {
		Intent mStartActivity = new Intent(this, MainActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		System.exit(0);
	}
}