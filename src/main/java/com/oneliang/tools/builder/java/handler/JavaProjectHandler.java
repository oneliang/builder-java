package com.oneliang.tools.builder.java.handler;

import com.oneliang.tools.builder.base.Handler;
import com.oneliang.tools.builder.java.base.JavaProject;

public class JavaProjectHandler extends AbstractJavaHandler {

	protected JavaProject javaProject=null;

	protected void beforeInnerHandlerHandle(Handler innerHandler) {
		if(innerHandler!=null&&innerHandler instanceof JavaProjectHandler){
			JavaProjectHandler javaProjectHandler=(JavaProjectHandler)innerHandler;
			javaProjectHandler.setJavaProject(javaProject);
		}
	}

	/**
	 * @param javaProject the javaProject to set
	 */
	public void setJavaProject(JavaProject javaProject) {
		this.javaProject = javaProject;
	}
}
