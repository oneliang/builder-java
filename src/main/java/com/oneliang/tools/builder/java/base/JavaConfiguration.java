package com.oneliang.tools.builder.java.base;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BuilderConfiguration.TaskNodeInsertBean;
import com.oneliang.tools.builder.base.Configuration;
import com.oneliang.tools.builder.base.Handler;
import com.oneliang.tools.builder.base.Project;
import com.oneliang.tools.builder.java.handler.JavaProjectHandler;
import com.oneliang.util.common.StringUtil;

public abstract class JavaConfiguration extends Configuration {

	public static final String SHA1="SHA1";
	public static final String MD5_WITH_RSA="MD5withRSA";
	public static final String MAP_KEY_JAVA_SDK="javaSdk";
	public static final String MAP_KEY_JAR_KEYSTORE="jarKeystore";
	public static final String MAP_KEY_JAR_STORE_PASSWORD="jarStorePassword";
	public static final String MAP_KEY_JAR_KEY_PASSWORD="jarKeyPassword";
	public static final String MAP_KEY_JAR_KEY_ALIAS="jarKeyAlias";
	public static final String MAP_KEY_JAR_DIGESTALG="jarDigestalg";
	public static final String MAP_KEY_JAR_SIGALG="jarSigalg";
	public static final String MAP_KEY_PROJECT_WORKSPACE="projectWorkspace";
	public static final String MAP_KEY_PROJECT_MAIN="projectMain";
	public static final String MAP_KEY_PROJECT_TASK_NODE_INSERT_NAME="projectTaskNodeInsertName";

	protected String javaSdk=null;
	protected Java java=null;
	protected String jarKeystore=null;
	protected String jarStorePassword=null;
	protected String jarKeyPassword=null;
	protected String jarKeyAlias=null;
	protected String jarDigestalg=SHA1;
	protected String jarSigalg=MD5_WITH_RSA;

	protected String projectWorkspace=null;
	protected String projectMain=null;
	protected String buildOutput=null;
	protected String projectTaskNodeInsertName=null;

	protected JavaProject mainJavaProject=null;
	protected Map<TaskNodeInsertBean,Project> taskNodeInsertBeanProjectMap=new HashMap<TaskNodeInsertBean,Project>();

	public static class Environment{
		public static final String JAVA_HOME="JAVA_HOME";
	}

	protected void initialize() {
		super.initialize();
		if(StringUtil.isBlank(this.javaSdk)){
			if(this.builderConfiguration.getEnvironmentMap().containsKey(Environment.JAVA_HOME)){
				this.javaSdk=this.builderConfiguration.getEnvironmentMap().get(Environment.JAVA_HOME);
			}else{
				throw new ConfigurationException("Need to configurate "+Environment.JAVA_HOME+" or set the java.sdk in build properties");
			}
		}else{
			this.javaSdk=this.javaSdk.trim();
		}
		this.java=new Java(this.javaSdk);
		this.jarKeystore=new File(this.jarKeystore).getAbsolutePath();
		this.projectWorkspace=new File(this.projectWorkspace).getAbsolutePath();
		logger.info(MAP_KEY_JAVA_SDK+Constant.Symbol.COLON+this.javaSdk);
		logger.info(MAP_KEY_JAR_KEYSTORE+Constant.Symbol.COLON+this.jarKeystore);
//		logger.info(MAP_KEY_JAR_STORE_PASSWORD+Constant.Symbol.COLON+this.jarStorePassword);
//		logger.info(MAP_KEY_JAR_KEY_PASSWORD+Constant.Symbol.COLON+this.jarKeyPassword);
		logger.info(MAP_KEY_JAR_DIGESTALG+Constant.Symbol.COLON+this.jarDigestalg);
		logger.info(MAP_KEY_JAR_SIGALG+Constant.Symbol.COLON+this.jarSigalg);
		logger.info(MAP_KEY_PROJECT_WORKSPACE+Constant.Symbol.COLON+this.projectWorkspace);
		logger.info(MAP_KEY_PROJECT_MAIN+Constant.Symbol.COLON+this.projectMain);
		logger.info(MAP_KEY_PROJECT_TASK_NODE_INSERT_NAME+Constant.Symbol.COLON+this.projectTaskNodeInsertName);
	}

	protected void initializeAllProject() {
		for(Project project:this.projectList){
			project.setParentProjectList(this.findParentProjectList(project));
			List<String> compileClasspathList=this.getProjectCompileClasspathList(project);
			if(project instanceof JavaProject){
				JavaProject javaProject=(JavaProject)project;
				javaProject.setCompileClasspathList(compileClasspathList);
			}
		}
		Project mainProject=this.projectMap.get(this.projectMain);
		if(mainProject!=null&&mainProject instanceof JavaProject){
			this.mainJavaProject=(JavaProject)mainProject;
		}
		//add project to android project list,reset project list and project map
		this.projectList.clear();
		this.projectMap.clear();
		List<Project> mainProjectParentProjectList=mainProject.getParentProjectList();
		this.addProject(mainProject);
		for(Project project:mainProjectParentProjectList){
			this.addProject(project);
		}
	}

	protected List<TaskNodeInsertBean> increaseTaskNodeInsertBeanList() {
		if(StringUtil.isBlank(this.projectTaskNodeInsertName)){
			throw new ConfigurationException("Configuration name:projectTaskNodeInsertName can not be null or empty.");
		}
		List<TaskNodeInsertBean> taskNodeInsertBeanList=new ArrayList<TaskNodeInsertBean>();
		TaskNodeInsertBean projectTaskNodeInsertBean=this.builderConfiguration.getTaskNodeInsertBeanMap().get(this.projectTaskNodeInsertName);
		projectTaskNodeInsertBean.setSkip(true);
		for(Project project:this.projectList){
			//compile task node insert
			String name=project.getName();
			String[] parentNames=project.getDependProjects();
			if(parentNames==null||parentNames.length==0){
				parentNames=projectTaskNodeInsertBean.getParentNames();
			}
			TaskNodeInsertBean taskNodeInsertBean=new TaskNodeInsertBean();
			taskNodeInsertBean.setName(name);
			taskNodeInsertBean.setParentNames(parentNames);
			taskNodeInsertBean.setHandlerName(projectTaskNodeInsertBean.getHandlerName());
			taskNodeInsertBeanList.add(taskNodeInsertBean);
			taskNodeInsertBeanProjectMap.put(taskNodeInsertBean, project);

			if(name.equals(this.projectMain)){
				List<TaskNodeInsertBean> childTaskNodeInsertBeanList=this.builderConfiguration.getChildTaskNodeInsertBeanMap().get(this.projectTaskNodeInsertName);
				if(childTaskNodeInsertBeanList!=null){
					for(TaskNodeInsertBean childTaskNodeInsertBean:childTaskNodeInsertBeanList){
						Set<String> parentNameSet=this.filterTaskNodeParentNames(childTaskNodeInsertBean.getParentNames(), this.projectTaskNodeInsertName);
						parentNameSet.add(name);
						childTaskNodeInsertBean.setParentNames(parentNameSet.toArray(new String[]{}));
					}
				}
			}
		}
		return taskNodeInsertBeanList;
	}

	/**
	 * generate task node name
	 * @param baseName
	 * @return String
	 */
	protected final String generateTaskNodeName(String baseName){
		return this.projectMain+Constant.Symbol.COLON+baseName;
	}

	/**
	 * rename task node insert bean
	 * @param taskNodeInsertBean
	 */
	protected void renameTaskNodeInsertBean(TaskNodeInsertBean taskNodeInsertBean){
		taskNodeInsertBean.setName(this.generateTaskNodeName(taskNodeInsertBean.getName()));
		String[] parentNames=taskNodeInsertBean.getParentNames();
		if(parentNames!=null&&parentNames.length>0){
			String[] newParentNames=new String[parentNames.length];
			int i=0;
			for(String parentName:parentNames){
				newParentNames[i++]=this.generateTaskNodeName(parentName);
			}
			taskNodeInsertBean.setParentNames(newParentNames);
		}
	}

	protected void initializingTaskNodeInsertBean(TaskNodeInsertBean taskNodeInsertBean) {
		this.renameTaskNodeInsertBean(taskNodeInsertBean);
		Handler handler=taskNodeInsertBean.getHandlerInstance();
		if(handler instanceof JavaProjectHandler){
			JavaProjectHandler javaProjectHandler=(JavaProjectHandler)handler;
			Project project=this.taskNodeInsertBeanProjectMap.get(taskNodeInsertBean);
			if(project!=null&&project instanceof JavaProject){
				JavaProject javaProject=(JavaProject)project;
				javaProjectHandler.setJavaProject(javaProject);
			}
		}
	}

	/**
	 * find parent project list
	 * @param Project
	 * @return List<Project>
	 */
	public List<Project> findParentProjectList(Project project){
		List<Project> projectList=new ArrayList<Project>();
		Queue<Project> queue=new ConcurrentLinkedQueue<Project>();
		queue.add(project);
		while(!queue.isEmpty()){
			Project currentProject=queue.poll();
			String[] dependProjects=currentProject.getDependProjects();
			if(dependProjects!=null){
				for(String dependProject:dependProjects){
					Project parentProject=this.projectMap.get(dependProject);
					if(parentProject!=null){
						if(!projectList.contains(parentProject)){
							projectList.add(parentProject);
							queue.add(parentProject);
						}
					}else{
						logger.warning("Parent project is null,project:"+currentProject.getName()+",depend project:"+dependProject);
					}
				}
			}
		}
		return projectList;
	}

	/**
	 * get project compile classpath list
	 * @param project
	 * @return List<String>
	 */
	protected List<String> getProjectCompileClasspathList(Project project){
		List<String> classpathList=new ArrayList<String>();
		List<Project> parentAndSelfProjectList=new ArrayList<Project>();
		parentAndSelfProjectList.add(project);
		parentAndSelfProjectList.addAll(project.getParentProjectList());
		for(Project parentAndSelfProject:parentAndSelfProjectList){
			classpathList.addAll(parentAndSelfProject.getDependJarList());
//			if(!javaProject.getName().equals(parentAndSelfJavaProject.getName())){
				classpathList.add(parentAndSelfProject.getClassesOutput());
//			}else{
//				if(!this.isNeedToClean()){
//					classpathList.add(parentAndSelfJavaProject.getClassesOutput());
//				}
//			}
		}
		return classpathList;
	}

	/**
	 * filter task node parent names
	 * @param originalParentNames
	 * @param excludeTaskNodeInsertName
	 * @return Set<String>
	 */
	protected Set<String> filterTaskNodeParentNames(String[] originalParentNames,String excludeTaskNodeInsertName){
		Set<String> parentNameSet=new HashSet<String>();
		if(originalParentNames!=null){
			for(String originalParentName:originalParentNames){
				if(!originalParentName.equals(excludeTaskNodeInsertName)){
					parentNameSet.add(originalParentName);
				}
			}
		}
		return parentNameSet;
	}

	/**
	 * @return the buildOutput
	 */
	public String getBuildOutput() {
		return buildOutput;
	}
	/**
	 * @param buildOutput the buildOutput to set
	 */
	public void setBuildOutput(String buildOutput) {
		this.buildOutput = buildOutput;
	}
	/**
	 * @return the javaSdk
	 */
	public String getJavaSdk() {
		return javaSdk;
	}
	/**
	 * @param javaSdk the javaSdk to set
	 */
	public void setJavaSdk(String javaSdk) {
		this.javaSdk = javaSdk;
	}
	/**
	 * @return the java
	 */
	public Java getJava() {
		return java;
	}

	/**
	 * @return the jarKeystore
	 */
	public String getJarKeystore() {
		return jarKeystore;
	}

	/**
	 * @param jarKeystore the jarKeystore to set
	 */
	public void setJarKeystore(String jarKeystore) {
		this.jarKeystore = jarKeystore;
	}

	/**
	 * @return the jarStorePassword
	 */
	public String getJarStorePassword() {
		return jarStorePassword;
	}

	/**
	 * @param jarStorePassword the jarStorePassword to set
	 */
	public void setJarStorePassword(String jarStorePassword) {
		this.jarStorePassword = jarStorePassword;
	}

	/**
	 * @return the jarKeyPassword
	 */
	public String getJarKeyPassword() {
		return jarKeyPassword;
	}

	/**
	 * @param jarKeyPassword the jarKeyPassword to set
	 */
	public void setJarKeyPassword(String jarKeyPassword) {
		this.jarKeyPassword = jarKeyPassword;
	}

	/**
	 * @return the jarKeyAlias
	 */
	public String getJarKeyAlias() {
		return jarKeyAlias;
	}

	/**
	 * @param jarKeyAlias the jarKeyAlias to set
	 */
	public void setJarKeyAlias(String jarKeyAlias) {
		this.jarKeyAlias = jarKeyAlias;
	}

	/**
	 * @return the jarDigestalg
	 */
	public String getJarDigestalg() {
		return jarDigestalg;
	}

	/**
	 * @param jarDigestalg the jarDigestalg to set
	 */
	public void setJarDigestalg(String jarDigestalg) {
		this.jarDigestalg = jarDigestalg;
	}

	/**
	 * @return the jarSigalg
	 */
	public String getJarSigalg() {
		return jarSigalg;
	}

	/**
	 * @param jarSigalg the jarSigalg to set
	 */
	public void setJarSigalg(String jarSigalg) {
		this.jarSigalg = jarSigalg;
	}

	/**
	 * @return the projectWorkspace
	 */
	public String getProjectWorkspace() {
		return projectWorkspace;
	}

	/**
	 * @param projectWorkspace the projectWorkspace to set
	 */
	public void setProjectWorkspace(String projectWorkspace) {
		this.projectWorkspace = projectWorkspace;
	}

	/**
	 * @return the projectMain
	 */
	public String getProjectMain() {
		return projectMain;
	}

	/**
	 * @param projectMain the projectMain to set
	 */
	public void setProjectMain(String projectMain) {
		this.projectMain = projectMain;
	}

	/**
	 * @param projectTaskNodeInsertName the projectTaskNodeInsertName to set
	 */
	public void setProjectTaskNodeInsertName(String projectTaskNodeInsertName) {
		this.projectTaskNodeInsertName = projectTaskNodeInsertName;
	}

	/**
	 * @return the mainJavaProject
	 */
	public JavaProject getMainJavaProject() {
		return mainJavaProject;
	}
}
