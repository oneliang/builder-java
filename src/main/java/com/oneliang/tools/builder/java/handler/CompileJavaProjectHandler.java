package com.oneliang.tools.builder.java.handler;

import java.util.ArrayList;
import java.util.List;

import com.oneliang.Constants;
import com.oneliang.tools.builder.base.BuildException;
import com.oneliang.tools.builder.base.BuilderUtil;
import com.oneliang.tools.builder.base.CacheHandler.CacheOption.ChangedFileProcessor;
import com.oneliang.tools.builder.base.ChangedFile;
import com.oneliang.util.common.StringUtil;
import com.oneliang.util.file.FileUtil;

public class CompileJavaProjectHandler extends JavaProjectHandler {

    private boolean compileSuccess = false;

    public boolean handle() {
        FileUtil.createDirectory(this.javaProject.getClassesOutput());
        FileUtil.createDirectory(this.javaProject.getCacheOutput());
        String sourceFileCacheFullFilename = javaProject.getCacheOutput() + Constants.Symbol.SLASH_LEFT + CACHE_JAVA_FILE;
        final CacheOption cacheOption = new CacheOption(sourceFileCacheFullFilename, this.javaProject.getSourceDirectoryList());
        cacheOption.fileSuffix = Constants.Symbol.DOT + Constants.File.JAVA;
        cacheOption.changedFileProcessor = new ChangedFileProcessor() {
            public boolean process(Iterable<ChangedFile> changedFileIterable) {
                boolean result = false;
                String classesOutput = javaProject.getClassesOutput();
                List<String> classpathList = javaProject.getCompileClasspathList();
                if (changedFileIterable != null && changedFileIterable.iterator().hasNext()) {
                    List<String> togetherSourceList = new ArrayList<String>();
                    String javacSourceListFullFilename = javaProject.getCacheOutput() + Constants.Symbol.SLASH_LEFT + JAVAC_SOURCE_FILE_LIST;
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (ChangedFile changedFile : changedFileIterable) {
                            if (changedFile.status.equals(ChangedFile.Status.DELETED)) {
                                continue;
                            }
                            String source = changedFile.fullFilename;
                            stringBuilder.append(source);
                            if (BuilderUtil.isWindowsOS()) {
                                stringBuilder.append(StringUtil.CRLF_STRING);
                            } else {
                                stringBuilder.append(StringUtil.LF_STRING);
                            }
                            // logger.log("\t"+javaProject.getName()+" compile
                            // source file:"+source);
                        }
                        FileUtil.writeFile(javacSourceListFullFilename, stringBuilder.toString().getBytes(Constants.Encoding.UTF8));
                    } catch (Exception e) {
                        logger.error(Constants.Base.EXCEPTION, e);
                        throw new BuildException(e);
                    }
                    togetherSourceList.add(Constants.Symbol.AT + javacSourceListFullFilename);
                    int javacResult = 0;
                    try {
                        javacResult = BuilderUtil.javac(togetherSourceList, classesOutput, true, classpathList);
                    } catch (Throwable e) {
                        logger.error(javaProject.getName() + Constants.Symbol.COLON + Constants.Base.EXCEPTION, e);
                        javacResult = 1;
                    }
                    if (javacResult != 0) {
                        result = false;
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }
                if (result) {
                    compileSuccess = true;
                }
                return result;
            }
        };
        this.dealWithCache(cacheOption);
        return compileSuccess;
    }
}
