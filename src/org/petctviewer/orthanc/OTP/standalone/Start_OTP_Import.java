package org.petctviewer.orthanc.OTP.standalone;

import org.petctviewer.orthanc.anonymize.VueAnon;

public class Start_OTP_Import   {

	public static void main(String[] args) {
		//System.setProperty("java.net.useSystemProxies", "true");
		//VueAnon.jprefer=Preferences.systemNodeForPackage(Start_OTP_Import.class);
		VueAnon anon=new VueAnon("OrthancCTP.json");
		anon.setLocationRelativeTo(null);
		anon.exportTabForOtp();
		anon.setCTPaddress("https://petctviewer.com");
		anon.setVisible(true);
		
		
	}

	

}


