package com.oneliang.tools.builder.java.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BuildException;
import com.oneliang.tools.builder.base.BuilderUtil;
import com.oneliang.tools.builder.java.base.JavaProject;
import com.oneliang.util.common.StringUtil;
import com.oneliang.util.file.FileUtil;

public class CompileJavaProjectHandler extends JavaProjectHandler {

	protected List<CacheOption> getCacheOptionList() {
		FileUtil.createDirectory(this.javaProject.getClassesOutput());
		FileUtil.createDirectory(this.javaProject.getCacheOutput());
		String sourceFileCacheFullFilename=javaProject.getCacheOutput()+"/javaFileCache";
		final CacheOption cacheOption=new CacheOption(sourceFileCacheFullFilename, this.javaProject.getSourceDirectoryList());
		cacheOption.fileSuffix=Constant.Symbol.DOT+Constant.File.JAVA;
		cacheOption.changedFileProcessor=new ChangedFileProcessor() {
			public boolean process(Iterable<ChangedFile> changedFileIterable) {
				boolean result=false;
				String classesOutput=javaProject.getClassesOutput();
				List<String> classpathList=javaProject.getCompileClasspathList();
				if(changedFileIterable!=null&&changedFileIterable.iterator().hasNext()){
					List<String> togetherSourceList=new ArrayList<String>();
					String javacSourceListFullFilename=javaProject.getCacheOutput()+"/"+JavaProject.JAVAC_SOURCE_FILE_LIST;
					try{
						StringBuilder stringBuilder=new StringBuilder();
						for(ChangedFile changedFile:changedFileIterable){
							String source=changedFile.fullFilename;
							stringBuilder.append(source);
							if(BuilderUtil.isWindowsOS()){
								stringBuilder.append(StringUtil.CRLF_STRING);
							}else{
								stringBuilder.append(StringUtil.LF_STRING);
							}
//							logger.log("\t"+javaProject.getName()+" compile source file:"+source);
						}
						FileUtil.writeFile(javacSourceListFullFilename, stringBuilder.toString().getBytes(Constant.Encoding.UTF8));
					}catch(Exception e){
						logger.error(Constant.Base.EXCEPTION, e);
						throw new BuildException(e);
					}
					togetherSourceList.add(Constant.Symbol.AT+javacSourceListFullFilename);
					int javacResult=0;
					try{
						javacResult=BuilderUtil.javac(classpathList, togetherSourceList, classesOutput, true);
					}catch(Throwable e){
						logger.error(javaProject.getName()+Constant.Symbol.COLON+Constant.Base.EXCEPTION, e);
						javacResult=1;
					}
					if(javacResult!=0){
						result=false;
					}else{
						result=true;
					}
				}else{
					result=true;
				}
				return result;
			}
		};
		return Arrays.asList(cacheOption);
	}
}
