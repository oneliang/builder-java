package com.oneliang.tools.builder.java.handler;

import com.oneliang.tools.builder.base.CacheHandler;
import com.oneliang.tools.builder.base.Configuration;
import com.oneliang.tools.builder.java.base.Java;
import com.oneliang.tools.builder.java.base.JavaConfiguration;
import com.oneliang.util.logging.Logger;
import com.oneliang.util.logging.LoggerManager;

public abstract class AbstractJavaHandler extends CacheHandler {

	protected static final Logger logger=LoggerManager.getLogger(AbstractJavaHandler.class);
	protected static final String CACHE_JAVA_FILE="javaFile";
	protected static final String CACHE_CLASS_FILE="classFile";
	protected static final String CACHE_JAR_FILE="jarFile";
	protected static final String CACHE_PREPARE_OUTPUT="prepareOutput";
	protected static final String JAVAC_SOURCE_FILE_LIST="javacSourceFileList";

	protected JavaConfiguration javaConfiguration=null;
	protected Java java=null;

	public void setConfiguration(Configuration configuration) {
		super.setConfiguration(configuration);
		if(configuration!=null&&(configuration instanceof JavaConfiguration)){
			this.javaConfiguration=(JavaConfiguration)configuration;
			this.java=this.javaConfiguration.getJava();
		}
	}
}
