package com.tl.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tl.dao.dbDao;
import com.tl.service.dbService;

@Service
@Configurable
public class dbServiceImpl implements dbService{
	private static Logger log =LoggerFactory.getLogger(dbServiceImpl.class);
	@Autowired private dbDao dbDao = null;
	
	@Override
	@Transactional(value="transactionManager",readOnly=true)
	public List<Map<String, String>> getAllBranches() {
		return dbDao.getAllBranches();
	}

}
