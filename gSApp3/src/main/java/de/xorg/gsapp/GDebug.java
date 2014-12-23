package de.xorg.gsapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class GDebug {

	public static boolean doLogWrite(String text, String logid) {
		try {
			File myFile = new File("/sdcard/" + logid + ".log");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
									new OutputStreamWriter(fOut);
			myOutWriter.append(text);
			myOutWriter.close();
			fOut.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
