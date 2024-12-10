package com.tl.service;

import java.util.List;
import java.util.Map;

public interface dbService {
	List<Map<String, String>> getAllBranches() throws Exception;
}
