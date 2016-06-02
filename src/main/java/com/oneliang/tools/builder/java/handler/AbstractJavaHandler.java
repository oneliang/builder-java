package com.oneliang.tools.builder.java.handler;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BuilderUtil;
import com.oneliang.tools.builder.base.CacheHandler;
import com.oneliang.tools.builder.base.Configuration;
import com.oneliang.tools.builder.java.base.Java;
import com.oneliang.tools.builder.java.base.JavaConfiguration;
import com.oneliang.tools.builder.java.base.JavaProject;
import com.oneliang.util.file.FileUtil;
import com.oneliang.util.file.FileUtil.NoCacheFileFinder;
import com.oneliang.util.file.FileUtil.NoCacheFileProcessor;
import com.oneliang.util.logging.Logger;
import com.oneliang.util.logging.LoggerManager;

public abstract class AbstractJavaHandler extends CacheHandler {

	protected static final Logger logger=LoggerManager.getLogger(AbstractJavaHandler.class);

	protected JavaConfiguration javaConfiguration=null;
	protected Java java=null;

	/**
	 * find java project source file list with cache
	 * @param javaProject
	 * @param noCacheFileProcessor
	 * @return sourceList
	 */
	protected final List<String> findJavaProjectSourceFileListWithCache(JavaProject javaProject,NoCacheFileProcessor noCacheFileProcessor){
		//source java file mapping
		final List<String> sourceFileDirectoryList=javaProject.getSourceDirectoryList();
		String sourceFileMappingFullFilename=javaProject.getCacheOutput()+"/"+JavaProject.JAVA_FILE_MAPPING;
		List<String> noCacheSourceFileList=FileUtil.dealWithFileCache(sourceFileMappingFullFilename, new NoCacheFileFinder(){
			public List<String> findNoCacheFileList(Properties cacheFileMapping) {
				return BuilderUtil.findWantToCompileSourceListWithCache(sourceFileDirectoryList, cacheFileMapping,true);
			}
		}, noCacheFileProcessor);
		return noCacheSourceFileList;
	}

	/**
	 * find java project class file list with cache
	 * @param javaProject
	 * @param saveCache
	 * @return List<String>
	 */
	protected final List<String> findJavaProjectClassFileListWithCache(JavaProject javaProject, final boolean saveCache){
		//compile java file mapping
		final List<String> classesDirectoryList=Arrays.asList(javaProject.getClassesOutput());
		String classFileMappingFullFilename=javaProject.getCacheOutput()+"/"+JavaProject.CLASS_FILE_MAPPING;
		List<String> noCacheClassFileList=FileUtil.dealWithFileCache(classFileMappingFullFilename, new NoCacheFileFinder(){
			public List<String> findNoCacheFileList(Properties cacheFileMapping) {
				return FileUtil.findFileListWithCache(classesDirectoryList, cacheFileMapping, Constant.Symbol.DOT+Constant.File.CLASS, true);
			}
		}, new NoCacheFileProcessor(){
			public boolean process(List<String> uncachedFileList) {
				return saveCache;
			}
		});
		return noCacheClassFileList;
	}

	/**
	 * @param java the java to set
	 */
	public void setJava(Java java) {
		this.java = java;
	}

	public void setConfiguration(Configuration configuration) {
		super.setConfiguration(configuration);
		if(configuration!=null&&(configuration instanceof JavaConfiguration)){
			this.javaConfiguration=(JavaConfiguration)configuration;
			this.java=this.javaConfiguration.getJava();
		}
	}
}
