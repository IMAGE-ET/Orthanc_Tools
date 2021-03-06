package org.petctviewer.orthanc.OTP;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.petctviewer.orthanc.anonymize.VueAnon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@SuppressWarnings("serial")
public class OTP_Gui extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField CTP_Username;
	private JPasswordField CTP_Password;
	private JTable tablePatient;
	private DefaultTableModel modelTablePatient;
	private JComboBox<String> comboBox_Studies, comboBox_Visits;
	private OTP otp;
	private JTable tableDetailsPatient;
	private Preferences jprefer = VueAnon.jprefer;
	
	//Selected patient
	private String patientAnonName;
	private String patientAnonID;
	private boolean ok=false;

	/**
	 * Create the dialog.
	 */
	public OTP_Gui(String CTPAddress) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			{
			}
		}
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBorder(new LineBorder(new Color(0, 0, 0)));
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new GridLayout(2, 1, 0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1);
				{
					JLabel lblNewLabel = new JLabel("Username");
					panel_1.add(lblNewLabel);
				}
				{
					CTP_Username = new JTextField();
					CTP_Username.setText(jprefer.get("CTPUsername", ""));
					panel_1.add(CTP_Username);
					CTP_Username.setColumns(10);
				}
				{
					JLabel lblPassword = new JLabel("Password");
					panel_1.add(lblPassword);
				}
				{
					CTP_Password = new JPasswordField(10);
					panel_1.add(CTP_Password);
					CTP_Password.setColumns(10);
				}
				{
					JButton btnConnect = new JButton("Connect");
					panel_1.add(btnConnect);
					JPanel panel_2 = new JPanel();
					panel.add(panel_2);
					panel_2.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1_1 = new JPanel();
						panel_2.add(panel_1_1, BorderLayout.CENTER);
						{
							JLabel lblAvailableStudy = new JLabel("Available Study");
							panel_1_1.add(lblAvailableStudy);
						}
						comboBox_Studies = new JComboBox<String>();
						
						panel_1_1.add(comboBox_Studies);
						{
							JLabel label = new JLabel("");
							panel_1_1.add(label);
						}
						{
							JLabel lblAvailableVisite = new JLabel("Available Visite");
							panel_1_1.add(lblAvailableVisite);
						}
						{
							comboBox_Visits = new JComboBox<String>();
							comboBox_Visits.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent arg0) {
									System.out.println(arg0.getSource());
									if(arg0.getStateChange() == ItemEvent.SELECTED && !comboBox_Visits.getSelectedItem().equals("None") ) {
										
										System.out.println("ici");
										JsonArray patients=otp.getAvailableImports((String) comboBox_Studies.getSelectedItem(), (String) comboBox_Visits.getSelectedItem());
										System.out.println(patients);
										if (tablePatient.getModel().getRowCount()>0) modelTablePatient.setRowCount(0);
										for (int i=0 ; i<patients.size() ; i++) {
											JsonObject patient=patients.get(i).getAsJsonObject();
											System.out.println(patient);
											String numeroPatient=patient.get("numeroPatient").getAsString();
											String firstName=patient.get("firstName").getAsString();
											String lastName=patient.get("lastName").getAsString();
											String patientSex=patient.get("patientSex").getAsString();
											String patientDOB=patient.get("patientDOB").getAsString();
											//String investigatorName=patient.get("investigatorName").getAsString();
											//String country=patient.get("country").getAsString();
											//String centerNumber=patient.get("centerNumber").getAsString();
											String acquisitionDate=patient.get("acquisitionDate").getAsString().replaceAll("-", "/");
											modelTablePatient.addRow(new Object[]{numeroPatient, lastName, firstName, patientSex , patientDOB, acquisitionDate});
											//SK MANQUE ACQUISITION DATE DANS L API
										}
									}
									
								}
							});
							panel_1_1.add(comboBox_Visits);
						}
						comboBox_Studies.addItemListener(new ItemListener() {
							
							public void itemStateChanged(ItemEvent arg0) {
								if(arg0.getStateChange() == ItemEvent.SELECTED && comboBox_Studies.getSelectedIndex() !=0 ) {
									System.out.println(comboBox_Studies.getSelectedIndex());
									
									comboBox_Visits.removeAllItems();
									if (tablePatient.getModel().getRowCount()>0) modelTablePatient.setRowCount(0);
									String[] visits=otp.getAvailableVisits((String) comboBox_Studies.getSelectedItem());
									if (visits !=null) {
										for (int i=0; i<visits.length; i++) {
											comboBox_Visits.addItem(visits[i]);
										}
									}
									else {
										comboBox_Visits.addItem("None");
									}
										
									
								}
									
							
								
							}
						});
					}
					btnConnect.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							jprefer.put("CTPUsername", CTP_Username.getText());
							comboBox_Studies.removeAllItems();
							comboBox_Visits.removeAllItems();
							if (tablePatient.getModel().getRowCount()>0) modelTablePatient.setRowCount(0);							
							otp=new OTP(CTP_Username.getText(), new String(CTP_Password.getPassword()), CTPAddress );
							
							boolean checklogin=otp.checkLogin();
							if(checklogin) {
								String[] studies=otp.getAvailableStudies();
								comboBox_Studies.addItem("Choose");
								for (int i=0; i<studies.length; i++) {
									comboBox_Studies.addItem(studies[i]);
								}
								
							}
							
							
							
						}
					});
				}
			}
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new LineBorder(new Color(0, 0, 0)));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.NORTH);
				{
					JLabel lblPatientAwaitingUpload = new JLabel("Matching Patient Awaiting Upload");
					panel_1.add(lblPatientAwaitingUpload);
				}
			}
			{
				JScrollPane scrollPane = new JScrollPane();
				panel.add(scrollPane);
				{
					tablePatient = new JTable();
					
					tablePatient.setPreferredScrollableViewportSize(new Dimension(300, 50));
					tablePatient.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					tablePatient.setModel(new DefaultTableModel(
						new Object[][] {
						},
						new String[] {
								"Patient Number", "Last Name", "First Name", "Sex", "DOB", "Acquisition Date" 
						}
					) {
						Class<?>[] columnTypes = new Class[] {
								String.class, String.class, String.class, String.class, String.class, String.class
						};
						public Class<?> getColumnClass(int columnIndex) {
							return columnTypes[columnIndex];
						}
					});
					modelTablePatient=(DefaultTableModel) tablePatient.getModel();
					tablePatient.getColumnModel().getColumn(1).setMinWidth(0);
					tablePatient.getColumnModel().getColumn(1).setMaxWidth(0);
					tablePatient.getColumnModel().getColumn(1).setWidth(0);
					tablePatient.getColumnModel().getColumn(1).setResizable(false);
					tablePatient.getColumnModel().getColumn(2).setMinWidth(0);
					tablePatient.getColumnModel().getColumn(2).setMaxWidth(0);
					tablePatient.getColumnModel().getColumn(2).setWidth(0);
					tablePatient.getColumnModel().getColumn(2).setResizable(false);
					tablePatient.getColumnModel().getColumn(3).setMinWidth(0);
					tablePatient.getColumnModel().getColumn(3).setMaxWidth(0);
					tablePatient.getColumnModel().getColumn(3).setWidth(0);
					tablePatient.getColumnModel().getColumn(3).setResizable(false);
					tablePatient.getColumnModel().getColumn(4).setMinWidth(0);
					tablePatient.getColumnModel().getColumn(4).setMaxWidth(0);
					tablePatient.getColumnModel().getColumn(4).setWidth(0);
					tablePatient.getColumnModel().getColumn(4).setResizable(false);
					tablePatient.getColumnModel().getColumn(5).setMaxWidth(0);
					tablePatient.getColumnModel().getColumn(5).setWidth(0);
					tablePatient.getColumnModel().getColumn(5).setWidth(0);
					tablePatient.getColumnModel().getColumn(5).setResizable(false);
					
					tablePatient.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
						
						@Override
						public void valueChanged(ListSelectionEvent arg0) {
							if (tablePatient.getSelectedRow() > -1) {
								//On affiche les donnees venant de la plateforme
								
								
								tableDetailsPatient.setValueAt( (String) tablePatient.getValueAt(tablePatient.getSelectedRow(),1) , 0, 2);
								tableDetailsPatient.setValueAt( (String) tablePatient.getValueAt(tablePatient.getSelectedRow(),2), 1, 2);
								tableDetailsPatient.setValueAt( (String) tablePatient.getValueAt(tablePatient.getSelectedRow(),3), 2, 2);
								tableDetailsPatient.setValueAt( (String) tablePatient.getValueAt(tablePatient.getSelectedRow(),4), 3, 2);
								tableDetailsPatient.setValueAt( (String) tablePatient.getValueAt(tablePatient.getSelectedRow(),5), 4, 2);
								
								//calcul la valeur du Match pour chaque parametre
								if(tableDetailsPatient.getValueAt(0, 1).toString().startsWith(tableDetailsPatient.getValueAt(0, 2).toString()) ) {
									tableDetailsPatient.setValueAt("Yes", 0, 3);
									}
								else {
									tableDetailsPatient.setValueAt("No", 0, 3);
									
								}
								
								if(tableDetailsPatient.getValueAt(1, 1).toString().startsWith(tableDetailsPatient.getValueAt(1,2).toString()) ) {
									tableDetailsPatient.setValueAt("Yes", 1, 3);
									
								}
								else {
									tableDetailsPatient.setValueAt("No", 1, 3);}
								}
								if(tableDetailsPatient.getValueAt(2, 2).toString().startsWith(tableDetailsPatient.getValueAt(2, 1).toString()) ) {
									tableDetailsPatient.setValueAt("Yes", 2, 3);
								}
								else {
									tableDetailsPatient.setValueAt("No", 2, 3);
								}
								
								//Date de naissance a gerer avec les ND...
								String[] dateLocale =tableDetailsPatient.getValueAt(3, 1).toString().split("/");
								String[] dateCTP =tableDetailsPatient.getValueAt(3, 2).toString().split("/");
								boolean matchDate=true;
								for (int i=0 ; i<3 ; i++) {
									if ( !dateCTP[i].equals("ND") && !dateLocale[i].equals(dateCTP[i]))  matchDate= false ;
								}
								if(matchDate) {
									tableDetailsPatient.setValueAt("Yes", 3, 3);
								}
								else {
									tableDetailsPatient.setValueAt("No", 3, 3);
								}
								
								if(tableDetailsPatient.getValueAt(4, 1).toString().equals(tableDetailsPatient.getValueAt(4, 2).toString()) ) {
									tableDetailsPatient.setValueAt("Yes", 4, 3);
								}
								else {
									tableDetailsPatient.setValueAt("No", 4, 3);
								}
								
							}
						
							
						
					});
					scrollPane.setViewportView(tablePatient);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.EAST);
				{
					tableDetailsPatient = new JTable();
					tableDetailsPatient.setEnabled(false);
					
					tableDetailsPatient.setModel(new DefaultTableModel(
						new String[][] {
							{"Last Name", "", "",""},
							{"First Name", "", "",""},
							{"Sex", "", "",""},
							{"Date Of Birth", "", "",""},
							{"Acquisition Date", "", "",""},
						},
						new String[] {
							"", "Local", "CTP", "Match"
						}
					));
					tableDetailsPatient.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
					
					
					
					
					//tableDetailsPatient.setEnabled(false);
					tableDetailsPatient.getColumnModel().getColumn(0).setPreferredWidth(100);
					
					{
						JScrollPane scrollPane = new JScrollPane();
						scrollPane.setViewportView(tableDetailsPatient);
						tableDetailsPatient.setPreferredScrollableViewportSize(new Dimension (250,100));
						panel_1.add(scrollPane);
					}
				}
				
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(tablePatient.getSelectedRowCount()==1 && checkMatch()) {
							patientAnonID=tablePatient.getValueAt(tablePatient.getSelectedRow(), 0).toString();
							patientAnonName=tablePatient.getValueAt(tablePatient.getSelectedRow(), 0).toString();
							ok=true;
							dispose();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void setStudyLocalValue(String patientName, String acquisitionDate, String sex, String DOB) {
		String[] name=null;
		//Split lastname / first name
		if(patientName.contains("^")) {
			name=patientName.split("\\^");
		}
		else {
			name=new String[]{ patientName, ""};
		}
		tableDetailsPatient.setValueAt( name[0] , 0, 1);
		tableDetailsPatient.setValueAt( name[1], 1, 1);
		tableDetailsPatient.setValueAt( sex, 2, 1);
		tableDetailsPatient.setValueAt( DOB, 3, 1);
		tableDetailsPatient.setValueAt( acquisitionDate, 4, 1);

	
	}
	
	public String getAnonName() {
		return this.patientAnonName;
	}
	
	public String getAnonID() {
		return this.patientAnonID;
	}
	
	public boolean getOk() {
		return this.ok;
	}
	
	public String getVisitName() {
		return comboBox_Visits.getSelectedItem().toString();
	}
	
	public String getLogin() {
		return CTP_Username.getText();
	}
	public String getPassword() {
		return new String(CTP_Password.getPassword());
	}
	
	public void setLogin(String login) {
		CTP_Username.setText(login);
	}
	public void setPassword(String password) {
		CTP_Password.setText(password);
	}
	
	public Object[] getOrthancServerReciever() {
		return new Object[] {otp.orthancAdress, otp.orthancPort, otp.orthancLogin, otp.orthancPassword};
	}
	
	private boolean checkMatch() {
		boolean check=true;
		int rowNb=tableDetailsPatient.getModel().getRowCount();
		String[] checkMessage= {"Last Name", "First Name", "Sex", "Date Of Birth", "Acquisition Date"};
		for(int i=0; i<rowNb; i++) {
			if(! tableDetailsPatient.getValueAt( i, 3).equals("Yes")) {
				check=false;
				
				int response=JOptionPane.showConfirmDialog(this, "<html>The <font color='red'>"+checkMessage[i]+"</font> don't not match with the expected patient, are you sure this patient is correct for this visit ?</html>", "Check Failed", JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
				if(response==JOptionPane.YES_OPTION) {
					check=true;
				}else {
					break;
				}
			};
			
		}
		
		
		return check;
		
	}
	
	//https://www.javaworld.com/article/2077430/core-java/set-the-jtable.html
	private class CustomTableCellRenderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent
	       (JTable table, Object value, boolean isSelected,
	       boolean hasFocus, int row, int column) {
	    	Component c = super.getTableCellRendererComponent
	        (table, value, isSelected, hasFocus, row, column);
	    	if(value instanceof String) {
	    		String valeur=(String) table.getValueAt(row, column);
	    		if(valeur.equals("Yes")) {
	    			c.setForeground(Color.GREEN);
				}
				else {
					c.setForeground(null);
				}
	
	    	}
	         
	        return c;
	    }
	}

}


