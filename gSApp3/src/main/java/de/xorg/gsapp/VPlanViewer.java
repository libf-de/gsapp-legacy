package de.xorg.gsapp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.xorg.gsapp.R;

public class VPlanViewer extends Activity {
	public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
	
	@SuppressWarnings("unused")
	private Context c;
	private boolean isFiltered = false;
	private String Filter = null;
	private boolean singleMode = false;
	private CardUI mCardView;
	private boolean isDarkUI = false;
	private String dateD = "unbekannt";
	private String hinweisD = "kein";
	
	public String cDeutsch = "#013ADF";
	public String cMathe = "#FF0000";
	public String cMusik = "#BDBDBD";
	public String cKunst = "#9A2EFE";
	public String cGeografie = "#0B610B";
	public String cReligion = "#F7FE2E";
	public String cEthik = "#F7FE2E";
	public String cMNT = "#01DF01";
	public String cEnglisch = "#DF7401";
	public String cSport = "#000000";
	public String cBiologie = "#01DF01";
	public String cChemie = "#FF00FF";
	public String cPhysik = "#00FFFF";
	public String cSozialkunde = "#8A4B08";
	public String cInformatik = "#BDBDBD";
	public String cWirtschaftRecht = "#610B0B";
	public String cGeschichte = "#7401DF";
	
	
	
	public Eintrage vplane;
	
	private ProgressDialog progressDialog;  
	
	public String UrlToLoad; 
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
		
		Boolean BeanUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
		if(BeanUI) {
			setTheme(R.style.AppThemeSBlack);
		} else {
		    setTheme(R.style.AppTheme);
		}
		setContentView(R.layout.cards);
		
		//Tablet-Oberfläche einstellen
		Boolean TabletUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("tablet", false);
		if(TabletUI) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		final Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide);
		//isDarkUI = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("bean", false);
		
		mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setAnimation(anim);
		mCardView.setSwipeable(false);
		
		vplane = new Eintrage();
		
		Util.setTranscluent(this, BeanUI);
		
		magic();
		
	}
	
	public void magic() {
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		Boolean isInternetPresent = cd.isConnectingToInternet();
		if(isInternetPresent) {
			mCardView.clearCards();
			
			if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("loadAsync", false)) {
				new GetVPL().execute();
			} else {
				loadSynced();
			}
		} else {
			mCardView.clearCards();
			MyPlayCard card = new MyPlayCard("Keine Verbindung", "Es ist keine Netzwerkverbindung verfügbar.\nTippen um es erneut zu versuchen", "#FF0000", "#FF0000", true, false, isDarkUI);
			card.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					magic();
				}
				
			});
			mCardView.addCard(card);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		//new GetVPL().execute();
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if(singleMode) {
			singleMode = false;
			displayAll();
		} else {
			//Toast.makeText(this, "loading synced", Toast.LENGTH_SHORT).show();
			//loadSynced();
			super.onBackPressed();
		}
		
        return;
	}
	
	public void loadSynced() {
		String result = "";
		try {
            /*Apache HttpClient Library*/
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
			//HttpGet request = new HttpGet("http://cmp.pixelserver.ga/vp.html");
			request.setHeader("Accept-Charset", "utf-8");
			HttpResponse response = client.execute(request);
			/* response code*/
			BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
            	result = result + line + "\n";
            }
            char gf = (char) 34;
			String Klasse = PreferenceManager.getDefaultSharedPreferences(VPlanViewer.this).getString("klasse", "");
			
			try {
				if(result != "E") {
					String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
					dateD = gPart;
					String bemerkung = result.split("<tr \"id=Shinweis\">")[1].split("</tr>")[0].replaceAll("\\<.*?>","").replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß").replaceAll("[\\\r\\\n]+","").trim();
					hinweisD = bemerkung;
					//dateD = dateD + "\n" + bemerkung;
				}
			} catch(Exception ex) {
				//Allgemeiner Fehler beim Auswerten
				Log.d("GSApp Vertretungsplan-Check", "Fehler beim Auswerten der Informationen");
			}
			
			//GDebug.doLogWrite(result, "a-completehtml");
			
			if(result != "E") {
				String gPart = result.split("<tr id=\"Svertretungen\">\n")[1];
				
				//GDebug.doLogWrite(gPart, "a-gpart");
				
				String[] rawC = gPart.split("\n");
				
				String cleaned = clearUp(rawC);
				
				//GDebug.doLogWrite(cleaned, "a-clean");
				
				String[] newC = cleaned.split("\n");
				
				int counter = 1;
				int va = 0;
				String klasse = "";
				String stunde = "";
				String orgfach = "";
				String vertret = "";
				String raum = "";
				String verfach = "";
				String bemerkung = "";
				
				for(String cnt : newC) {
					if(counter == 1) {
						klasse = cnt;
						counter = counter + 1;
					} else if(counter == 2) {
						stunde = cnt;
						counter = counter + 1;
					} else if(counter == 3) {
						orgfach = cnt;
						counter = counter + 1;
					} else if(counter == 4) {
						vertret = cnt;
						counter = counter + 1;
					} else if(counter == 5) {
						raum = cnt;
						counter = counter + 1;
					} else if(counter == 6) {
						verfach = cnt;
						counter = counter + 1;
					} else if(counter == 7) {
						bemerkung = cnt;
						counter = 1;
						if(!hinweisD.equals("Hinweis:")) {
							MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false, isDarkUI);
							card.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									AlertDialog ad = new AlertDialog.Builder(VPlanViewer.this).create();  
								    ad.setCancelable(true); // This blocks the 'BACK' button  
								    ad.setTitle("Hinweis");
								    ad.setMessage(hinweisD);
								      
								    ad.setButton("OK", new DialogInterface.OnClickListener() {  
								        @Override  
								        public void onClick(DialogInterface dialog, int which) {  
								            dialog.dismiss();                      
								        }  
								    });  
								    ad.show();  
								}
								
							});
							mCardView.addCard(card);
						}
						if(Klasse.equals("")) {
							isFiltered = false;
							displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
							va = va + 1;
							klasse = "";
							stunde = "";
							orgfach = "";
							vertret = "";
							raum = "";
							verfach = "";
							bemerkung = "";
						} else {
							isFiltered = true;
							Filter = Klasse;
							String skl = String.valueOf(klasse.charAt(0));
							String SUCL = klasse.replace("/2", " " + skl + ".2");
							SUCL = SUCL.replace("/3", " " + skl + ".3");
							SUCL = SUCL.replace("/4", " " + skl + ".4");
							SUCL = SUCL.replace("/5", " " + skl + ".5");
							
							if(SUCL.contains(Klasse)) {
								displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
								va = va + 1;
							}
						}
					}
				}
				System.out.println("--- PARSE END AT " + System.currentTimeMillis() + " ---");
				displayAll();
				System.out.println("--- DISPLAY END AT " + System.currentTimeMillis() + " ---");
			} else {
				mkMsg("Space error :(");
			}
		} catch (Exception exe) {
			exe.printStackTrace();
        }
	}
	
	private class GetVPL extends AsyncTask<String, Void, String> {
		GetVPL(){
		    Log.d("GSApp-Gino", "Lade Vertretungsplan");
		}
		
		@Override  
        protected void onPreExecute()  
        {  
			 System.setProperty("http.keepAlive", "false");
            //Create a new progress dialog  
            progressDialog = new ProgressDialog(VPlanViewer.this);  
            //Set the progress dialog to display a horizontal progress bar  
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
            //Set the dialog title to 'Loading...'  
            progressDialog.setTitle("GSApp 4.0 »Gino«");  
            //Set the dialog message to 'Loading application View, please wait...'  
            progressDialog.setMessage("Lade Daten...");  
            //This dialog can't be canceled by pressing the back key  
            progressDialog.setCancelable(false);  
            //This dialog isn't indeterminate  
            progressDialog.setIndeterminate(true);  
            //Display the progress dialog  
            progressDialog.show();  
            
            System.out.println("--- GET START AT " + System.currentTimeMillis() + " ---");
        }  

		 protected String doInBackground(String... message) {
			 HttpClient httpclient;
			 HttpGet request;
			 HttpResponse response = null;
			 String result = "";
			 try {
			 httpclient = new DefaultHttpClient();
			 request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
			 response = httpclient.execute(request);
			 }
			 catch (Exception e) {
			 result = "E";
			 e.printStackTrace();
			 }
			 try {
			 BufferedReader rd = new BufferedReader(new InputStreamReader(
			 response.getEntity().getContent()));
			 String line = "";
			 while ((line = rd.readLine()) != null) {
			 result = result + line + "\n" ;
			 }
			 } catch (Exception e) {
			 result = "E";
			 e.printStackTrace();
			 }
			 return result;
		}

		protected void onPostExecute(String result) {
			System.out.println("--- GET END AT " + System.currentTimeMillis() + " ---");
			//close the progress dialog  
            progressDialog.dismiss(); 
			char gf = (char) 34;
			String Klasse = PreferenceManager.getDefaultSharedPreferences(VPlanViewer.this).getString("klasse", "");
			
			try {
				if(result != "E") {
					String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
					dateD = gPart;
				}
			} catch(Exception ex) {
				//Allgemeiner Fehler beim Auswerten
				Log.d("GSApp Vertretungsplan-Check", "Fehler beim Auswerten der Informationen");
			}
			
			if(result != "E") {
				String gPart = result.split("<tr id=\"Svertretungen\">\n")[1];
				String[] rawC = gPart.split("\n");
				
				String[] newC = clearUp(rawC).split("\n");
				
				int counter = 1;
				int va = 0;
				String klasse = "";
				String stunde = "";
				String orgfach = "";
				String vertret = "";
				String raum = "";
				String verfach = "";
				String bemerkung = "";
				
				for(String cnt : newC) {
					if(counter == 1) {
						klasse = cnt;
						counter = counter + 1;
					} else if(counter == 2) {
						stunde = cnt;
						counter = counter + 1;
					} else if(counter == 3) {
						orgfach = cnt;
						counter = counter + 1;
					} else if(counter == 4) {
						vertret = cnt;
						counter = counter + 1;
					} else if(counter == 5) {
						raum = cnt;
						counter = counter + 1;
					} else if(counter == 6) {
						verfach = cnt;
						counter = counter + 1;
					} else if(counter == 7) {
						bemerkung = cnt;
						counter = 1;
						
						if(Klasse.equals("")) {
							isFiltered = false;
							displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
							va = va + 1;
							klasse = "";
							stunde = "";
							orgfach = "";
							vertret = "";
							raum = "";
							verfach = "";
							bemerkung = "";
						} else {
							isFiltered = true;
							Filter = Klasse;
							String skl = String.valueOf(klasse.charAt(0));
							String SUCL = klasse.replace("/2", " " + skl + ".2");
							SUCL = SUCL.replace("/3", " " + skl + ".3");
							SUCL = SUCL.replace("/4", " " + skl + ".4");
							SUCL = SUCL.replace("/5", " " + skl + ".5");
							
							if(SUCL.contains(Klasse)) {
								displayStuff(klasse, stunde, orgfach, vertret, raum, verfach, bemerkung);
								va = va + 1;
							}
						}
					}
				}
				System.out.println("--- PARSE END AT " + System.currentTimeMillis() + " ---");
				displayAll();
				System.out.println("--- DISPLAY END AT " + System.currentTimeMillis() + " ---");
			} else {
				mkMsg("Space error :(");
			}
		}

	}
	
	private void displayStuff(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
		Eintrag mei = new Eintrag(klasse, stunde, fachnormal, vertretung, raum, fachvertret, bemerkung);
		vplane.add(mei);
		//mCardView.addCard(new MyPlayCard(stunde + ". Stunde", "Statt " + fachnormal + " " + fachvertret + " bei " + vertretung + " in Raum " + raum + ".\n" + bemerkung, getFachColor(fachnormal), getFachColor(fachnormal), false, false));
		//TextView vpv = (TextView) findViewById(R.id.vpView);
		//vpv.setText(vpv.getText().toString().replace("Laden...", "") + "\n\nKlasse: " + klasse + " || Stunde: " + stunde + " || Fach normal: " + fachnormal + " || Vertretung: " + vertretung + " || Raum: " + raum + " || Fach vertr.: " + fachvertret + " || Bemerkung: " + bemerkung);
	}
	
	private void displayAll() {
		mCardView.clearCards();
		CardStack dateHead = new CardStack();
		dateHead.setTitle("Für " + dateD);
		mCardView.addStack(dateHead);
		
		if(!hinweisD.equals("Hinweis:")) {
			MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false, isDarkUI);
			card.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog ad = new AlertDialog.Builder(VPlanViewer.this).create();  
				    ad.setCancelable(true); // This blocks the 'BACK' button  
				    ad.setTitle("Hinweis");
				    ad.setMessage(hinweisD);
				      
				    ad.setButton("OK", new DialogInterface.OnClickListener() {  
				        @Override  
				        public void onClick(DialogInterface dialog, int which) {  
				            dialog.dismiss();                      
				        }  
				    });  
				    ad.show();  
				}
				
			});
			mCardView.addCard(card);
		}
		
		try {
			if(isFiltered) {
				CardStack stacky = new CardStack();
				stacky.setTitle("Vertretungen für Klasse " + Filter);
				mCardView.addStack(stacky);
			}
			for(String klassee : vplane.getKlassen()) {
				if(!isFiltered) {
					CardStack stacky = new CardStack();
					stacky.setTitle("Klasse " + klassee);
					mCardView.addStack(stacky);
				}
				for(final Eintrag single : vplane.getKlasseGruppe(klassee, !isFiltered)) {
					MyPlayCard card;
					if(single.getBemerkung().equals("Ausfall")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde - Ausfall!", "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
					} else if(single.getBemerkung().equals("Stillbesch.")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde - Stillbesch.!", "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
					} else if(single.getBemerkung().equals("AA")) {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
					} else if(single.getFachNormal().equals(single.getFachVertretung())) {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Du hast " + LongName(single.getFachNormal()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
					} else {
						card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
					}
					if(isFiltered) {
						card.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								displayMoreInformation(single);
							}
							
						});
						mCardView.addCard(card);
					} else {
						card.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								displaySingleClass(single.getKlasse());
							}
							
						});
						mCardView.addCardToLastStack(card);
					}
				}
			}
		} catch (KeineKlassenException e) {
			// TODO Auto-generated catch block
			mCardView.addCard(new MyPlayCard("Keine Vertretungen", "", cMNT, cMNT, false, false, isDarkUI));
			e.printStackTrace();
		} catch (KeineEintrageException e) {
			// TODO Auto-generated catch block
			mCardView.addCard(new MyPlayCard("ERROR", "KeineEinträgeException", "#FF0000", "#FF0000", false, false, isDarkUI));
			e.printStackTrace();
		}
		
		mCardView.refresh();
	}
	
	private void displaySingleClass(String klasse) {
		mCardView.clearCards();
		singleMode = true;
		CardStack dateHead = new CardStack();
		dateHead.setTitle("Für " + dateD);
		mCardView.addStack(dateHead);
		
		if(!hinweisD.equals("Hinweis:")) {
			MyPlayCard card = new MyPlayCard("Hinweis:", hinweisD.replace("Hinweis:", "").replaceAll("[\\\r\\\n]+","").trim(), "#00FF00", "#00FF00", true, false, isDarkUI);
			card.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog ad = new AlertDialog.Builder(VPlanViewer.this).create();  
				    ad.setCancelable(true); // This blocks the 'BACK' button  
				    ad.setTitle("Hinweis");
				    ad.setMessage(hinweisD);
				      
				    ad.setButton("OK", new DialogInterface.OnClickListener() {  
				        @Override  
				        public void onClick(DialogInterface dialog, int which) {  
				            dialog.dismiss();                      
				        }  
				    });  
				    ad.show();  
				}
				
			});
			mCardView.addCard(card);
		}
		
		try {
			CardStack stackPlay = new CardStack();
			stackPlay.setTitle("Vertretungen für Klasse " + klasse);
			mCardView.addStack(stackPlay);
			
			for(final Eintrag single : vplane.getKlasseGruppeS(klasse)) {
				MyPlayCard card;
				if(single.getBemerkung().equals("Ausfall")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde - Ausfall!", "Statt " + LongName(single.getFachNormal()) + " hast du Ausfall (Raum " + single.getRaum() + ")", getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
				} else if(single.getBemerkung().equals("Stillbesch.")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde - Stillbesch.!", "Statt " + LongName(single.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
				} else if(single.getBemerkung().equals("AA")) {
					card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + single.getRaum(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
				} else {
					card = new MyPlayCard(single.getStunde() + ". Stunde", "Statt " + LongName(single.getFachNormal()) + " hast du " + LongName(single.getFachVertretung()) + " bei " + single.getVertretung() + " in Raum " + single.getRaum() + ".\n\n" + single.getBemerkung(), getFachColor(single.getFachNormal()), getFachColor(single.getFachNormal()), true, false, isDarkUI);
				}
				card.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						displayMoreInformation(single);
					}
					
				});
				mCardView.addCard(card);
			}
			
			mCardView.refresh();
		} catch (KeineEintrageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void displayMoreInformation(Eintrag eintrag) {
	    AlertDialog ad = new AlertDialog.Builder(this).create();  
	    ad.setCancelable(true); // This blocks the 'BACK' button  
	    ad.setTitle("Information");
	    if(eintrag.getBemerkung().equals("Ausfall")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Ausfall (Raum " + eintrag.getRaum() + ")");
	    } else if(eintrag.getBemerkung().equals("Stillbesch.")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Stillbeschäftigung im Raum " + eintrag.getRaum());
	    } else if(eintrag.getBemerkung().equals("AA")) {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du Arbeitsauftrag im Raum " + eintrag.getRaum());
	    } else {
	    	ad.setMessage("Statt " + LongName(eintrag.getFachNormal()) + " hast du " + LongName(eintrag.getFachVertretung()) + " bei " + eintrag.getVertretung() + " im Raum " + eintrag.getRaum() + "\n\nBemerkung: " + eintrag.getBemerkung());
	    }
	      
	    ad.setButton("OK", new DialogInterface.OnClickListener() {  
	        @Override  
	        public void onClick(DialogInterface dialog, int which) {  
	            dialog.dismiss();                      
	        }  
	    });  
	    ad.show();  
	}
	
	private String getFachColor(String fach) {
		switch(fach.toLowerCase()) {
		case "de":
			return cDeutsch;
		case "ma":
			return cMathe;
		case "mu":
			return cMusik;
		case "ku":
			return cKunst;
		case "gg":
			return cGeografie;
		case "re":
			return cReligion;
		case "et":
			return cEthik;
		case "mnt":
			return cMNT;
		case "en":
			return cEnglisch;
		case "sp":
			return cSport;
		case "spj":
			return cSport;
		case "spm":
			return cSport;
		case "bi":
			return cBiologie;
		case "ch":
			return cChemie;
		case "ph":
			return cPhysik;
		case "sk":
			return cSozialkunde;
		case "if":
			return cInformatik;
		case "wr":
			return cWirtschaftRecht;
		default:
			return cSport;
			
		}
	}
	
	private String LongName(String fach) {
		switch(fach.toLowerCase()) {
		case "de":
			return "Deutsch";
		case "ma":
			return "Mathe";
		case "mu":
			return "Musik";
		case "ku":
			return "Kunst";
		case "gg":
			return "Geografie";
		case "re":
			return "Religion";
		case "et":
			return "Ethik";
		case "mnt":
			return "MNT";
		case "en":
			return "Englisch";
		case "sp":
			return "Sport";
		case "spj":
			return "Sport Jungen";
		case "spm":
			return "Sport Mädchen";
		case "bi":
			return "Biologie";
		case "ch":
			return "Chemie";
		case "ph":
			return "Physik";
		case "sk":
			return "Sozialkunde";
		case "if":
			return "Informatik";
		case "wr":
			return "Wirtschaft/Recht";
		default:
			return fach;
			
		}
	}
	
	private String clearUp(String[] inpud) {
		char gf = (char) 34;
		String me = "";
		for(String ln : inpud) {
			ln = ln.replaceAll("\\<.*?>","");
			ln = ln.replace("&uuml;", "ü").replace("&Uuml;", "Ü").replace("&auml;", "ä").replace("&Auml;", "Ä").replace("&ouml;", "ö").replace("&Ouml;", "Ö").replace("&szlig;", "ß");
			ln = ln.replace("                        ", "");
			//ln = ln.replace("        ", "");
			ln = ln.trim();
			ln = ln.replace("	", "");
			
			if(ln.equals("      ")) {
			} else if(ln.equals("var hoehe = parent.document.getElementById('inhframe').style.height;")) {
			} else if(ln.equals("setFrameHeight();")) {
			} else if(ln.equals("var pageTracker = _gat._getTracker(" + gf + "UA-5496889-1" + gf + ");")) {
			} else if(ln.equals("pageTracker._trackPageview();")) {
			} else if(ln.equals("    ")) {
			} else if(ln.equals("	")) {
			} else if(ln.equals("  ")) {
			} else if(ln.startsWith("var")) {
			} else if(ln.startsWith("document.write")) {
			} else if(ln.equals("")) {
			//} else if(ln.endsWith("&nbsp;")) {
			//	me = me + "XXXX\n";
			} else {
				if (ln.matches(".*\\w.*")) {
					me = me + ln + "\n";
				} else if(ln.contains("##")) {
					me = me + ln + "\n";
				}
			}
		}
		
		return me;
	}
	
	private static int[] unset(int[] arrIn, int index) 
	{
	    int i;

	    // new array is shorter
	    int[] arrOut = new int[arrIn.length-1];

	    // copy element "before" arrIn[index]
	    for(i = 0; i < index ; i++) {
	        arrOut[i] = arrIn[i];
	    }

	    // copy element "after" arrIn[index]
	    for(i = index; i < arrOut.length ; i++) {
	        arrOut[i] = arrIn[i+1];
	    }

	    return arrOut;
	}
	
	public void mkMsg(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		Log.d("gsapp-space", msg);
	}	
	
}

class Eintrag {
	String Klasse;
	String Stunde;
	String Fachnormal;
	String Vertretung;
	String Raum;
	String Fachvertret;
	String Bemerkung;
	Eintrag(String klasse, String stunde, String fachnormal, String vertretung, String raum, String fachvertret, String bemerkung) {
		Klasse = klasse;
		Stunde = stunde;
		Fachnormal = fachnormal;
		Vertretung = vertretung;
		Raum = raum;
		Fachvertret = fachvertret;
		Bemerkung = bemerkung;
	}
	
	public String getKlasse() {
		return Klasse;
	}
	public String getStunde() {
		return Stunde;
	}
	public String getFachNormal() {
		return Fachnormal;
	}
	public String getVertretung() {
		if(Vertretung.equals("##")) {
			return "niemandem";
		} else {
			return Vertretung;
		}
	}
	public String getRaum() {
		if(Raum.equals("##")) {
			return "k.A.";
		} else {
			return Raum;
		}
	}
	public String getFachVertretung() {
		if(Fachvertret.equals("##")) {
			return "nichts";
		} else {
			return Fachvertret;
		}
	}
	public String getBemerkung() {
		if(Bemerkung.equals("&nbsp;")) {
			return "keine Bemerkung";
		} else {
			return Bemerkung;
		}
	}
	
	public void setKlasse(String value) {
		Klasse = value;
	}
	public void setStunde(String value) {
		Stunde = value;
	}
	public void setFachNormal(String value) {
		Fachnormal = value;
	}
	public void setVertretung(String value) {
		Vertretung = value;
	}
	public void setRaum(String value) {
		Raum = value;
	}
	public void setFachVertretung(String value) {
		Fachvertret = value;
	}
	public void setBemerkung(String value) {
		Bemerkung = value;
	}
	
	public Boolean isKlasse(String input) {
		if(Klasse == input) {
			return true;
		} else {
			return false;
		}
	}
}

class Eintrage extends ArrayList<Eintrag> {
	public ArrayList<Eintrag> getKlasseGruppe(String klasse, Boolean reverse) throws KeineEintrageException {
		ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
		for(Eintrag single : this) {
			Log.d("GSApp-Gino", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
			if(single.getKlasse().equals(klasse)) {
				outp.add(single);
			}
		}
		
		if(outp.size() < 1) {
			throw new KeineEintrageException();
		} else {
			if(reverse) {
				Collections.reverse(outp);
			}
			return outp;
		}
	}
	
	
	public ArrayList<Eintrag> getKlasseGruppeS(String klasse) throws KeineEintrageException {
		ArrayList<Eintrag> outp = new ArrayList<Eintrag>();
		for(Eintrag single : this) {
			Log.d("GSApp-Gino", "ET: " + single.getKlasse() + " vs SUCH: " + klasse);
			if(single.getKlasse().equals(klasse)) {
				outp.add(single);
			}
		}
		
		if(outp.size() < 1) {
			throw new KeineEintrageException();
		} else {
			return outp;
		}
	}
	
	public ArrayList<String> getKlassen() throws KeineKlassenException {
		String liste = "";
		for(Eintrag single : this) {
			if(!liste.contains(single.getKlasse())) {
				liste = liste + single.getKlasse() + ",";
			}
		}
		
		if(liste.equals("")) {
			throw new KeineKlassenException();
		} else {
			liste = method(liste);
			ArrayList<String> outp = new ArrayList<String>();
			Collections.addAll(outp, liste.split(","));
			return outp;
		}
	}
	
	public String method(String str) {
	    if (str.length() > 0 && str.charAt(str.length()-1)=='x') {
	      str = str.substring(0, str.length()-1);
	    }
	    return str;
	}
}

class KeineEintrageException extends Exception {
	
}

class KeineKlassenException extends Exception {
	
}