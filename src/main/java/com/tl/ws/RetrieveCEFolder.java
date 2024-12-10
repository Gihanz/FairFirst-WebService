package com.tl.ws;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

public class RetrieveCEFolder {

	private static Logger log = LoggerFactory.getLogger(RetrieveCEFolder.class);

	public Folder retrieveCEFolder(String ref) {

		Folder folder = null;
		System.setProperty("java.security.auth.login.config", "/opt/IBM/BOC/jaas.conf.WSI");
		Connection connection = Factory.Connection.getConnection("http://172.25.100.120:9080/wsi/FNCEWS40MTOM/");
		Subject sub = UserContext.createSubject(connection, "p8admin","test@1234","FileNetP8WSI");
		UserContext.get().pushSubject(sub);
		
		Domain domain = Factory.Domain.getInstance(connection, null);
		ObjectStore objStore = Factory.ObjectStore.fetchInstance(domain, "CMTOS",null);
		SearchScope searchScope = new SearchScope(objStore);

		String sqlStr = "SELECT * FROM [FFC_Claims] WHERE [FFC_ReferenceNumber] = '"+ref+"'";
		SearchSQL searchSQL = new SearchSQL(sqlStr);
		log.info("Query = "+sqlStr);
		IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSQL, new Integer(10), null, new Boolean(true)); 
		if(!(independentObjectSet.isEmpty())){
			folder = (Folder)independentObjectSet.iterator().next();
			log.info("Retrieved CE Folder = "+folder.get_FolderName());
		}

		return folder;
	}
	
}
