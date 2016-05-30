package com.oneliang.tools.builder.java.handler;

import java.io.FileOutputStream;
import java.util.List;

import com.oneliang.Constant;
import com.oneliang.tools.builder.base.BaseHandler;
import com.oneliang.tools.builder.base.BuildException;
import com.oneliang.tools.builder.base.BuilderUtil;
import com.oneliang.util.common.StringUtil;
import com.oneliang.util.file.FileUtil;
import com.oneliang.util.json.JsonObject;
import com.oneliang.util.logging.Logger;
import com.oneliang.util.logging.LoggerManager;

public class CollectMatchFileHandler extends BaseHandler {

	protected static final Logger logger=LoggerManager.getLogger(CollectMatchFileHandler.class);

	public boolean handle() {
		if(this.inputKeyValue!=null){
			JsonObject jsonObject=new JsonObject(this.inputKeyValue);
			FileUtil.MatchOption matchOption=new FileUtil.MatchOption(jsonObject.getString("sourceFloder"));
			matchOption.fileSuffix=jsonObject.getString("suffix");
			List<String> sourceFileList=FileUtil.findMatchFile(matchOption);
			String javacSourceListFullFilename=jsonObject.getString("tempOutputFile");
			FileUtil.createFile(javacSourceListFullFilename);
			try{
				FileOutputStream fileOutputStream=new FileOutputStream(javacSourceListFullFilename);
				for(String source:sourceFileList){
					fileOutputStream.write(source.getBytes(Constant.Encoding.UTF8));
					if(BuilderUtil.isWindowsOS()){
						fileOutputStream.write(StringUtil.CRLF);
					}else{
						fileOutputStream.write(StringUtil.LF);
					}
					fileOutputStream.flush();
//					logger.log("\t"+androidProject.getName()+" compile source file:"+source);
				}
				fileOutputStream.close();
			}catch(Exception e){
				logger.error(Constant.Base.EXCEPTION, e);
				throw new BuildException(e);
			}
			this.configuration.putTemporaryData(this.outputKey, Constant.Symbol.AT+javacSourceListFullFilename);
		}
		return true;
	}
}
