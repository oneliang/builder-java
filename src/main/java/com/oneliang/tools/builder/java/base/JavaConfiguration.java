package com.oneliang.tools.builder.java.base;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BuilderConfiguration.TaskNodeInsertBean;
import com.oneliang.tools.builder.base.Configuration;
import com.oneliang.tools.builder.base.Handler;
import com.oneliang.tools.builder.base.Project;
import com.oneliang.tools.builder.java.handler.JavaProjectHandler;
import com.oneliang.util.common.StringUtil;
import com.oneliang.util.logging.Logger;
import com.oneliang.util.logging.LoggerManager;

public abstract class JavaConfiguration extends Configuration {

	protected static final Logger logger=LoggerManager.getLogger(JavaConfiguration.class);
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
	protected final List<JavaProject> javaProjectList=new CopyOnWriteArrayList<JavaProject>();
	protected final Map<String, JavaProject> javaProjectMap=new ConcurrentHashMap<String, JavaProject>();
	private Map<TaskNodeInsertBean,JavaProject> taskNodeInsertBeanJavaProjectMap=new HashMap<TaskNodeInsertBean,JavaProject>();

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
		if(this.projectList.isEmpty()){
			if(this.ideInitializer==null){
				throw new NullPointerException("ide initializer must not be null");
			}
			this.ideInitializer.initializeAllProjectFromIDE();
		}
		for(Project project:this.projectList){
			JavaProject javaProject=null;
			if(!(project instanceof JavaProject)){
				continue;
			}
			javaProject=(JavaProject)project;
			this.javaProjectList.add(javaProject);
			if(!this.javaProjectMap.containsKey(javaProject.getName())){
				this.javaProjectMap.put(javaProject.getName(),javaProject);
			}
		}
		this.mainJavaProject=this.javaProjectMap.get(this.projectMain);
		for(JavaProject javaProject:this.javaProjectList){
			javaProject.setParentJavaProjectList(this.findParentJavaProjectList(javaProject));
			List<String> compileClasspathList=this.getJavaProjectCompileClasspathList(javaProject);
			javaProject.setCompileClasspathList(compileClasspathList);
		}
	}

	protected List<TaskNodeInsertBean> increaseTaskNodeInsertBeanList() {
		if(StringUtil.isBlank(this.projectTaskNodeInsertName)){
			throw new ConfigurationException("Configuration name:projectTaskNodeInsertName can not be null or empty.");
		}
		List<TaskNodeInsertBean> taskNodeInsertBeanList=new ArrayList<TaskNodeInsertBean>();
		TaskNodeInsertBean projectTaskNodeInsertBean=this.builderConfiguration.getTaskNodeInsertBeanMap().get(this.projectTaskNodeInsertName);
		projectTaskNodeInsertBean.setSkip(true);
		for(JavaProject javaProject:this.javaProjectList){
			//compile task node insert
			String name=javaProject.getName();
			String[] parentNames=javaProject.getDependProjects();
			if(parentNames==null||parentNames.length==0){
				parentNames=projectTaskNodeInsertBean.getParentNames();
			}
			TaskNodeInsertBean taskNodeInsertBean=new TaskNodeInsertBean();
			taskNodeInsertBean.setName(name);
			taskNodeInsertBean.setParentNames(parentNames);
			taskNodeInsertBean.setHandlerName(projectTaskNodeInsertBean.getHandlerName());
			taskNodeInsertBeanList.add(taskNodeInsertBean);
			taskNodeInsertBeanJavaProjectMap.put(taskNodeInsertBean, javaProject);

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
			javaProjectHandler.setJavaProject(this.taskNodeInsertBeanJavaProjectMap.get(taskNodeInsertBean));
		}
	}

	/**
	 * find parent java project list
	 * @param javaProject
	 * @return List<JavaProject>
	 */
	public List<JavaProject> findParentJavaProjectList(JavaProject javaProject){
		List<JavaProject> javaPojectList=new ArrayList<JavaProject>();
		Queue<JavaProject> queue=new ConcurrentLinkedQueue<JavaProject>();
		queue.add(javaProject);
		while(!queue.isEmpty()){
			JavaProject project=queue.poll();
			String[] dependProjects=project.getDependProjects();
			if(dependProjects!=null){
				for(String dependProject:dependProjects){
					JavaProject parentJavaProject=this.javaProjectMap.get(dependProject);
					if(!javaPojectList.contains(parentJavaProject)){
						javaPojectList.add(parentJavaProject);
						queue.add(parentJavaProject);
					}
				}
			}
		}
		return javaPojectList;
	}

	/**
	 * get java project compile classpath list
	 * @param javaProject
	 * @return List<String>
	 */
	private List<String> getJavaProjectCompileClasspathList(JavaProject javaProject){
		List<String> classpathList=new ArrayList<String>();
		List<JavaProject> parentAndSelfJavaProjectList=new ArrayList<JavaProject>();
		parentAndSelfJavaProjectList.add(javaProject);
		parentAndSelfJavaProjectList.addAll(javaProject.getParentJavaProjectList());
		for(JavaProject parentAndSelfJavaProject:parentAndSelfJavaProjectList){
			classpathList.addAll(parentAndSelfJavaProject.getDependJarList());
//			if(!javaProject.getName().equals(parentAndSelfJavaProject.getName())){
				classpathList.add(parentAndSelfJavaProject.getClassesOutput());
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
	 * @return the javaProjectList
	 */
	public List<JavaProject> getJavaProjectList() {
		return javaProjectList;
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
