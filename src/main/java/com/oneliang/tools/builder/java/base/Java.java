package com.oneliang.tools.builder.java.base;

import java.io.File;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BuilderUtil;
import com.oneliang.util.common.StringUtil;

public class Java {

	private static final String BIN="bin";
	private static final String JAVA="java";
	private static final String JAVAC="javac";
	private static final String JAR="jar";
	private static final String JAR_SIGNER="jarsigner";

	private String home=null;
	private String bin=null;
	private String javaExecutor=null;
	private String javacExecutor=null;
	private String jarExecutor=null;
	private String jarSignerExecutor=null;

	public Java(String home){
		if(home==null){
			throw new NullPointerException("home is null");
		}
		this.home=home;
		File file=new File(this.home);
		this.home=file.getAbsolutePath();
		this.bin=this.home+"/"+BIN;
		String osExecuteFileSuffix=BuilderUtil.isWindowsOS()?(Constant.Symbol.DOT+Constant.File.EXE):StringUtil.BLANK;
		this.javaExecutor=this.bin+"/"+JAVA+osExecuteFileSuffix;
		this.javacExecutor=this.bin+"/"+JAVAC+osExecuteFileSuffix;
		this.jarExecutor=this.bin+"/"+JAR+osExecuteFileSuffix;
		this.jarSignerExecutor=this.bin+"/"+JAR_SIGNER+osExecuteFileSuffix;
	}

	/**
	 * @return the bin
	 */
	public String getBin() {
		return bin;
	}

	/**
	 * @return the javacExecutor
	 */
	public String getJavacExecutor() {
		return javacExecutor;
	}

	/**
	 * @return the jar
	 */
	public String getJarExecutor() {
		return jarExecutor;
	}

	/**
	 * @return the jarSignerExecutor
	 */
	public String getJarSignerExecutor() {
		return jarSignerExecutor;
	}

	/**
	 * @return the javaExecutor
	 */
	public String getJavaExecutor() {
		return javaExecutor;
	}

    /**
     * @return the home
     */
    public String getHome() {
        return home;
    }
}
