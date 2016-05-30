package com.oneliang.tools.builder.java.handler;

import com.oneliang.util.file.FileUtil;

public class CleanHandler extends AbstractJavaHandler {

	public boolean handle() {
		FileUtil.deleteAllFile(this.javaConfiguration.getBuildOutput());
		return true;
	}
}
