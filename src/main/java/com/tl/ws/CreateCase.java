package com.tl.ws;

import java.util.Locale;

import javax.security.auth.Subject;

import org.apache.commons.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
import com.ibm.casemgmt.api.Case;
import com.ibm.casemgmt.api.CaseType;
import com.ibm.casemgmt.api.DeployedSolution;
import com.ibm.casemgmt.api.constants.ModificationIntent;
import com.ibm.casemgmt.api.context.CaseMgmtContext;
import com.ibm.casemgmt.api.context.P8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleP8ConnectionCache;
import com.ibm.casemgmt.api.context.SimpleVWSessionCache;
import com.ibm.casemgmt.api.objectref.ObjectStoreReference;
import com.ibm.casemgmt.api.properties.CaseMgmtProperties;


public class CreateCase {

	private static Logger log = LoggerFactory.getLogger(UploadDoc.class);
	private UserContext uc;
	private boolean userSubjectPushed;
	private Connection conn;
	private CaseMgmtContext origCmctx;

	public Connection getCMConnection() {

		log.info("Inside getCMConnection()");
		String ceURI = "http://172.25.100.120:9080/wsi/FNCEWS40MTOM/";
		String userId = "p8admin";
		String password = "test@1234";
		String stanza = "FileNetP8WSI";

		P8ConnectionCache connCache = new SimpleP8ConnectionCache();
		conn = connCache.getP8Connection(ceURI);
		Subject subject = UserContext.createSubject(conn, userId, password, stanza);
		uc = UserContext.get();
		uc.pushSubject(subject);
		userSubjectPushed = true;
		Locale origLocale = uc.getLocale();
		uc.setLocale(Locale.ENGLISH);
		origCmctx = CaseMgmtContext.set(new CaseMgmtContext(new SimpleVWSessionCache(), connCache));
		log.info("End of getCMConnection()");
		return conn;
	}

	public void releaseCMConnection() {

		log.info("origCmctx is " + origCmctx + "userSubjectPushed is " + userSubjectPushed + " uc is " + uc);

		if (userSubjectPushed) {
			CaseMgmtContext.set(origCmctx);
			Locale origLocale = uc.getLocale();
			uc.setLocale(origLocale);
			uc.popSubject();
			userSubjectPushed = false;
			conn = null;
			origCmctx = null;
			System.out.println("Connection Released");
			log.info("Connection Released");
		}
	}

	public String createCase(JSONObject caseParameters, String referenceNumber, String caseType, String solutionName) {

		String newCaseIdentifier = null;
		Connection conn = getCMConnection();

		String caseTypename = caseType;
		String targetOsname = "CMTOS";
		String domainName = "p8domain";
		
		try {
			Domain domain = Factory.Domain.fetchInstance(conn, domainName, null);
			ObjectStore os = Factory.ObjectStore.fetchInstance(domain, targetOsname, null);
			ObjectStoreReference osRef = new ObjectStoreReference(os);
			DeployedSolution someSolution = DeployedSolution.getFetchlessInstance(osRef, solutionName);
			log.info("Solution Name : " + someSolution.getSolutionName());
			CaseType caseTypeInstance = CaseType.fetchInstance(osRef, caseTypename);
			{
				Case pendingCase = Case.createPendingInstanceFetchDefaults(caseTypeInstance);
				CaseMgmtProperties properties = pendingCase.getProperties();

				properties.putObjectValue("FFC_ReferenceNumber", referenceNumber);
				for(int x=0; x<caseParameters.length();x++){
					String key = caseParameters.names().getString(x);
					log.info("key : "+key+", value :"+caseParameters.get(key));
					properties.putObjectValue(key, caseParameters.get(key));				
				}
				log.info("Properties Successfull Updated");

				pendingCase.save(RefreshMode.REFRESH, null, ModificationIntent.MODIFY);
				newCaseIdentifier = pendingCase.getIdentifier();

				log.info("New CaseIdentifier is : " + newCaseIdentifier);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage() + " : " + e.fillInStackTrace());
		} finally {
			releaseCMConnection();
		}
		return newCaseIdentifier;
	}

}
