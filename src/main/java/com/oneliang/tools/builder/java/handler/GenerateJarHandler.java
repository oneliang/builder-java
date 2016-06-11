package com.oneliang.tools.builder.java.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.ChangedFile;
import com.oneliang.tools.builder.base.Project;
import com.oneliang.tools.builder.java.base.JavaProject;
import com.oneliang.util.file.FileUtil;

public class GenerateJarHandler extends AbstractJavaHandler {

	protected List<CacheOption> getCacheOptionList(){
		FileUtil.createDirectory(this.javaConfiguration.getMainJavaProject().getPrepareOutput());
		List<String> classesOutputList=new ArrayList<String>();
		final List<Project> projectList=this.javaConfiguration.getProjectList();
		for(Project project:projectList){
			classesOutputList.add(project.getClassesOutput());
		}
		String cacheFullFilename=this.javaConfiguration.getMainJavaProject().getCacheOutput()+Constant.Symbol.SLASH_LEFT+CACHE_CLASS_FILE;
		final String prepareOutput=javaConfiguration.getMainJavaProject().getPrepareOutput();
		CacheOption classesCacheOption=new CacheOption(cacheFullFilename, classesOutputList);
		classesCacheOption.fileSuffix=Constant.Symbol.DOT+Constant.File.CLASS;
		classesCacheOption.changedFileProcessor=new ChangedFileProcessor(){
			public boolean process(Iterable<ChangedFile> changedFileIterable) {
				if(changedFileIterable!=null){
					for(ChangedFile changedFile:changedFileIterable){
						String directory=changedFile.directory;
						String fullFilename=changedFile.fullFilename;
						File directoryFile=new File(directory);
						File file=new File(fullFilename);
						String relativeName=file.getAbsolutePath().substring(directoryFile.getAbsolutePath().length());
						String outputFullFilename=prepareOutput+relativeName;
						FileUtil.copyFile(fullFilename, outputFullFilename, FileUtil.FileCopyType.FILE_TO_FILE);
					}
				}
				return true;
			}
		};
		cacheFullFilename=this.javaConfiguration.getMainJavaProject().getCacheOutput()+Constant.Symbol.SLASH_LEFT+CACHE_PREPARE_OUTPUT;
		CacheOption jarCacheOption=new CacheOption(cacheFullFilename, Arrays.asList(this.javaConfiguration.getMainJavaProject().getPrepareOutput()));
		jarCacheOption.changedFileProcessor=new ChangedFileProcessor(){
			public boolean process(Iterable<ChangedFile> changedFileIterable) {
				if(changedFileIterable.iterator().hasNext()){
					JavaProject mainJavaProject=javaConfiguration.getMainJavaProject();
					String outputJarFullFilename=mainJavaProject.getOutputHome()+Constant.Symbol.SLASH_LEFT+mainJavaProject.getName()+Constant.Symbol.DOT+Constant.File.JAR;
					FileUtil.zip(outputJarFullFilename, javaConfiguration.getMainJavaProject().getPrepareOutput());
				}
				return true;
			}
		};
		return Arrays.asList(classesCacheOption, jarCacheOption);
	}
}
