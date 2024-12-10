package com.tl.ws;

import java.io.File;
import java.net.URL;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tl.service.dbService;

public class BaseWebServiceEndPointImpl {

	@Autowired
	dbService dbService;
	
   // public static Logger log = Logger.getLogger("com.boc.ws.BaseWebServiceEndPointImpl");
    private static Logger log =LoggerFactory.getLogger(BaseWebServiceEndPointImpl.class);
	static ResourceBundle configMsgBundle = ResourceBundle.getBundle("restconfig"); //Locale.getDefault()
	static ResourceBundle appExceptionMsgBundle = ResourceBundle.getBundle("ExceptionMessages",Locale.getDefault());
	
    private static Calendar lastLog4jPropertiesReloadedOn = null;
    public static String log4jpath;	
	
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	private String error;
	
	public static ResourceBundle getAppExceptionMsgBundle() {
		return appExceptionMsgBundle;
	}
	public String getExcptnMesProperty(String key)
    {
		if(appExceptionMsgBundle != null && appExceptionMsgBundle.containsKey(key)){
			return appExceptionMsgBundle.getString(key).intern();
		} else{
			return "Error in processing your request";
		}
    }
	
	public String getExcptnMesProperty(String key,Object[] params)
    {
		try
		{
		if(appExceptionMsgBundle != null && appExceptionMsgBundle.containsKey(key)){		
			return MessageFormat.format(appExceptionMsgBundle.getString(key).intern(), params);
		}else{
			return "Error in processing your request";
		}
		}catch(Exception e){
			return "Error in processing your request";
		}
    }
	public static void setAppExceptionMsgBundle(ResourceBundle appExceptionMsgBundle) {
		BaseWebServiceEndPointImpl.appExceptionMsgBundle = appExceptionMsgBundle;
	}
	
}
