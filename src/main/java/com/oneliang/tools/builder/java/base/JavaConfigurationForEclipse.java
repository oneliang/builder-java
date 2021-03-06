package com.oneliang.tools.builder.java.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oneliang.Constants;
import com.oneliang.tools.builder.base.Project;
import com.oneliang.util.common.JavaXmlUtil;
import com.oneliang.util.common.StringUtil;

public class JavaConfigurationForEclipse extends JavaConfiguration{

	protected static final String CLASSPATH=".classpath";

	protected void initializeAllProject() {
		initializeAllProjectFromEclipse();
		super.initializeAllProject();
	}

	private void initializeAllProjectFromEclipse() {
		JavaProject mainJavaProject=new JavaProject(this.getProjectWorkspace(),this.getProjectMain(),this.getBuildOutput());
		mainJavaProject.initialize();
		this.addProject(mainJavaProject);
		Queue<JavaProject> queue=new ConcurrentLinkedQueue<JavaProject>();
		queue.add(mainJavaProject);
		while(!queue.isEmpty()){
			JavaProject javaProject=queue.poll();
			//read classpath
			String classpath=javaProject.getHome()+Constants.Symbol.SLASH_LEFT+CLASSPATH;
			List<String> sourceDirectoryList=new ArrayList<String>();
			List<String> dependProjectList=new ArrayList<String>();
			Document document=null;
			try{
				document=JavaXmlUtil.parse(classpath);
			}catch (Exception e) {
				logger.warning("Use 'src' for source directory,because '"+classpath+"' file is not exist");
			}
			if(document!=null){
				Element root=document.getDocumentElement();
				NodeList entryElementList=root.getElementsByTagName("classpathentry");
				if(entryElementList!=null&&entryElementList.getLength()>0){
					for(int i=0;i<entryElementList.getLength();i++){
						NamedNodeMap namedNodeMap=entryElementList.item(i).getAttributes();
						Node kindNode=namedNodeMap.getNamedItem("kind");
						if(kindNode!=null){
							String value=kindNode.getNodeValue();
							if(value!=null){
								if(value.equals("src")){
									Node pathNode=namedNodeMap.getNamedItem("path");
									if(pathNode!=null){
										String sourceDirectory=pathNode.getNodeValue();
										if(sourceDirectory!=null&&sourceDirectory.startsWith(Constants.Symbol.SLASH_LEFT)){
											String dependProjectName=sourceDirectory.replace(Constants.Symbol.SLASH_LEFT, StringUtil.BLANK);
											dependProjectList.add(dependProjectName);
											if(!this.projectMap.containsKey(dependProjectName)){
												JavaProject parentJavaProject=new JavaProject(this.projectWorkspace,dependProjectName, this.buildOutput);
												parentJavaProject.initialize();
												this.addProject(parentJavaProject);
												queue.add(parentJavaProject);
											}
										}else{
											if(this.isSourceDirectory(javaProject, sourceDirectory)){
												sourceDirectoryList.add(sourceDirectory);
											}
										}
									}
								}else if(value.equals("lib")){
									Node pathNode=namedNodeMap.getNamedItem("path");
									if(pathNode!=null){
										String dependJar=pathNode.getNodeValue();
										File dependJarFile=new File(javaProject.getHome(),dependJar);
										javaProject.addDependJar(dependJarFile.getAbsolutePath());
									}
								}
							}
						}
					}
				}
			}
			javaProject.setDependProjects(dependProjectList.toArray(new String[]{}));
			javaProject.setSources(sourceDirectoryList.toArray(new String[]{}));
		}
	}

	private boolean isSourceDirectory(Project Project, String sourceDirectory) {
		return true;
	}
}
