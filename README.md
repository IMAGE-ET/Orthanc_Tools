# Orthanc_Tools
DICOM tools built on Orthanc API in Java

Disclaimer : This project is in Maintenance Only.
This project will be replaced by Orthanc Tools JS which provide similar functionalities in a web oriented application, see https://github.com/salimkanoun/Orthanc-Tools-JS

Detailled Documentation : https://github.com/salimkanoun/Orthanc_Tools/blob/master/Orthanc_Tools_Documentation.pdf

YouTube demos : https://www.youtube.com/watch?v=cCvDfanQWCw&list=PLlWfh5HNr8mIK3sAe03qY8ynS569sHnGm

Features : 

- Anonymization : Fine tunable anonymization and sharing services of Anonymized DICOMs (FTP/SSH/WebDAV)

- Modification : Edit DICOM tags in Patient / Study / Serie levels

- Export : 
   - Zip Export of DICOMs stored in Orthanc (Hierachical or DICOMDIR)
   - CD/DVD image generation with patient's DICOM and ImageJ viewer (zip or ISO)
   
 - Manage : 
   - Single and Batch deletion of Patients / Studies / Series in Orthanc
   
 - Query : 
   - Query / Retrieve from remote AET
   - Automatic / Batch retrieve of studies (with Schedule feature)
      - Possibility to make series based filters for selective auto - retrieve
      - CSV report of auto-retrieve procedure
   
 - Import :
   - Recursive import to Orthanc of local DICOMs
   
 - Monitoring :
   - Auto-Fetch : Automatically retrieve patient's history for each new study/patient recieved in Orthanc
   - CD-Burner : Trigger DVD burning for Epson and Primera discproducers from DICOM transfert
   - Tag-Monitoring : Autocollection of DICOM tag value of recieved patients/studies/series (monitoring injected dose, patient's weight,        acquisition time ...) and possibility to store these data into a structured mySQL database
   - Auto-Routing (Not desgigned yet)
   
 - Orthanc JSON Editor :  
   - Edit all Orthanc settings in a comprehensive GUI and generate JSON settings file for Orthanc Server
   
 - Read : Load and visualize DICOM (better to use with Fiji version for image interaction)
   
 Contribution from http://petctviewer.org, free and open source PET/CT viewer based on Fiji/ImageJ
 
 GPL v.3 Licence
 
 Salim Kanoun & Anousone Vongsalat
 
