/**
Copyright (C) 2017 VONGSALAT Anousone & KANOUN Salim

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.petctviewer.orthanc.setup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.petctviewer.orthanc.anonymize.VueAnon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Permet de recuperer l'adresse du serveur Orthanc a partir des settings de
 * BIdatabase ou des settings defini dans le settings panel des Orthanc Plugin
 * et permet de creer les connexion get et post
 * 
 * @author kanoun_s
 *
 */
public class OrthancRestApis {

	private Preferences jpreferPerso = VueAnon.jprefer;
	private String fullAddress;
	private String authentication;
	private String orthancVersion;
	private boolean versionHigher131;
	private boolean connected;
	private String localAETName;
	private JsonParser jsonParser = new JsonParser();
	private JsonParser parser = new JsonParser();

	private String protocol= "http";
	private String host = "localhost";
	private Integer port = 80;
	private String rootPath = "";

	public OrthancRestApis(String fullAddress) {
		if (fullAddress == null) {
			refreshServerAddress();
		} else {
			this.fullAddress = fullAddress;
			getSystemInformationsAndTest();
		}

	}

	public void refreshServerAddress() {

		int serverNum = jpreferPerso.getInt("currentOrthancServer", 1);
		String ip = jpreferPerso.get("ip" + serverNum, "http://localhost");
		String port = jpreferPerso.get("port" + serverNum, "8042");
		this.fullAddress = ip + ":" + port;

		try {
			URL url = new URL(ip);
			this.host = url.getHost();
			this.port = Integer.parseInt(port);
			this.protocol = url.getProtocol();
			this.rootPath = url.getFile();
		} catch (Exception e) {
			System.out.println("Error Parsing URL");
		}
		
		if(jpreferPerso.get("username"+serverNum, null) != null && jpreferPerso.get("password"+serverNum, null) != null){
			authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username"+serverNum, null) + ":" + jpreferPerso.get("password"+serverNum, null)).getBytes());
		}
		
		getSystemInformationsAndTest();
		
	}
	
	
	private HttpURLConnection makeGetConnection(String apiUrl) throws Exception {
		
		HttpURLConnection conn=null;
		// URL url = new URL(fullAddress+apiUrl);
		URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		if((fullAddress != null && fullAddress.contains("https"))){
				HttpsTrustModifier.Trust(conn);
		}
		if(authentication != null){
			conn.setRequestProperty("Authorization", "Basic " + authentication);
		}
		conn.getResponseMessage();


		return conn;
	
	}
	
	
	
	private HttpURLConnection makeGetConnectionImage(String apiUrl) {
		
		HttpURLConnection conn=null;
		try {
			// URL url = new URL(fullAddress+apiUrl);
			URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Accept", "image/png");
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			if((fullAddress != null && fullAddress.contains("https"))){
					HttpsTrustModifier.Trust(conn);
			}
			if(authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + authentication);
			}
			conn.getResponseMessage();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return conn;
	
	}

	public StringBuilder makeGetConnectionAndStringBuilder(String apiUrl) {
		
		StringBuilder sb = null ;
		try {
			sb = new StringBuilder();
			HttpURLConnection conn = makeGetConnection(apiUrl);
			if (conn !=null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
				String output;
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
				conn.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return sb;
	}

	public HttpURLConnection makePostConnection(String apiUrl, String post) throws Exception {

		HttpURLConnection conn = null ;
			URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			if((fullAddress != null && fullAddress.contains("https")) ){
					HttpsTrustModifier.Trust(conn);
			}
			if(this.authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + this.authentication);
			}
			OutputStream os = conn.getOutputStream();
			os.write(post.getBytes());
			os.flush();
			conn.getResponseMessage();
		
		return conn;
	}
	
	public HttpURLConnection makePutConnection(String apiUrl, String post) throws Exception {

		HttpURLConnection conn = null ;
			URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("PUT");
			if((fullAddress != null && fullAddress.contains("https")) ){
					HttpsTrustModifier.Trust(conn);
			}
			if(this.authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + this.authentication);
			}
			OutputStream os = conn.getOutputStream();
			os.write(post.getBytes());
			os.flush();
			conn.getResponseMessage();
		
		return conn;
	}

	public HttpURLConnection sendDicom(String apiUrl, byte[] post) {
		
		HttpURLConnection conn =null;
		
		try {
			URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			if((fullAddress != null && fullAddress.contains("https")) ){
				HttpsTrustModifier.Trust(conn);
				
			}
			if(this.authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + this.authentication);
			}
			conn.setRequestProperty("content-length", Integer.toString(post.length));
			conn.setRequestProperty("content-type", "application/dicom");
			OutputStream os = conn.getOutputStream();
			os.write(post);
			os.flush();
			conn.getResponseMessage();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return conn;
	}
		
	public StringBuilder makePostConnectionAndStringBuilder(String apiUrl, String post) {
		
		StringBuilder sb =null;
		try {
			sb=new StringBuilder();
			HttpURLConnection conn = makePostConnection(apiUrl, post);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			// We get the study ID at the end
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			conn.disconnect();
			conn.getResponseMessage();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sb; 
	}
	
	public StringBuilder makePutConnectionAndStringBuilder(String apiUrl, String post) {
		
		StringBuilder sb =null;
		try {
			sb=new StringBuilder();
			HttpURLConnection conn = makePutConnection(apiUrl, post);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			// We get the study ID at the end
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			conn.disconnect();
			conn.getResponseMessage();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sb; 
	}

	public InputStream openImage(String apiUrl) {
		
		InputStream is=null;
		try {
			HttpURLConnection conn = this.makeGetConnectionImage(apiUrl);
			is = conn.getInputStream();
		} catch(Exception e) { 
			e.printStackTrace();
		}

		return is;
	}
	
	public boolean makeDeleteConnection(String apiUrl) {
		try {
			URL url = new URL(this.protocol, this.host, this.port, this.rootPath+apiUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("DELETE");
			if((fullAddress != null && fullAddress.contains("https")) ){		
					HttpsTrustModifier.Trust(conn);
			}
			if(this.authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + this.authentication);
			}
			
			conn.getResponseMessage();
			conn.disconnect();
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
	
	}
	
	public void getSystemInformationsAndTest() {
		
		StringBuilder sb= makeGetConnectionAndStringBuilder("/system");
		try {
			JsonParser parser=new JsonParser();
			JsonObject systemJson=(JsonObject) parser.parse(sb.toString());
			orthancVersion=systemJson.get("Version").getAsString();
			localAETName=systemJson.get("DicomAet").getAsString();
			versionHigher131=isVersionAfter131();
			connected=true;	
		}catch(Exception e) {
			System.out.print(e);
			connected=false;
			JOptionPane.showMessageDialog(null, "Orthanc Unreachable", "No Reachable", JOptionPane.ERROR_MESSAGE);
		}
		
		
	}
	
	/**
	 * Test if version is higher than 1.3.1
	 * @return
	 */
	private boolean isVersionAfter131() {
		if(orthancVersion.equals("mainline")) return true;
		int test=versionCompare(orthancVersion, "1.3.1");
		if (test>0) return true; else return false;
		
	}
	/**
	 * Get the boolean test version
	 * @return
	 */
	public boolean getIfVersionAfter131() {
		return versionHigher131;
	}
	
	/**
	 * Compares two version strings. 
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical 
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1 a string of ordinal numbers separated by decimal points. 
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
	 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	private static int versionCompare(String str1, String str2) {
	    String[] vals1 = str1.split("\\.");
	    String[] vals2 = str2.split("\\.");
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
	      i++;
	    }
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length) {
	        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
	        return Integer.signum(diff);
	    }
	    // the strings are equal or one string is a substring of the other
	    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
	    return Integer.signum(vals1.length - vals2.length);
	}
	
	public void restartOrthanc() {
		StringBuilder sb=makePostConnectionAndStringBuilder("/tools/reset", "");
		if (sb !=null) {
			JOptionPane.showMessageDialog(null,"Server Sucessfully restarted");
		}

	}
	
	/**
	 * Retrun available AETs in Orthanc in Array
	 * @return
	 */
	public String[] getAET() {
		StringBuilder answers=makeGetConnectionAndStringBuilder("/modalities");
		String[] aets= {"None"};
		if(answers !=null) {
			JsonArray aetsAnswer = (JsonArray) jsonParser.parse(answers.toString());
			
			// Prepare the string Array that will contains available AETs
			aets= new String[aetsAnswer.size()];
			
			for(int i=0; i<aetsAnswer.size(); i++){
				aets[i]=aetsAnswer.get(i).getAsString();
			}
			
		}
		return aets;
	}
	
	public String getLocalAET() {
		return this.localAETName;
	}
	
	/**
	 * Return available Peers in Orthanc in Array
	 * @return
	 */
	public String[] getPeers() {
		StringBuilder answers=makeGetConnectionAndStringBuilder("/peers");
		String[] peers= {"None"};
		if(answers !=null) {
			JsonArray peersAnswer = (JsonArray) jsonParser.parse(answers.toString());
			// Prepare the string Array that will contains available Peers
			peers= new String[peersAnswer.size()];
			
			for(int i=0; i<peersAnswer.size(); i++){
				peers[i]=peersAnswer.get(i).getAsString();
			}
		}
		return peers;
	}
	
	
	//SK A TESTER
	public boolean sendToAet(String aet, ArrayList<String> idList) {

		JsonArray ids=new JsonArray();
		for(int i = 0; i < idList.size(); i++){
			ids.add(idList.get(i));
		}
		StringBuilder sb=makePostConnectionAndStringBuilder("/modalities/" + aet + "/store", ids.toString());
		//SK A TESTER DANS UN VRAI RESEAU POUR VOIR SI IL Y A UNE REPONSE
		System.out.println(sb);
		if(sb!=null) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/**
	 * Send Orthanc ressources IDs to Peer, return true if transfert success
	 * @param peer
	 * @param idList
	 * @return
	 */
	public boolean sendToPeer(String peer, ArrayList<String> idList) {
		JsonArray ids=new JsonArray();
		for(int i = 0; i < idList.size(); i++){
			ids.add(idList.get(i));
		}
		StringBuilder sb=makePostConnectionAndStringBuilder("/peers/" + peer + "/store", ids.toString());
		
		JsonObject answer=(JsonObject) parser.parse(sb.toString());
		int failed=answer.get("FailedInstancesCount").getAsInt();
		if(failed==0) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Send the data through transfer accelerator and return the JobID to monitor progress
	 * in /jobs/ID
	 * @param peer
	 * @param idList
	 * @return
	 */
	public String sendStudiesToPeerAccelerator(String peer, ArrayList<String> idList) {
		JsonObject request=new JsonObject();
		request.addProperty("Compression", "gzip");
		request.addProperty("Peer", peer);
		
		JsonArray ressources=new JsonArray();
		for(String id:idList) {
			JsonObject studyToSend=new JsonObject();
			studyToSend.addProperty("Level", "Study");
			studyToSend.addProperty("ID", id);
			ressources.add(studyToSend);
		}
		request.add("Resources", ressources);
		System.out.println(request);
		StringBuilder sb=makePostConnectionAndStringBuilder("/transfers/send/", request.toString());
		
		JsonObject answer=this.parser.parse(sb.toString()).getAsJsonObject();
		String jobId=answer.get("ID").getAsString();
		
		return jobId;
		
	}
	
	public void addPeerOtp(Object[] peer) {
		JsonObject jsonPeer=new JsonObject();
		if(((String) peer[0]).endsWith("/")) {
			peer[0]=((String) peer[0]).substring(0, ((String) peer[0]).length());
		}
		jsonPeer.addProperty("Url", peer[0]+":"+peer[1]);
		jsonPeer.addProperty("Username", (String) peer[2]);
		jsonPeer.addProperty("Password", (String) peer[3]);
		
		try {
			this.makePutConnection("/peers/otp", jsonPeer.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void removePeerOtp() {
		this.makeDeleteConnection("/peers/toto");
	}
	
	public static void main(String[] arg) {
		OrthancRestApis apis=new OrthancRestApis(null);
		ArrayList<String> idList = new ArrayList<String>();
		idList.add("96ab96e0-d7df8f2f-3b50ae6d-55fb9aef-42732200");
		apis.sendStudiesToPeerAccelerator("NewCTP", idList);
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	

	
}
