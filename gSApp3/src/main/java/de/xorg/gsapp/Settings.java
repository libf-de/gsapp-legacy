package de.xorg.gsapp;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity implements OnItemSelectedListener {
	
	String EinstellungKlasse = "";
	Boolean TabletMode = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
		if(BeanUI) {
			setTheme(R.style.AppThemeBlack);
		} else {
			setTheme(R.style.AppTheme);
		}
		setContentView(R.layout.settings);
		
		Util.setTranscluent(this, BeanUI);
		
		setTitle("Einstellungen");
		
		// Variablen
		Boolean TabletUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("tablet", false);
		Boolean VPASync = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("loadAsync", false);
		Boolean VPChecker = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("rideCheck", false);
        Button NachrichtenButton = (Button) findViewById(R.id.SendeNachricht);
        Button ALoginButton = (Button) findViewById(R.id.AutologinConfig);
		Spinner kla = (Spinner) findViewById(R.id.klasseEinst);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.klassen_array, android.R.layout.simple_spinner_item);
		final TextView zoomm = (TextView) findViewById(R.id.AnzeigeLabel);
		final SeekBar zoomS1 = (SeekBar) findViewById(R.id.ZoomBar);
		final CheckBox zoomS2 = (CheckBox) findViewById(R.id.ZoomOnOff);
		final CheckBox VerwendeTablet = (CheckBox) findViewById(R.id.UseTabletUI);
		final CheckBox BlackBean = (CheckBox) findViewById(R.id.UseBlack);
		final CheckBox VUpd = (CheckBox) findViewById(R.id.UseCheck);
		final CheckBox VASync = (CheckBox) findViewById(R.id.vpAsync);
		
		// Bildschirmgroesse
		if(TabletUI) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			TabletMode = true;
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			TabletMode = false;
		}

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		kla.setAdapter(adapter);
		kla.setOnItemSelectedListener(this);
		kla.setSelection(adapter.getPosition(PreferenceManager.getDefaultSharedPreferences(this).getString("klasse", "")));
		
		zoomm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				toaster("Zoomwert beim Anzeigen von Informationen. Wenn Anzeige zu groß = Kleinerer Wert, wenn Anzeige zu klein = Größerer Wert");
			}
		});
		
		VASync.setChecked(VPASync);
		VASync.setLongClickable(true);
		VASync.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
			    AlertDialog ad = new AlertDialog.Builder(Settings.this).create();  
			    ad.setCancelable(true); // This blocks the 'BACK' button
			    ad.setTitle("Kurzhilfe");
			    ad.setMessage("Synchron laden ist scheller als asynchrones Laden, allerdings kann es (insbesondere bei langsamen Internet) zu Instabilität führen. Wenn die App beim Laden des Vertretungsplans abstürtzt, aktivieren sie Asynchrones laden. Insbesondere für Samsung Galaxy-Handys wird Synchrones laden empfohlen, da das Asynchrone laden hier um die 2-3 Minuten dauert.\n\nSynchron laden = nicht angehakt, asynchron laden = angehakt");  
			    ad.setButton("OK", new DialogInterface.OnClickListener() {  
			        @Override  
			        public void onClick(DialogInterface dialog, int which) {  
			            dialog.dismiss();                      
			        }  
			    });  
			    ad.show();  
				return false;
			}
			
		});

		if(TabletMode) {
			VerwendeTablet.setChecked(true);
		} else {
			VerwendeTablet.setChecked(false);
		}
		
		if(BeanUI) {
			BlackBean.setChecked(true);
		} else {
			BlackBean.setChecked(false);
		}
        BlackBean.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BlackBean.isChecked()) {
                    AlertDialog ad = new AlertDialog.Builder(Settings.this).create();
                    ad.setCancelable(true); // This blocks the 'BACK' button
                    ad.setTitle("Info");
                    ad.setMessage("Dunkles Design ist noch experimentell. Der Vertretungsplan macht hier Probleme, da die Hintergrundfarbe der Karten nicht gesetzt werden kann. Dies wird möglicherweise behoben, wenn auf die offizielle Methode von Google zur Erzeugung dieser Karten gesetzt wird. Ob und wann ich dies tun werde, ist noch offen. Solange ist die Hintergrundfarbe hier nicht schwarz, sondern grau. Sorry.");
                    ad.setButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
            }
        });
		
		if(VPChecker) {
			VUpd.setChecked(true);
		} else {
			VUpd.setChecked(false);
		}
		
		BlackBean.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Set Theme OnTheFly
				
				Util.setTranscluent(Settings.this, BlackBean.isChecked());
			}
		});
		
		VUpd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Settings.this, "Diese Einstellung wird beim nächsten Start aktiv", Toast.LENGTH_LONG).show();
			}
		});
		
		if(!(TabletMode)) {
			final RadioButton call = (RadioButton) findViewById(R.id.radioButton1);
			final RadioButton dial = (RadioButton) findViewById(R.id.radioButton2);
			
			call.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(call.isChecked()) {
						dial.setChecked(false);
					} else {
						dial.setChecked(true);
					}
				}
			});
			
			dial.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(dial.isChecked()) {
						call.setChecked(false);
					} else {
						call.setChecked(true);
					}
				}
			});
			
			String ttyp = PreferenceManager.getDefaultSharedPreferences(this).getString("ruf", "dial");
			
			if(ttyp.equals("call")) {
				call.setChecked(true);
				dial.setChecked(false);
			} else {
				call.setChecked(false);
				dial.setChecked(true);
			}
			
		} else {
			DisplayMetrics dm = new DisplayMetrics();
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
		    	Button ResetTablet2 = (Button) findViewById(R.id.removeTablet);
		    	if(ResetTablet2 != null) {
		    		ResetTablet2.setVisibility(View.INVISIBLE);
		    	}
		    } else {
		    	Button ResetTablet3 = (Button) findViewById(R.id.removeTablet);
		    	if(ResetTablet3 != null) {
		    		ResetTablet3.setOnClickListener(new OnClickListener() {
			    		@Override
			    		public void onClick(View v) {
			    			CheckBox TabletBox = (CheckBox) findViewById(R.id.UseTabletUI);
			    			TabletBox.setChecked(false);
			    			Editor editor = PreferenceManager.getDefaultSharedPreferences(Settings.this).edit();
			    	        editor.putBoolean("tablet", VerwendeTablet.isChecked());
			    	        editor.commit();
			    	        Toast.makeText(Settings.this, "Bitte starten sie die App nun neu!", Toast.LENGTH_SHORT).show();
			    		}
			    	});
		    	}
		    }
		}
		
		VerwendeTablet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DisplayMetrics dm = new DisplayMetrics();
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
			    } else {
			    	Toast.makeText(Settings.this, "Ihr Display ist wahrscheinlich kleiner als 7 Zoll! Wenn die Oberfläche für ihr Gerät zu groß ist, löschen sie die Daten der GSApp in den Systemeinstellungen!", Toast.LENGTH_LONG).show();
			    }
			}
			
		});
		
		
		ALoginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Autologin();
			}
			
		});
		
		String ZLevel = PreferenceManager.getDefaultSharedPreferences(this).getString("zoomlevel", "");
		
		if(ZLevel.equals("")) {
			zoomS2.setChecked(false);
			zoomS1.setEnabled(false);
			zoomS1.setProgress(100);
		} else {
			try {
				zoomS2.setChecked(true);
				zoomS1.setEnabled(true);
				zoomS1.setProgress(Integer.parseInt(ZLevel) - 50);
			} catch(Exception ex) {
				
			}
		}
		
		zoomS2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(zoomS2.isChecked()) {
					zoomS1.setEnabled(true);
				} else {
					zoomS1.setEnabled(false);
				}
			}
		});
		
		NachrichtenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				SendeNachricht();
			}
		});
		
		NachrichtenButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				toaster("Eine Nachricht an den Entwickler senden!");
				return true;
			}
		});
		
		Button playstore = (Button) findViewById(R.id.StoreButton);
		
		playstore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id=de.xorg.gsapp"));
				startActivity(intent);
			}
		});	
		
		Toast.makeText(this, "Drücken sie den Zurück-Knopf um die Einstellungen zu speichern!", Toast.LENGTH_SHORT).show();
	}
	
	public void SendeNachricht() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	Toast.makeText(Settings.this, "Bitte geben sie ihr Feedback im Play Store :)", Toast.LENGTH_SHORT).show();
		        	Intent intent = new Intent(Intent.ACTION_VIEW);
		        	intent.setData(Uri.parse("market://details?id=de.xorg.gsapp"));
		        	startActivity(intent);
		        	break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            SendFehler();
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Was möchten sie uns mitteilen?").setPositiveButton("Feedback/Vorschlag", dialogClickListener)
		    .setNegativeButton("Fehler melden", dialogClickListener).show();
	}
	
	public void MirrorSelect() {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.mirror);
		dialog.setTitle("Serverauswahl");
		 
		// set the custom dialog components - text, image and button
		final RadioButton NUMAR = (RadioButton) dialog.findViewById(R.id.serverNumar);
		final RadioButton PROJECT = (RadioButton) dialog.findViewById(R.id.serverProject);
		String SetServer = PreferenceManager.getDefaultSharedPreferences(this).getString("mirror", "numar");
	    if(SetServer.equalsIgnoreCase("numar")) {
	    	((RadioButton) dialog.findViewById(R.id.serverNumar)).setChecked(true);
	    } else if(SetServer.equalsIgnoreCase("project")) {
	    	((RadioButton) dialog.findViewById(R.id.serverProject)).setChecked(true);
	    }
		 
		Button saveButton = (Button) dialog.findViewById(R.id.serverSave);
		Button cancelButton = (Button) dialog.findViewById(R.id.serverCancel);
		// if button is clicked, close the custom dialog
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String SERVER = "M";
				if(NUMAR.isChecked()) {
				 SERVER = "M";
				} else if(PROJECT.isChecked()) {
				 SERVER = "S";
				} else {
				 SERVER = "M";
				}
				 
				//MirrorManager.setMirror(SERVER, Settings.this);
				dialog.dismiss();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void Autologin() {
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.anmeldung);
		dialog.setTitle("Automatische Anmeldung");
		 
		// set the custom dialog components - text, image and button
		final TextView User = (TextView) dialog.findViewById(R.id.username);
		final CheckBox Use = (CheckBox) dialog.findViewById(R.id.autologinEnable);
		String Username = Datenspeicher.getUser(this);
	    if(Username.equalsIgnoreCase("")) {
	    	Use.setChecked(false);
	    	((TextView) dialog.findViewById(R.id.username)).setText("");
	    	((TextView) dialog.findViewById(R.id.password)).setText("");
	    } else {
	    	Use.setChecked(true);
	    	((TextView) dialog.findViewById(R.id.username)).setText(Datenspeicher.getUser(this));
	    	try {
	    		String PASSWORD_STATE = Datenspeicher.getPassword(this);
	    		if(PASSWORD_STATE.startsWith("error")) {
	    			if(PASSWORD_STATE.contains("nocb")) {
	    				Toast.makeText(this, "Fehler beim Laden des Passworts: Kontrollbyte fehlt!", Toast.LENGTH_SHORT).show();
	    				((TextView) dialog.findViewById(R.id.password)).setText("");
	    			} else {
	    				Toast.makeText(this, "Fehler beim Laden des Passworts!", Toast.LENGTH_SHORT).show();
	    				((TextView) dialog.findViewById(R.id.password)).setText("");
	    			}
	    		} else {
	    			((TextView) dialog.findViewById(R.id.password)).setText(PASSWORD_STATE);
	    		}
			} catch (Exception e) {
				((TextView) dialog.findViewById(R.id.password)).setText("");
				e.printStackTrace();
			}
	    }
		 
		Button saveButton = (Button) dialog.findViewById(R.id.serverSave);
		Button cancelButton = (Button) dialog.findViewById(R.id.serverCancel);
		// if button is clicked, close the custom dialog
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Use.isChecked()) {
					Datenspeicher.saveUser(User.getText().toString(), Settings.this);
					if(!Datenspeicher.savePassword(((TextView) dialog.findViewById(R.id.password)).getText().toString(), Settings.this)) {
						Toast.makeText(Settings.this, "Fehler beim Verschlüsseln des Passworts", Toast.LENGTH_SHORT).show();
					}
				} else {
					Datenspeicher.savePassword("", Settings.this);
					Datenspeicher.saveUser("", Settings.this);
				}
				dialog.dismiss();
			}
		});
		
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void SendFehler() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		
		final StringBuilder Report = new StringBuilder();
		
		Report.append("Schreiben sie die Beschreibung hier!\n");
		Report.append("\n");
		Report.append("\n");
		Report.append("Debug-Informationen:\n");
		Report.append(About.dataLoader(Settings.this));
		Report.append("\n");

		alert.setTitle("Fehler melden");
		alert.setMessage("Fehler bitte per E-Mail senden. Die App lädt automatisch eine Vorlage für den Fehlerreport in eine E-Mail-App ihrer Wahl. Fügen sie bitte noch eine Fehlerbeschreibung hinzu. Achten sie bitte darauf, eine möglichst genaue Beschreibung zu verfassen (mit »Die App ist abgestürzt« kann ich NICHTS anfangen, schreiben sie z.B. »als ich die Einstellungen geöffnet habe, ist die App abgestürzt«) \n\nKlicken sie auf Weiter, um eine E-Mail-App auszuwählen.");
		
		alert.setCancelable(true);

		alert.setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"xorgmc@gmail.com"});
			i.putExtra(Intent.EXTRA_SUBJECT, "GSApp Fehlerreport");
			i.putExtra(Intent.EXTRA_TEXT   , Report.toString());
			try {
			    startActivity(Intent.createChooser(i, "E-Mail-App auswählen"));
			} catch (android.content.ActivityNotFoundException ex) {
			    toaster("Fehler: Es sind keine E-Mail-Apps installiert!");
			}
		  }
		});

		alert.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		  }
		});

		alert.show();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		Spinner sp = (Spinner) findViewById(R.id.klasseEinst);
		String Name = (String) sp.getSelectedItem();
		EinstellungKlasse = Name.replace("keine", "");
    }

    public void onNothingSelected(AdapterView<?> parent) {
    	EinstellungKlasse = "";
    	Toast.makeText(this, "Hier läuft irgendwas falsch...", Toast.LENGTH_LONG).show();
    }
	
	@Override
	public void onBackPressed() {
		
		saveAll();
		
		Log.d("Settings", "Back pressed");
		
		Intent mStartActivity = new Intent(this, MainActivity.class);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
		System.exit(0);
        
        super.onBackPressed();
        return;
	}
	
	@Override
	public void onDestroy() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		super.onDestroy();
	}
	
	public void saveAll() {
		
		String ttyp = "dial";
		if(!(TabletMode)) {
			RadioButton call = (RadioButton) findViewById(R.id.radioButton1);
			
			if(call.isChecked()) {
				ttyp = "call";
			} else {
				ttyp = "dial";
			}
		}
		
		SeekBar zoomS1 = (SeekBar) findViewById(R.id.ZoomBar);
		CheckBox zoomS2 = (CheckBox) findViewById(R.id.ZoomOnOff); 
		CheckBox VerwendeTablet = (CheckBox) findViewById(R.id.UseTabletUI);
		CheckBox BlackBean = (CheckBox) findViewById(R.id.UseBlack);
		CheckBox VCheck = (CheckBox) findViewById(R.id.UseCheck);
		CheckBox VALoad = (CheckBox) findViewById(R.id.vpAsync);
		
		String ZOOMLEVEL = "";
		if(zoomS2.isChecked()) {
			ZOOMLEVEL = String.valueOf(zoomS1.getProgress() + 50);
		} else {
			ZOOMLEVEL = "";
		}
		
		Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString("klasse", EinstellungKlasse);
        if(!(TabletMode)) { editor.putString("ruf", ttyp); }
        editor.putBoolean("tablet", VerwendeTablet.isChecked());
        editor.putBoolean("loadAsync", VALoad.isChecked());
        editor.putBoolean("bean", BlackBean.isChecked());
        editor.putBoolean("rideCheck", VCheck.isChecked());
        editor.putString("zoomlevel", ZOOMLEVEL);
        editor.commit();
	}
	
	public void toaster(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
}



