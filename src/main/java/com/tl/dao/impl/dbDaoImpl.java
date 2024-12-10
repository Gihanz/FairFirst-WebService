package com.tl.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.tl.dao.dbDao;
import com.tl.dao.exceptions.DAOException;

@Repository
public class dbDaoImpl extends abstractWFConfigdao implements dbDao{
	
private static Logger log =LoggerFactory.getLogger(dbDaoImpl.class);

public List<Map<String, String>> getAllBranches() {		
	List<Map<String, String>> rtnData= new ArrayList<Map<String, String>>();
	 	 
	    String branchQry = null;		
			
		return rtnData;
}
	

}
