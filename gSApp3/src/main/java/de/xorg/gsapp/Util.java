package de.xorg.gsapp;


import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
 
public class Util {
 
    public static String getVersionID(Context context){
        String ID = context.getString(R.string.version);
        String[] UID = ID.split(" ");
        return UID[0];
    }

    public static String getVersion(Context context) {
        String VER = context.getString(R.string.version);
        String[] VRS = VER.split(" ");
        if(BuildConfig.DEBUG) {
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
    
    public static void setTranscluent(Activity activity, Boolean BeanUI) {
    	if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
    		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
    		if(tintManager != null) {
    			tintManager.setNavigationBarTintEnabled(true);
    		    tintManager.setStatusBarTintEnabled(true);
    		    if(BeanUI) {
    		    	tintManager.setTintColor(Color.parseColor("#262626"));
    		    	tintManager.setNavigationBarTintColor(Color.parseColor("#000000"));
    		    } else {
    		    	tintManager.setTintColor(Color.parseColor("#FED21B"));
    		    }
    		}
    	}
    }
}