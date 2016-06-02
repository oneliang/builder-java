package com.oneliang.tools.builder.java.base;

import java.util.List;

import com.oneliang.tools.builder.base.Project;

public class JavaProject extends Project{

	//use in building
	private List<JavaProject> parentJavaProjectList=null;
	protected List<String> compileClasspathList=null;
	protected List<String> onlyCompileClasspathList=null;

	public JavaProject() {
	}

	public JavaProject(String workspace, String name) {
		super(workspace, name);
	}

	public JavaProject(String workspace, String name, String outputHome) {
		super(workspace, name, outputHome);
	}

	public void initialize() {
		super.initialize();
	}

	/**
	 * @return the parentJavaProjectList
	 */
	public List<JavaProject> getParentJavaProjectList() {
		return parentJavaProjectList;
	}

	/**
	 * @param parentJavaProjectList the parentJavaProjectList to set
	 */
	void setParentJavaProjectList(List<JavaProject> parentJavaProjectList) {
		this.parentJavaProjectList = parentJavaProjectList;
	}

	/**
	 * @return the compileClasspathList
	 */
	public List<String> getCompileClasspathList() {
		return compileClasspathList;
	}

	/**
	 * @param compileClasspathList the compileClasspathList to set
	 */
	public void setCompileClasspathList(List<String> compileClasspathList) {
		this.compileClasspathList = compileClasspathList;
	}
	/**
	 * @return the onlyCompileClasspathList
	 */
	public List<String> getOnlyCompileClasspathList() {
		return onlyCompileClasspathList;
	}

	/**
	 * @param onlyCompileClasspathList the onlyCompileClasspathList to set
	 */
	public void setOnlyCompileClasspathList(List<String> onlyCompileClasspathList) {
		this.onlyCompileClasspathList = onlyCompileClasspathList;
	}
}
