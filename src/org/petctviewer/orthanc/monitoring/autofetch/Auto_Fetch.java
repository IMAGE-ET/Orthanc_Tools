package org.petctviewer.orthanc.monitoring.autofetch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.petctviewer.orthanc.monitoring.Orthanc_Monitoring;
import org.petctviewer.orthanc.query.QueryRetrieve;
import org.petctviewer.orthanc.query.datastorage.StudyDetails;
import org.petctviewer.orthanc.setup.OrthancRestApis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Auto_Fetch {
	
	private OrthancRestApis connexion;
	private String level, studyDate, modality, studyDescription, queryAet , retrieveAet;
	private JsonParser parser=new JsonParser();
	private QueryRetrieve restApi;
	private JLabel status;
	
	//sert a etre arrete via methode
	private Timer timer;
	
	public Auto_Fetch(OrthancRestApis connexion, String level, String studyDate, String modality, String studyDescription, String queryAet, JLabel status) {
		this.connexion=connexion;
		restApi=new QueryRetrieve(connexion);
		this.level=level;
		this.studyDate=studyDate;
		this.studyDescription=studyDescription;
		this.queryAet=queryAet;
		this.modality=modality;
		retrieveAet= restApi.getLocalAet();
		this.status=status;
		
	}
	
	public void startAutoFetch() {
		Orthanc_Monitoring monitoring=new Orthanc_Monitoring(connexion);
		monitoring.autoSetChangeLastLine();
		
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				System.out.println("starting Auto-Fetch");
				monitoring.makeMonitor();
				List<String> idToProcess=new ArrayList<String>();
				//Si on monitore le level Study on parcours les study arrivees pour stocker les ID patients a retrieve
				if (level.equals("study")) {
					for (int i=0; i<monitoring.newStudyID.size(); i++) {
						StringBuilder sb = connexion.makeGetConnectionAndStringBuilder("/studies/"+monitoring.newStudyID.get(i));
					
						JsonObject study = parser.parse(sb.toString()).getAsJsonObject();
						JsonObject patientMainTag= study.get("PatientMainDicomTags").getAsJsonObject();
						idToProcess.add(patientMainTag.get("PatientID").getAsString());
					}
				}
				//Si on monitore le level Patients on parcours les patients arrivees pour stocker les ID patients a retrieve
				if (level.equals("patient")) {
					for (int i=0; i<monitoring.newPatientID.size(); i++) {
						StringBuilder sb = connexion.makeGetConnectionAndStringBuilder("/patients/"+monitoring.newPatientID.get(i));
						JsonObject patient = parser.parse(sb.toString()).getAsJsonObject();
						JsonObject patientMainTag=patient.get("MainDicomTags").getAsJsonObject();
						idToProcess.add(patientMainTag.get("PatientID").getAsString());
					}
					
				}
				//Pour chaque ID patient on lance le retrieve
				for (int i=0 ; i<idToProcess.size(); i++) {
					makeRetrieve(idToProcess.get(i));
				}
				
				//On vide pour les messages suivant
				idToProcess.clear();
				monitoring.clearAllList();
				
			}
			
		};
		
        //running timer task as daemon thread
        timer = new Timer(true);
        //Toutes les 90 seconds
        timer.scheduleAtFixedRate(timerTask, 0, (90*1000));
	}
	
	/**
	 * Retrieve en local toutes les reponse d'un ID patient avec le filtre (date, modality, description)
	 * @param patientID
	 * @throws IOException
	 */
	private void makeRetrieve(String patientID) {
		
		StudyDetails [] patients=restApi.getStudiesResults("Study", "*", patientID, studyDate, modality, studyDescription, "*", queryAet);
		
		int numberofAnswers=patients.length;
		
		for (int i=0 ; i<numberofAnswers ; i++) {
			try {
				restApi.retrieve(patients[i].getQueryID(), i, retrieveAet);
				status.setText("Retriving "+(i+1)+ "/"+ (numberofAnswers) );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		status.setText("Done, waiting");
		
		
	}
	
	
	public void stopAutoFecth() {
		timer.cancel();
	}

}
