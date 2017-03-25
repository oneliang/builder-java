package com.oneliang.tools.builder.java.base;

import java.util.List;

import com.oneliang.tools.builder.base.Project;

public class JavaProject extends Project {

	protected List<String> compileProcessorPathList = null;
	// use in building
	protected List<String> compileClasspathList = null;
	protected List<String> onlyCompileClasspathList = null;

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
	 * @return the compileProcessorPathList
	 */
	public List<String> getCompileProcessorPathList() {
		return compileProcessorPathList;
	}

	/**
	 * @param compileProcessorPathList
	 *            the compileProcessorPathList to set
	 */
	public void setCompileProcessorPathList(List<String> compileProcessorPathList) {
		this.compileProcessorPathList = compileProcessorPathList;
	}

	/**
	 * @return the compileClasspathList
	 */
	public List<String> getCompileClasspathList() {
		return compileClasspathList;
	}

	/**
	 * @param compileClasspathList
	 *            the compileClasspathList to set
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
	 * @param onlyCompileClasspathList
	 *            the onlyCompileClasspathList to set
	 */
	public void setOnlyCompileClasspathList(List<String> onlyCompileClasspathList) {
		this.onlyCompileClasspathList = onlyCompileClasspathList;
	}
}
