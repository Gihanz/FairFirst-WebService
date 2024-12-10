package com.tl.ws;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.security.auth.Subject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.json.JSONArray;
import org.apache.commons.json.JSONException;
import org.apache.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.property.Property;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

@RestController
public class WsServiceImpl extends BaseWebServiceEndPointImpl {

	static Logger log = LoggerFactory.getLogger(WsServiceImpl.class);

	@RequestMapping(value = "/test")
	public String testService() {
		return "Service working fine";
	}

	@RequestMapping(value = "/createCase", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
	public String createCase(@RequestBody JSONObject caseParameters) {

		log.info("In CreateCase Method");
		String response;
		log.info("CaseParameters : " + caseParameters);
		Date x=new Date();
		int year = x.getYear();
		int month = x.getMonth();	
		int date = x.getDate();
		int hrs = x.getHours();
		int min = x.getMinutes();
		int sec = x.getSeconds();

		String referenceNumber = (year+""+Date.UTC(year, month, date, hrs, min, sec)).substring(0, 13);
		System.out.println("Reference Number = "+referenceNumber);

		log.info("Reference Number = "+referenceNumber);
		try {

			CreateCase createCase = new CreateCase();
			createCase.createCase(caseParameters, referenceNumber, "FFC_Claims", "Fair First Claims");
			response = referenceNumber;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage() + "" + e);
			response = e.getMessage() + " : " + e;
		} 
		return response;	
	}

	@RequestMapping(value = "/uploadDoc", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public boolean uploadDoc(@RequestBody JSONObject docDataJson) throws IOException {
		
		log.info("In UploadDoc Method");
		boolean uploadFlag = false;
		UploadDoc ud = new UploadDoc();
		
		try {			
			String refNo = docDataJson.get("refNo").toString();
			log.info("refNo :" + refNo);	
			
			org.apache.commons.json.JSONArray docData = docDataJson.getJSONArray("Doc");
						
			for(int x=0;x<docData.length();x++) {
				JSONObject Doc = docData.getJSONObject(x);
				String docName = Doc.get("DocName").toString();				
				InputStream myInputStream = IOUtils.toInputStream(Doc.get("DocByteArr").toString());
				log.info("Doc Name "+docName);
				log.info("InputStream "+Doc.get("DocByteArr").toString());
				
			    RetrieveCEFolder cef = new RetrieveCEFolder();
			    Folder ceFolder = cef.retrieveCEFolder(refNo);
				ud.uploadDoc(ceFolder, docName, "text/plain" ,myInputStream);
				uploadFlag = true;
			}
			
		} catch (JSONException e1) {
			e1.printStackTrace();
			uploadFlag = false;
		} 

		return uploadFlag;
	}

	@RequestMapping(value = "/createCaseWithFiles", method = RequestMethod.POST)
	public String createCaseWithFiles(@RequestParam("caseParameters") JSONObject caseParameters, @RequestParam("myFiles") MultipartFile[] files) throws IOException {
		
		log.info("In CreateCaseWithFiles Method");
		String response;
		Date d = new Date();
		int year = d.getYear();
		int month = d.getMonth();	
		int date = d.getDate();
		int hrs = d.getHours();
		int min = d.getMinutes();
		int sec = d.getSeconds();

		String referenceNumber = (year+""+Date.UTC(year, month, date, hrs, min, sec)).substring(0, 13);
		System.out.println("Reference Number = "+referenceNumber);

		log.info("Reference Number = "+referenceNumber);
		try {

			CreateCase createCase = new CreateCase();
			log.info("CaseParameters : " + caseParameters);
			createCase.createCase(caseParameters, referenceNumber, "FFC_Claims", "Fair First Claims");
			
			RetrieveCEFolder cef = new RetrieveCEFolder();
		    Folder ceFolder = cef.retrieveCEFolder(referenceNumber);
			
			UploadDoc ud = new UploadDoc();

			log.info("files.length : "+files.length);
			
			for(int x=0; x<files.length; x++) {
				String docName = files[x].getOriginalFilename();			
				String extension = files[x].getContentType();
				InputStream myInputStream = files[x].getInputStream();
			    log.info("docName : "+docName+", extension : "+extension);
				ud.uploadDoc(ceFolder, docName, extension ,myInputStream);
			}
			
			response = referenceNumber;
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage() + " : " + e);
			response = e.getMessage() + " : " + e;
		} 
		return response;
	}
	
	@RequestMapping(value = "/recentCases", method = RequestMethod.GET, produces = "application/json")
	public JSONArray recentCases() {
		
		log.info("In RecentCases Method");
		JSONArray responseArr = new JSONArray();
		
		System.setProperty("java.security.auth.login.config", "/opt/IBM/BOC/jaas.conf.WSI");
		Connection connection = Factory.Connection.getConnection("http://172.25.100.120:9080/wsi/FNCEWS40MTOM/");
		Subject sub = UserContext.createSubject(connection, "p8admin","test@1234","FileNetP8WSI");
		UserContext.get().pushSubject(sub);
		
		Domain domain = Factory.Domain.getInstance(connection, null);
		ObjectStore objStore = Factory.ObjectStore.fetchInstance(domain, "CMTOS",null);
		SearchScope searchScope = new SearchScope(objStore);

		String sqlStr = "SELECT TOP 5 [FolderName], [ClassDescription], [DateCreated], [FFC_Amount], [FFC_CardNumber], [FFC_CompanyName], [FFC_ConditionleadtoAdmission], [FFC_EPFNumber], [FFC_FullName], [FFC_JobTitle], [FFC_MobileNumber], [FFC_PolicyNumber], [FFC_ReferenceNumber], [FFC_ResidenceTelephone] FROM [FFC_Claims] ORDER BY DateCreated DESC";
		SearchSQL searchSQL = new SearchSQL(sqlStr);
		log.info("Query = "+sqlStr);
		IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSQL, new Integer(10), null, new Boolean(true)); 
		
		if(!(independentObjectSet.isEmpty())){
			
			try{
				Iterator it = independentObjectSet.iterator();		
				while(it.hasNext())
				{
					Folder caseFolder = (Folder)it.next();
					log.info("Retrieved Case Folder = "+caseFolder.get_FolderName());
					Properties caseProperties = caseFolder.getProperties();
					
					JSONObject propertyMap= new JSONObject();
					propertyMap.put("ReferenceNumber", caseProperties.getStringValue("FFC_ReferenceNumber"));
					
					DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");  
	                String dateCreated = dateFormat.format(caseProperties.getDateTimeValue("DateCreated")); 
	                
					propertyMap.put("DateCreated", dateCreated);
					propertyMap.put("Amount", caseProperties.getFloat64Value("FFC_Amount"));
					propertyMap.put("EPFNumber", caseProperties.getStringValue("FFC_EPFNumber"));
					propertyMap.put("PolicyNumber", caseProperties.getStringValue("FFC_PolicyNumber"));
					propertyMap.put("CardNumber", caseProperties.getStringValue("FFC_CardNumber"));
					propertyMap.put("CompanyName", caseProperties.getStringValue("FFC_CompanyName"));
					propertyMap.put("CardNumber", caseProperties.getStringValue("FFC_CardNumber"));
					propertyMap.put("ConditionleadtoAdmission", caseProperties.getStringValue("FFC_ConditionleadtoAdmission"));
					propertyMap.put("FullName", caseProperties.getStringValue("FFC_FullName"));
					propertyMap.put("JobTitle", caseProperties.getStringValue("FFC_JobTitle"));
					propertyMap.put("MobileNumber", caseProperties.getStringValue("FFC_MobileNumber"));
					propertyMap.put("ResidenceTelephone", caseProperties.getStringValue("FFC_ResidenceTelephone"));
									
					responseArr.add(propertyMap);
				}
			}catch(Exception e){
				e.printStackTrace();
				log.error(e.getMessage() + " : " + e);			
			}					
		}
		return responseArr;			
	}
	
	@RequestMapping(value="/caseSearch",produces="application/json")
	public JSONArray caseSearch(@RequestParam("PropertyId") String propertyId, @RequestParam("value") String propertyValue)
	{
		
		log.info("In CaseSearch Method");
		log.info("PropertyId : "+propertyId+", value : "+propertyValue);
		JSONArray responseArr = new JSONArray();
		
		System.setProperty("java.security.auth.login.config", "/opt/IBM/BOC/jaas.conf.WSI");
		Connection connection = Factory.Connection.getConnection("http://172.25.100.120:9080/wsi/FNCEWS40MTOM/");
		Subject sub = UserContext.createSubject(connection, "p8admin","test@1234","FileNetP8WSI");
		UserContext.get().pushSubject(sub);
		
		Domain domain = Factory.Domain.getInstance(connection, null);
		ObjectStore objStore = Factory.ObjectStore.fetchInstance(domain, "CMTOS",null);
		SearchScope searchScope = new SearchScope(objStore);

		String sqlStr = "SELECT [FolderName], [ClassDescription], [DateCreated], [FFC_Amount], [FFC_CardNumber], [FFC_CompanyName], [FFC_ConditionleadtoAdmission], [FFC_EPFNumber], [FFC_FullName], [FFC_JobTitle], [FFC_MobileNumber], [FFC_PolicyNumber], [FFC_ReferenceNumber], [FFC_ResidenceTelephone] FROM [FFC_Claims] WHERE ["+propertyId+"]='"+propertyValue+"'";
		SearchSQL searchSQL = new SearchSQL(sqlStr);
		log.info("Query = "+sqlStr);
		IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSQL, new Integer(10), null, new Boolean(true)); 
		
		if(!(independentObjectSet.isEmpty())){
			
			try{
				Iterator it = independentObjectSet.iterator();		
				while(it.hasNext())
				{
					Folder caseFolder = (Folder)it.next();
					log.info("Retrieved Case Folder = "+caseFolder.get_FolderName());
					Properties caseProperties = caseFolder.getProperties();
					
					JSONObject propertyMap= new JSONObject();
					propertyMap.put("ReferenceNumber", caseProperties.getStringValue("FFC_ReferenceNumber"));
					
					DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");  
	                String dateCreated = dateFormat.format(caseProperties.getDateTimeValue("DateCreated")); 
	                
					propertyMap.put("DateCreated", dateCreated);
					propertyMap.put("Amount", caseProperties.getFloat64Value("FFC_Amount"));
					propertyMap.put("EPFNumber", caseProperties.getStringValue("FFC_EPFNumber"));
					propertyMap.put("PolicyNumber", caseProperties.getStringValue("FFC_PolicyNumber"));
					propertyMap.put("CardNumber", caseProperties.getStringValue("FFC_CardNumber"));
					propertyMap.put("CompanyName", caseProperties.getStringValue("FFC_CompanyName"));
					propertyMap.put("CardNumber", caseProperties.getStringValue("FFC_CardNumber"));
					propertyMap.put("ConditionleadtoAdmission", caseProperties.getStringValue("FFC_ConditionleadtoAdmission"));
					propertyMap.put("FullName", caseProperties.getStringValue("FFC_FullName"));
					propertyMap.put("JobTitle", caseProperties.getStringValue("FFC_JobTitle"));
					propertyMap.put("MobileNumber", caseProperties.getStringValue("FFC_MobileNumber"));
					propertyMap.put("ResidenceTelephone", caseProperties.getStringValue("FFC_ResidenceTelephone"));
									
					responseArr.add(propertyMap);
				}
			}catch(Exception e){
				e.printStackTrace();
				log.error(e.getMessage() + " : " + e);			
			}					
		}
		return responseArr;
	}
}
