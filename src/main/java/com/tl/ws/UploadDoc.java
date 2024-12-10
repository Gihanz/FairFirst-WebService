package com.tl.ws;

import java.io.InputStream;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.util.UserContext;

public class UploadDoc {

	private static Logger log = LoggerFactory.getLogger(UploadDoc.class);

	public void uploadDoc(Folder folder, String DocName, String mimeType, InputStream Document) {

		System.setProperty("java.security.auth.login.config", "/opt/IBM/BOC/jaas.conf.WSI");
		Connection connection = Factory.Connection.getConnection("http://172.25.100.120:9080/wsi/FNCEWS40MTOM/");
		Subject sub = UserContext.createSubject(connection, "p8admin","test@1234","FileNetP8WSI");
		UserContext.get().pushSubject(sub);
		
		Domain domain = Factory.Domain.getInstance(connection, null);
		ObjectStore objStore = Factory.ObjectStore.fetchInstance(domain, "CMTOS",null);

		log.info("Folder = "+folder.get_PathName());
		 
		// Create Document
		Document doc = Factory.Document.createInstance(objStore, "FFC_ClaimForm", null); 
		ContentElementList contEleList = Factory.ContentElement.createList();
		ContentTransfer ct = Factory.ContentTransfer.createInstance();

		ct.setCaptureSource(Document);
		ct.set_ContentType(mimeType);
		ct.set_RetrievalName(DocName);
		contEleList.add(ct);

		doc.set_ContentElements(contEleList);
		doc.getProperties().putValue("DocumentTitle", DocName);
		
		doc.set_MimeType(mimeType);
		doc.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);
		doc.save(RefreshMode.REFRESH);

		ReferentialContainmentRelationship rcr = folder.file(doc,AutoUniqueName.AUTO_UNIQUE, DocName, DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
		rcr.save(RefreshMode.REFRESH);
		log.info("Version Series ID of new document is : "+doc.get_VersionSeries().get_Id());
				 
	}
}
