package de.xorg.gsapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class CheckService extends BroadcastReceiver {
	final public static String ONE_TIME = "onetime";
	 
	public static Context MC;
	public static NotificationManager NM;
	
	public static PowerManager pm;
	public static PowerManager.WakeLock wl;
	
	public static Boolean isRunning = false;

    private GALog l;
	
	// Wird alle 30 Minuten ausgeführt wenn Service läuft
	
	//Um Hinweis zu entfernen, »Das WakeLock nicht immer entfernt wird« (wird es aber trotsdem)
	 @SuppressLint("Wakelock")
	@Override
	 public void onReceive(Context context, Intent intent) {
	         pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	         wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GSApp Check");

             l = new GALog(context);
	         
	         //WakeLock aktivieren
	         if(wl != null) {
	        	 wl.acquire();
	         }
	         
	         MC = context;
	         NM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	         
	         ConnectionDetector cd = new ConnectionDetector(context);
	         
	         Boolean isInternetPresent = cd.isConnectingToInternet(); // Besteht eine Verbindung zum Internet?
	         
	         if(isInternetPresent) {
	        	 new GetDate(context).execute();
	         } else {
	        	 wl.release();
	         }
	         
	         //WakeLock wird nach Überprüfung entfernt (weil Überprüfung Async ist)
	 }
	 
	 // Service starten
	 public void SetAlarm(Context context)
	    {
	        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	        Intent intent = new Intent(context, CheckService.class);
	        intent.putExtra(ONE_TIME, Boolean.FALSE);
	        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
	        
	        isRunning = true;
	        
	        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 1800 , pi);
	    }
	 
	    // Service abbrechen
	    public void CancelAlarm(Context context)
	    {
	        Intent intent = new Intent(context, CheckService.class);
	        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
	        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	        
	        isRunning = false;
	        
	        alarmManager.cancel(sender);
	    }
	     
	    // Einmalig sofort prüfen
	    public void ForceCheck(Context c) {
	    	 MC = c;
	         NM = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
	         
	         ConnectionDetector cd = new ConnectionDetector(c);
	         
	         Boolean isInternetPresent = cd.isConnectingToInternet(); // Besteht eine Verbindung zum Internet?
	         
	         Toast.makeText(c, "Forcing check method...", Toast.LENGTH_SHORT).show();
	         
	         if(isInternetPresent) {
	        	 Toast.makeText(c, "Internet is present", Toast.LENGTH_SHORT).show();
	        	 new GetDate(c).execute();
	         }
	    }
	    // Methode zum Überprüfen ob der Service (schon) läuft
	    public Boolean CheckAlarm(Context context) {
	    	return isRunning;
	    }
	    
	    public static void PostNotification(String text, String datum, String pageBody) {
            GALog loc = new GALog(CheckService.MC);
	    	Intent intent = new Intent(CheckService.MC, VPlanViewer.class);
	    	PendingIntent pIntent = PendingIntent.getActivity(CheckService.MC, 0, intent, 0);

	    	NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(CheckService.MC)
	    	     .setSmallIcon(R.drawable.vertretung)
	    	     .setContentTitle("Neuer Vertretungsplan!")
	    	     .setWhen(System.currentTimeMillis())
	    	     .setContentIntent(pIntent)
	    	     .setAutoCancel(true)
	    	     .setContentText(text);

	    	    mBuilder.setContentIntent(pIntent);
	    	    try {
	    	    	NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
		    	    String[] events = parsePage(pageBody).split("\n");
		    	    // Sets a title for the Inbox in expanded layout
		    	    inboxStyle.setBigContentTitle("Vertretungsplan:");
		    	    // Moves events into the expanded layout
		    	    for (int i=0; i < events.length; i++) {

		    	        inboxStyle.addLine(events[i]);
		    	    }
		    	    // Moves the expanded layout object into the notification object.
		    	    mBuilder.setStyle(inboxStyle);
	    	    } catch(Exception ex) {
                    loc.debug("Error creating expanded notification: " + ex.getMessage());
	    	    }

	    	    NotificationManager mNotificationManager =  (NotificationManager) CheckService.MC.getSystemService(Context.NOTIFICATION_SERVICE);
	    	    mNotificationManager.notify(0, mBuilder.build()); 
	    }
	    
	    public static String clearUp(String[] inpud) {
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
	    
	    public static String parsePage(String pageBody) {
	    	char gf = (char) 34;
            GALog loc = new GALog(CheckService.MC);

			String Klasse = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("klasse", "");
			
			String outpString = "";

            loc.debug("CheckService/Parse/PageDebug: +" + pageBody.replace("\n", "*BR*") + "+");
			
			if(pageBody != "E") {
				String gPart = pageBody.split("<tr id=\"Svertretungen\">")[1];
                loc.debug("CheckService/Parse/PartDebug: +" + gPart.replace("\n", "*BR") + "+");
				
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
							String oneEntry = klasse + ": " + stunde + ". - " + orgfach + " => " + verfach;
							outpString += oneEntry + "\n";
							va = va + 1;
							klasse = "";
							stunde = "";
							orgfach = "";
							vertret = "";
							raum = "";
							verfach = "";
							bemerkung = "";
						} else {
							String skl = String.valueOf(klasse.charAt(0));
							String SUCL = klasse.replace("/2", " " + skl + ".2");
							SUCL = SUCL.replace("/3", " " + skl + ".3");
							SUCL = SUCL.replace("/4", " " + skl + ".4");
							SUCL = SUCL.replace("/5", " " + skl + ".5");
							
							if(SUCL.contains(Klasse)) {
								String oneEntry = stunde + ". - " + orgfach + " => " + verfach;
								outpString += oneEntry + "\n";
								va = va + 1;
							}
						}
					}
				}
				return outpString;
			} else {
                loc.debug("CheckServce/Parse: Error parsing (LOAD=E) page");
				return "null";
			}
	    }
	    
	  //Datum des zuletzt angesehenem Vertretungsplan auslesen
	    public static String getRiddenDate() {
	    	String DATUM = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("readDate", "ERR");
	    	return DATUM;
	    }

        public static void setRiddenDate(String when) {
            SharedPreferences.Editor ed = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).edit();
            ed.putString("readDate", when);
            ed.commit();
        }
	    
	    //Gespeicherte Klasse auslesen (unbenutzt)
	    public static String getKlasse() {
	    	String CLASS = PreferenceManager.getDefaultSharedPreferences(CheckService.MC).getString("klasse", "");
	    	return CLASS;
	    }
	    
	    //Methode um WakeLock zu entfernen
	    public static void RemoveWakelock() {
	    	try {
	    		if(wl != null) {
		    		wl.release();
		    	}
	    	} catch(Exception ex) {
	    		Log.d("GSApp Vertretungsplan-Check", "Fehler beim WakeLock entfernen");
	    	}
	    }
}

class GetDate extends AsyncTask<String, Void, String> {
	Context cont;
	GetDate(Context ct){
	    this.cont = ct;
	}

	 protected String doInBackground(String... message) {
	    HttpClient httpclient;
	    HttpGet request;
	    HttpResponse response = null;
	    String result = "";

	    // Verbindung zum Server mit der "Apache HttpClient Library" aufbauen
	    try {
	        httpclient = new DefaultHttpClient();
	        request = new HttpGet("http://www.gymnasium-sonneberg.de/Informationen/vp.html");
	        response = httpclient.execute(request);
	    }

	    catch (Exception e) {
	        // Code um Fehler zu behandeln
	        result = "error";
	    }

	    // Serverantwort auswerten
	    try {
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
	                response.getEntity().getContent()));
	        String line = "";
	        while ((line = rd.readLine()) != null) {

	            // Serverantwort auslesen
	            result = result + line + "\n" ;
	        }
	    } catch (Exception e) {
	        // Code um Fehler zu behandeln
	        result = "error";
	    }
	    return result;
	}

	protected void onPostExecute(String result) {
		try {
			//Serverantwort (Datum des verfügbarem Vertretungsplan)
			char gf = (char) 34;
            GALog loc = new GALog(CheckService.MC);

			if(result != "E") {
				String gPart = result.split("<td colspan=\"7\" class=\"vpUeberschr\">")[1].split("</td>")[0].replace("        ", "");
				String gDate = gPart.replace("Montag, den ", "").replace("Dienstag, den ", "").replace("Mittwoch, den ", "").replace("Donnerstag, den ", "").replace("Freitag, den ", "")/*.split(".")*/;
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
				DateFormat RC = new SimpleDateFormat("yyyyMMdd");
				Date dt = df.parse(gDate);
				String GRC = RC.format(dt);
                loc.debug("CheckService/GetDate: Datum von Server erhalten: +" + GRC + "+");
				
			
			
			//Datum des zuletzt angesehenem Vertretungsplan
			String riddenDate = CheckService.getRiddenDate();
                loc.debug("CheckService/GetDate: Gespeichertes Datum: -" + riddenDate + "-");

            if(riddenDate.equals("ERR")) {
                CheckService.setRiddenDate(GRC);
                CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", GRC, result);
            } else if(result.equals("error")) {
				//Kein zuletzt angesehen-Datum verfügbar oder fehlerhafte Antwort vom Server
                loc.error("CheckService/GetDate: Serverfehler (RD:" + riddenDate + ")");
			} else {
				//Gültige Serverantwort
				int rideDate = Integer.parseInt(riddenDate);
				int newDate = Integer.parseInt(GRC);
				
				if(newDate > rideDate) {
					//Verfügbarer Vertretungsplan ist neuer als zuletzt angesehener
                    loc.debug("CheckService/GetDate: Neuer Plan verfügbar");
					CheckService.PostNotification("Es ist ein neuer Vertretungsplan verfügbar!", String.valueOf(newDate), result);
                    loc.debug("CheckService/GetDate: Neuer Plan verfügbar-POST NTF");
				} else {
					loc.debug("CheckService/GetDate: Kein neuer Plan verfügbar");
				}
			}
			}
			CheckService.RemoveWakelock();
		} catch(Exception ex) {
			//Allgemeiner Fehler beim Auswerten
			ex.printStackTrace();
			Log.d("GSApp Vertretungsplan-Check", "Fehler beim Auswerten der Informationen");
			CheckService.RemoveWakelock();
		}
	}

}