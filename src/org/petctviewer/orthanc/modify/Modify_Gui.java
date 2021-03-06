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

package org.petctviewer.orthanc.modify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.orthanc.anonymize.VueAnon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.JSpinner;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.prefs.Preferences;
import java.awt.event.ActionEvent;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

@SuppressWarnings("serial")
public class Modify_Gui extends JDialog {

	private JPanel contentPane;
	private JPanel study_panel ;
	private JPanel serie_panel ;
	private JTable table_patient;
	private JPanel patient_panel ;
	private JTable table_study;
	private JTable table_serie;
	private JTable table_SharedTags;
	private JButton btnShowTags;
	private JSpinner spinner_instanceNumber;
	
	private JCheckBox chckbxRemovePrivateTags;
	private JCheckBox chckbxDeleteOriginalDicoms;
	
	private Modify modify;
	
	private VueAnon guiParent;
	
	private Preferences prefs=VueAnon.jprefer;
	
	//Build modification list Replace and Remove
	private JsonObject queryReplace=new JsonObject();
	private JsonArray queryRemove=new JsonArray();

	/**
	 * Make edition dialog box
	 * @param modify
	 * @param guiParent
	 * @param state
	 */
	public Modify_Gui(Modify modify, VueAnon guiParent) {
		super(guiParent, "Modify", true);
		this.modify=modify;
		this.guiParent=guiParent;
		makegui();
		
	}
	
	private void makegui() {
		setIconImage(new ImageIcon(ClassLoader.getSystemResource("logos/OrthancIcon.png")).getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel button_panel = new JPanel();
		FlowLayout fl_button_panel = (FlowLayout) button_panel.getLayout();
		fl_button_panel.setAlignment(FlowLayout.RIGHT);
		contentPane.add(button_panel, BorderLayout.SOUTH);
		
		JLabel label = new JLabel("");
		button_panel.add(label);
		
		chckbxRemovePrivateTags = new JCheckBox("Remove Private Tags");
		
		chckbxDeleteOriginalDicoms = new JCheckBox("Delete Original Dicoms");
		
		JButton btnModify = new JButton("Modify");
		
		btnModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
					@Override
					protected Void doInBackground() throws Exception {
						JsonObject query =modify.buildModifyQuery(queryReplace, queryRemove, chckbxRemovePrivateTags.isSelected());
						System.out.println(query);
						if (query !=null) {
							saveprefs();
							dispose();
							guiParent.setStateMessage("Modifying...", "red", -1);
							modify.sendQuery(query, chckbxDeleteOriginalDicoms.isSelected());
						}
						return null;
					}
					@Override
					protected void done() {
						try {
							get();
							guiParent.setStateMessage("Modified DICOM created", "green", -1);
							modify.refreshTable();
						} catch (Exception e) {
							guiParent.setStateMessage("Modification not allowed", "red", -1);
							e.printStackTrace();
						}
					}
				};
				
				worker.execute();
				
			}
		});
		
		button_panel.add(chckbxRemovePrivateTags);
		button_panel.add(chckbxDeleteOriginalDicoms);
		button_panel.add(btnModify);
		
		JButton btnCancel = new JButton("Cancel");
		
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		button_panel.add(btnCancel);
		
		JPanel center_panel = new JPanel();
		contentPane.add(center_panel, BorderLayout.CENTER);
		center_panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_tags = new JPanel();
		center_panel.add(panel_tags);
		panel_tags.setLayout(new GridLayout(0, 2, 0, 0));
		
		patient_panel = new JPanel();
		panel_tags.add(patient_panel);
		patient_panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_patient = new JScrollPane();
		patient_panel.add(scrollPane_patient);
		
		table_patient = new JTable();
		
		table_patient.setModel(new DefaultTableModel(new String[] {"Tag", "Value", "Remove"},0) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class,String.class, Boolean.class
			};
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column){  
		          if (column==0) return false;  else return true;
		     }
		});
		
		scrollPane_patient.setViewportView(table_patient);
		
		JLabel lblPatient = new JLabel("Patient");
		patient_panel.add(lblPatient, BorderLayout.NORTH);
		
		study_panel = new JPanel();
		panel_tags.add(study_panel);
		study_panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_study = new JScrollPane();
		study_panel.add(scrollPane_study, BorderLayout.CENTER);
		
		table_study = new JTable();
		
		table_study.setModel(new DefaultTableModel(new String[] {"Tag", "Value", "Remove"},0) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class, String.class, Boolean.class
			};
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column){  
		          if (column==0) return false;  else return true;
		     }
		});
		
		scrollPane_study.setViewportView(table_study);
		
		JLabel lblStudy = new JLabel("Study");
		study_panel.add(lblStudy, BorderLayout.NORTH);
		
		serie_panel = new JPanel();
		panel_tags.add(serie_panel);
		serie_panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_serie = new JScrollPane();
		serie_panel.add(scrollPane_serie, BorderLayout.CENTER);
		
		table_serie = new JTable();
		
		table_serie.setModel(new DefaultTableModel(new String[] {"Tag", "name", "Remove"},0) {
			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class, String.class, Boolean.class
			};
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column){  
		          if (column==0) return false;  else return true;
		     }
		});
		
		scrollPane_serie.setViewportView(table_serie);
		
		JLabel lblSerie = new JLabel("Serie");
		serie_panel.add(lblSerie, BorderLayout.NORTH);
		
		JPanel panel_others = new JPanel();
		panel_tags.add(panel_others);
		panel_others.setLayout(new BorderLayout(0, 0));
		
		JLabel lblOther = new JLabel("Other");
		panel_others.add(lblOther, BorderLayout.NORTH);
		
		JPanel panel_other = new JPanel();
		panel_others.add(panel_other, BorderLayout.CENTER);
		panel_other.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_showTags = new JPanel();
		panel_other.add(panel_showTags, BorderLayout.CENTER);
		panel_showTags.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_Instance = new JPanel();
		panel_showTags.add(panel_Instance, BorderLayout.NORTH);
		
		JButton btnSharedTags = new JButton("Get Shared Tags");
		panel_Instance.add(btnSharedTags);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_showTags.add(scrollPane);
		
		table_SharedTags = new JTable();
		table_SharedTags.setPreferredScrollableViewportSize(new Dimension(300, 100));
		
		DefaultTableModel table_customChange_model= new DefaultTableModel(new String[] {"Tag", "Name", "Value", "Delete"},0) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, Boolean.class
			};
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column){  
		          if (column==0 ||column==1 ) return false;  else return true;
		     }
		};
		table_SharedTags.setModel(table_customChange_model);
		scrollPane.setViewportView(table_SharedTags);
		
		btnSharedTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(
					    null,
					    "Editing Shared tags will discard changes in other tables, continue ?",
					    "Shared Tags",
					    JOptionPane.YES_NO_OPTION);
				
				if (answer==JOptionPane.OK_OPTION) {
					removeAllRow(table_SharedTags);				
					JsonObject response = modify.getSharedTags();
					
					Set<String> sharedTagsItems=response.keySet();
					for (String sharedTags:sharedTagsItems) {
						String address = (String) sharedTags;
						JsonObject response2 = response.get(sharedTags).getAsJsonObject();
						String tag = response2.get("Name").getAsString();
						String value = response2.get("Value").getAsString() ;
						
						table_customChange_model.addRow(new Object[] {address, tag, value, Boolean.FALSE});
					}
					// Unable all other table because risk of redundency
					hideTables("all");
					queryReplace=new JsonObject();
					queryRemove=new JsonArray();
					table_SharedTags.putClientProperty("terminateEditOnFocusLost", true);
					table_SharedTags.getModel().addTableModelListener(tablechangeListenerSharedTags);
					table_SharedTags.setAutoCreateRowSorter(true);
					table_SharedTags.getRowSorter().toggleSortOrder(0);
					btnSharedTags.setEnabled(false);
					
					}

			}
		});
		
		JPanel panel_otherButtons = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_otherButtons.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel_showTags.add(panel_otherButtons, BorderLayout.SOUTH);
		
		JLabel lblSeeTagInstance = new JLabel("See tag instance number");
		panel_otherButtons.add(lblSeeTagInstance);
		
		spinner_instanceNumber = new JSpinner();
		spinner_instanceNumber.setEnabled(false);
		spinner_instanceNumber.setToolTipText("only available for Serie level");
		spinner_instanceNumber.setModel(new SpinnerNumberModel(0, 0, 9999, 1));
		panel_otherButtons.add(spinner_instanceNumber);
		
		btnShowTags = new JButton("Show");
		btnShowTags.setEnabled(false);
		btnShowTags.setToolTipText("only available for Serie level");
		panel_otherButtons.add(btnShowTags);
		btnShowTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JsonObject instanceTags=modify.getInstanceTags((int) spinner_instanceNumber.getValue());
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				JsonParser jp = new JsonParser();
				JsonElement je = jp.parse(instanceTags.toString());
				String prettyJsonString = gson.toJson(je);
				
				JTextArea textArea = new JTextArea(prettyJsonString);
				JScrollPane scrollPane = new JScrollPane(textArea);  
				textArea.setLineWrap(true);  
				textArea.setWrapStyleWord(true); 
				scrollPane.setPreferredSize( new Dimension( 500, 500 ) );
				JOptionPane.showMessageDialog(null, scrollPane, "DICOM Tags", JOptionPane.INFORMATION_MESSAGE);
				
			}
		});
		
		loadprefs();

	}
	
	private void saveprefs() {
		prefs.putBoolean("Modify_DeleteOriginal", 	chckbxDeleteOriginalDicoms.isSelected() );
		prefs.putBoolean("Modify_RemovePrivateTags", chckbxRemovePrivateTags.isSelected() );
	}
	
	private void loadprefs() {
		chckbxDeleteOriginalDicoms.setSelected(prefs.getBoolean("Modify_DeleteOriginal", false));
		chckbxRemovePrivateTags.setSelected(prefs.getBoolean("Modify_RemovePrivateTags", false));
	}
	
	public void setTables(JsonObject MainTags, String level) {
		Set<String> mainPatientTags=MainTags.keySet();
		
		if (level.equals("patient")) {
			DefaultTableModel patientModel =(DefaultTableModel) table_patient.getModel();
			for (String mainPatientTag : mainPatientTags) {
				String tag=mainPatientTag;
				String value=MainTags.get(mainPatientTag).getAsString();
				patientModel.addRow(new Object[] {tag, value, Boolean.FALSE});
			}
			table_patient.putClientProperty("terminateEditOnFocusLost", true);
			//On ajoute le listener pour ecouter les changement de l'utilisateur
			addTableModelListener(table_patient);
		}
		
		else if (level.equals("study")) {
			DefaultTableModel studyModel =(DefaultTableModel) table_study.getModel();
			for (String mainPatientTag : mainPatientTags) {
				String tag=mainPatientTag;
				String value=MainTags.get(mainPatientTag).getAsString();
				studyModel.addRow(new Object[] {tag, value, Boolean.FALSE});
			}
			table_study.putClientProperty("terminateEditOnFocusLost", true);
			addTableModelListener(table_study);
			
		}
		
		else if (level.equals("serie")) {
			DefaultTableModel serieModel =(DefaultTableModel) table_serie.getModel();
			for (String mainPatientTag : mainPatientTags) {
				String tag=mainPatientTag;
				String value=MainTags.get(mainPatientTag).getAsString();
				serieModel.addRow(new Object[] {tag, value, Boolean.FALSE});
			}
			table_serie.putClientProperty("terminateEditOnFocusLost", true);
			addTableModelListener(table_serie);
			disableTable(table_patient);
			disableTable(table_study);
			btnShowTags.setEnabled(true);
			spinner_instanceNumber.setEnabled(true);
		}
		
	}
	
	private void disableTable(JTable table) {
		table.setEnabled(false);
		table.getColumnModel().getColumn(2).setMinWidth(0);
		table.getColumnModel().getColumn(2).setMaxWidth(0);
		table.setBackground(Color.LIGHT_GRAY);
	}
	
	public void hideTables(String level) {
		if (level.equals("patients")) {
			study_panel.setVisible(false);
			serie_panel.setVisible(false);
		} else if (level.equals("studies")) {
			serie_panel.setVisible(false);
		} else if (level.equals("all")) {
			removeAllRow(table_patient);
			removeAllRow(table_serie);
			removeAllRow(table_study);
		}
			
	}
	
	
	private void removeAllRow (JTable table) {
		DefaultTableModel model =(DefaultTableModel) table.getModel();
		model.setRowCount(0);
	}
	
	private void addTableModelListener(JTable table) {
		table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType()==TableModelEvent.UPDATE) {
					
					JsonElement tag=new JsonPrimitive( table.getValueAt(e.getFirstRow(), 0).toString() );
					// If item not to remove, add to replace list and remove if present in the remove list
					if (table.getValueAt(e.getFirstRow(), 2).equals(Boolean.FALSE)) {
						queryReplace.addProperty(tag.getAsString(), (String) table.getValueAt(e.getFirstRow(), 1));
						queryRemove.remove(tag);
					}
					//else add item to replace list and remove it from remove list
					else {
						queryRemove.add(tag);
						queryReplace.remove(tag.getAsString());
					}
				}
				
			}
	    });
		
	}
    
	/**
	 * Listener for the Shared Tag Table
	 */
    TableModelListener tablechangeListenerSharedTags =new TableModelListener() {
    	@Override
		public void tableChanged(TableModelEvent e) {
    		if (e.getType()==TableModelEvent.UPDATE) {
    			
    			int modifiedRow=table_SharedTags.convertRowIndexToView(e.getFirstRow());
    			JsonElement tag=new JsonPrimitive(table_SharedTags.getValueAt(modifiedRow, 1).toString() );
    			
	    		if (!(boolean) table_SharedTags.getValueAt(modifiedRow, 3)) {
	    			queryReplace.addProperty( tag.getAsString(), (String) table_SharedTags.getValueAt(modifiedRow, 2));
	    			queryRemove.remove(tag);
				}
				//else add item to replace list and remove it from remove list
				else {
					queryRemove.add(tag);
					queryReplace.remove(tag.getAsString());
				}
	    		
    		}
		}
    };

}
