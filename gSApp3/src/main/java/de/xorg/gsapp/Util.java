package de.xorg.gsapp;


import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.ViewConfiguration;
import android.widget.Toast;

import java.util.Calendar;

public class Util {

    public static String getUserAgentString(Context c, boolean isSync) {
        String MORE;
        if(isSync) {
            MORE = "syncLoad";
        } else {
            MORE = "asyncLoad";
        }
        return "GSApp " + getVersion(c) + " on " + About.getDeviceName() + " (Android " + android.os.Build.VERSION.RELEASE.toString() + ") " + MORE;
    }
 
    public static String getVersionID(Context context){
        String ID = context.getString(R.string.version);
        String[] UID = ID.split(" ");
        return UID[0];
    }

    public static String getVersion(Context context) {
        String VER = context.getString(R.string.version);
        String[] VRS = VER.split(" ");
        if(BuildConfig.DEBUG && !VER.startsWith("The")) {
            return VRS[0] + "D " + VRS[1];
        } else {
            return VER;
        }
    }
    
    public static int getVersionCode(Context context) {
    	PackageInfo pinfo;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }
    
    public static String getReleaseName(Context context) {
    	String ID = context.getString(R.string.version);
        String[] UID = ID.split(" ");
        char gf = (char) 34;
        String GFF = String.valueOf(gf);
        
        String CODENAME = ID.replace(UID[0], "").replace(GFF, "");
        
        return CODENAME;
    }

    public static void setThemeUI(Activity a) {
        switch (PreferenceManager.getDefaultSharedPreferences(a).getString("themeMode", "android")) {
            case "android":
                if(hasSoftNavigation(a)) {
                    a.setTheme(R.style.AppTheme);
                } else {
                    a.setTheme(R.style.PAppTheme);
                }
                break;
            case "classic":
                a.setTheme(R.style.ClassicTheme);
                break;
            case "holo":
                a.setTheme(R.style.HoloTheme);
                break;
            case "holodark":
                a.setTheme(R.style.HoloDarkTheme);
                break;
            case "material":
                a.setTheme(R.style.MaterialTheme);
                break;
            case "materialdark":
                a.setTheme(R.style.MaterialDarkTheme);
                break;
            default:
                Toast.makeText(a, "WARNUNG: UNGÜLTIGE ENTWICKEREINSTELLUNG UITHEMEMODE!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("NewApi")
    public static boolean hasSoftNavigation(Context context)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            return !ViewConfiguration.get(context).hasPermanentMenuKey();
        }
        return false;
    }

    public static void setOrientation(Activity a) {
        Calendar c = Calendar.getInstance();
        if(c.get(Calendar.DAY_OF_MONTH) == 1 && c.get(Calendar.MONTH) == Calendar.APRIL) {
            a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            Toast.makeText(a, "APRIL APRIL :D", Toast.LENGTH_SHORT).show();
        } else {
            switch (PreferenceManager.getDefaultSharedPreferences(a).getInt("rotateMode", 1)) {
                case 3:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
                    break;
                case 10:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    break;
                case 13:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
                    break;
                case 0:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
                case 14:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    break;
                case 5:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    break;
                case 1:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case 8:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case 9:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case 4:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    break;
                case 6:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                case 7:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case -1:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
                case 2:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    break;
                case 11:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
                    break;
                case 12:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
                    break;
                default:
                    a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    Toast.makeText(a, "WARNUNG: UNGÜLTIGE ENTWICKEREINSTELLUNG UIROTATEMODEGLOBAL!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}