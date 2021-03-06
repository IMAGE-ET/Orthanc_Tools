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

package org.petctviewer.orthanc.anonymize;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import org.petctviewer.orthanc.anonymize.datastorage.PatientAnon;
import org.petctviewer.orthanc.anonymize.datastorage.Study2;

public class TableAnonPatientsModel extends DefaultTableModel{
	private static final long serialVersionUID = 1L;

	private String[] entetes = {"Old name", "Old ID", "Old OrthancId", "New name*", "New ID*", "New OrthancId", "patientAnonObject"};
	private Class<?>[] classEntetes = {String.class, String.class, String.class, String.class, String.class, String.class, PatientAnon.class};
	
	public TableAnonPatientsModel(){
		super(0,7);
	}


	@Override
	public String getColumnName(int columnIndex){
		return entetes[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int column){
		return classEntetes[column];
	}

	public boolean isCellEditable(int row, int col){
		if(col == 3 || col == 4){
			return true; 
		}
		return false;
	}

	
	public void removePatient(int rowIndex){
		this.removeRow(rowIndex);
	}

	public ArrayList<PatientAnon> getPatientList(){
		ArrayList<PatientAnon> patients=new ArrayList<PatientAnon>();
		
		for(int i=0; i<this.getRowCount(); i++) {
			PatientAnon patient=(PatientAnon) getValueAt(i, 6);
			patients.add(patient);
		}
		
		return patients;
	}
	
	public PatientAnon getPatient(int rowIndex){
		return (PatientAnon) this.getValueAt(rowIndex, 6);
	}
	
	private int searchPatientOrthancIdRow(String patientOrthancId) {
		for(int i=0; i<this.getRowCount(); i++) {
			if(this.getValueAt(i, 2).equals(patientOrthancId)) {
				return i;
			}
		}
		return -1;
		
	}
	
	/*
	 * This method adds patient to the patients list, which will eventually be used by the JTable
	 */
	public void addPatient(PatientAnon patient) {
		
		int patientExistingRow=searchPatientOrthancIdRow(patient.getPatientOrthancId());
		if(patientExistingRow==-1) {
			//"Old name", "Old ID", "Old OrthancId", "New name*", "New ID*", "New OrthancId", "patientAnonObject"
			this.addRow(new Object[]{patient.getName(), patient.getPatientId(), patient.getPatientOrthancId(),
				"","","",patient});
		}else {
			this.getPatient(patientExistingRow).addStudies(patient.getStudies());
			this.getPatient(patientExistingRow).addAllChildStudiesToAnonymizeList();
		}
	}
		
	public void addStudy(Study2 study) {
		DateFormat df=new SimpleDateFormat("yyyyMMdd");
		int patientExistingRow=searchPatientOrthancIdRow(study.getParentPatientId());
		
		//If not existing patient, create a new patientAnonObject and add the selected study in it
		if(patientExistingRow==-1) {
			PatientAnon patientAnon=new PatientAnon (study.getPatientName(), study.getPatientID(),df.format(study.getPatientDob()), study.getPatientSex(), study.getParentPatientId());
			patientAnon.addStudy(study);
			patientAnon.addNewAnonymizeStudyFromExistingStudy(study.getOrthancId());
			this.addRow(new Object[]{study.getPatientName(), study.getPatientID(), study.getParentPatientId(),
				"","","", patientAnon});
		//If existing patient retrieve the PatientAnon object and Add the selected study in it
		}else {
			if(getPatient(patientExistingRow).getChildStudy(study.getOrthancId())== null) {
				this.getPatient(patientExistingRow).addStudy(study);
			}
			
			this.getPatient(patientExistingRow).addNewAnonymizeStudyFromExistingStudy(study.getOrthancId());
			//modelAnonStudies.add
		}
		
	}

	/*
	 * This method clears the patients list
	 */
	public void clear(){
		this.setRowCount(0);
	}
}
