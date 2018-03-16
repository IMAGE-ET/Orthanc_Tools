/**
Copyright (C) 2017 KANOUN Salim

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

package org.petctviewer.orthanc.query;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;



public class AutoQuery  {

	protected Rest api;
	protected Object[] aet;
	protected Object[] aetRetrieve;
	private Preferences jPrefer;
	
	protected long fONCE_PER_DAY=1000*60*60*24;
	protected int fTEN_PM=22;
	protected int fZERO_MINUTES=00;
	protected int discard=10;
	private DateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	
	public AutoQuery(Rest rest) {
		api=rest;
		try {
			aet=api.getAET();
			aetRetrieve=api.getLocalAET();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Get Jprefer Value
		jPrefer = Preferences.userNodeForPackage(AutoQuery.class);
		jPrefer = jPrefer.node("AutoQuery");
		discard=jPrefer.getInt("discard", 10);
		fTEN_PM=jPrefer.getInt("hour", 22);
		fZERO_MINUTES=jPrefer.getInt("minutes", 00);
		
	}
	/***
	 * Send a Query to a remote AET
	 * @param name
	 * @param id
	 * @param dateFrom
	 * @param dateTo
	 * @param modality
	 * @param studyDescription
	 * @param accessionNumber
	 * @param aet
	 * @return
	 * @throws IOException
	 */
	public String[] sendQuery(String name, String id, String dateFrom, String dateTo, String modality, String studyDescription, String accessionNumber, String aet) throws IOException {
		String[] results=null;
		try {
			//Selectionne l'AET de query
			//On format les date pour avoir la bonne string
			Date From=null;
			Date To=null;
			String date=null;
			
			if (StringUtils.equals(dateFrom, "*")==false) {From=df.parse(dateFrom);};
			if (StringUtils.equals(dateTo, "*")==false) {To=df.parse(dateTo);};
			
			if (From!=null && To!=null) date=df.format(From)+"-"+df.format(To);
			if (From==null && To!=null) date="-"+df.format(To);
			if (From!=null && To==null) date=df.format(From)+"-";
			if (From==null && To==null) date="*";getClass();
			
			//On lance la query
			if (StringUtils.equals(name, "*")==false|| StringUtils.equals(id, "*")==false ||StringUtils.equals(dateFrom, "*")==false || StringUtils.equals(dateTo, "*")==false || StringUtils.equals(modality, "*")==false|| StringUtils.equals(studyDescription, "*")==false|| StringUtils.equals(accessionNumber, "*")==false) {
				results=api.getQueryAnswerIndexes("Study", name , id, date, modality, studyDescription , accessionNumber, aet);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return results;
		
	}
	/**
	 * Retrive all results if lower than discard value
	 * @param results
	 * @param aetRetrieve
	 * @param discard
	 * @throws IOException
	 */
	public void retrieveQuery(String[] results, String aetRetrieve, int discard) throws IOException {
		if (results.length<=discard){		
			for (int i=0; i<Integer.valueOf(results[1]); i++) {
			api.retrieve(results[0], String.valueOf(i), aetRetrieve );
			}
		}
		else {
			System.out.println("Retrieve discarted because Query answers over discard limit");
		}
	}
	
	
	
	
	/**
	 * Date programmation
	 * @return
	 */
	protected Date tenPM() {
		Calendar today =new GregorianCalendar();
		Calendar tenPM =new GregorianCalendar(
		today.get(Calendar.YEAR),
		today.get(Calendar.MONTH),
		today.get(Calendar.DAY_OF_MONTH),
		fTEN_PM,
		fZERO_MINUTES
		);
		return tenPM.getTime();
	}
	
	/**
	 * CSV reader and inject value in table
	 * @param file
	 * @param table
	 * @throws IOException
	 */
	protected void csvReading(File file, JTable table) throws IOException {
  	  CSVFormat csvFileFormat = CSVFormat.EXCEL.withFirstRecordAsHeader().withIgnoreEmptyLines();
  	  CSVParser csvParser=CSVParser.parse(file, StandardCharsets.UTF_8,  csvFileFormat);
  	  //On met les records dans une list
  	  List<CSVRecord> csvRecord=csvParser.getRecords();
  	  // On balaie le fichier ligne par ligne
  	  for (int i=0 ; i<csvRecord.size(); i++) {
  		  //On recupere les variables
  		  String name=csvRecord.get(i).get(0);
  		  String prenom=csvRecord.get(i).get(1);
  		  String id=csvRecord.get(i).get(2);
  		  String accession=csvRecord.get(i).get(3);
  		  String dateFrom=csvRecord.get(i).get(4);
  		  String dateTo=csvRecord.get(i).get(5);
  		  String modality=csvRecord.get(i).get(6);
  		  String studyDescription=csvRecord.get(i).get(7);
  		  //On les pousse dans le tableau
  		  DefaultTableModel model = (DefaultTableModel) table.getModel();
  		  model.addRow(new Object[] {name, prenom, id, accession,dateFrom, dateTo, modality, studyDescription });
  		  }
  	  }
	/**
	 * Get content of a result.
	 * @param results
	 * @param path
	 * @throws IOException
	 * @throws ParseException 
	 */
	protected void getContent(String[] results, ArrayList<Patient> patientArray) throws IOException, ParseException {
		DateFormat parser = new SimpleDateFormat("yyyyMMdd");
		
		for (int i=0; i<Integer.parseInt(results[1]); i++) {
			String name = (String)api.getValue(api.getIndexContent(results[0],i), "PatientName");
			String id = (String)api.getValue(api.getIndexContent(results[0],i), "PatientID");
			String accNumber = (String)api.getValue(api.getIndexContent(results[0],i), "AccessionNumber");
			Date date = parser.parse((String)api.getValue(api.getIndexContent(results[0],i), "StudyDate"));
			String studyDesc = (String)api.getValue(api.getIndexContent(results[0],i), "StudyDescription");
			String modality = (String)api.getValue(api.getIndexContent(results[0],i), "ModalitiesInStudy");
			String studyUID = (String)api.getValue(api.getIndexContent(results[0],i), "StudyInstanceUID");
			
			Patient patient=new Patient(name, id, date, studyDesc, accNumber, studyUID, modality);
			patientArray.add(patient);
			//buildCSV(name,id,accNumber,date,modality,studyDesc,csv);
		}
		
		
	}
	
	
	
	


}
