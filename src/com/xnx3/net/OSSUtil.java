package com.xnx3.net;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import com.aliyun.oss.OSSClient;
import com.xnx3.ConfigManagerUtil;
import com.xnx3.Lang;
import com.xnx3.net.ossbean.PutResult;

/**
 * aliyun OSS
 * <br><b>需导入</b> 
 * <br/>aliyun-sdk-oss-2.2.3.jar
 * <br/>commons-codec-1.9.jar
 * <br/>commons-logging-1.2.jar
 * <br/>hamcrest-core-1.1.jar
 * <br/>httpclient-4.4.1.jar
 * <br/>httpcore-4.4.1.jar
 * <br/>jdom-1.1.jar
 * @author 管雷鸣
 */
public class OSSUtil {
	public static String endpoint = "";
	public static String accessKeyId = "";
	public static String accessKeySecret = "";
	public static String bucketName = "";
    /**
     * 处理过的OSS外网域名,如 http://xnx3.oss-cn-qingdao.aliyuncs.com/
     * <br/>(文件上传成功时会加上此域名拼接出文件的访问完整URL。位于Bucket概览－OSS域名)
     */
    public static String url = ""; 
	
    private static OSSClient ossClient;
    
	static{
		endpoint = ConfigManagerUtil.getSingleton("xnx3Config.xml").getValue("aliyunOSS.endpoint");
		accessKeyId = ConfigManagerUtil.getSingleton("xnx3Config.xml").getValue("aliyunOSS.accessKeyId");
		accessKeySecret = ConfigManagerUtil.getSingleton("xnx3Config.xml").getValue("aliyunOSS.accessKeySecret");
		bucketName = ConfigManagerUtil.getSingleton("xnx3Config.xml").getValue("aliyunOSS.bucketName");
		
		url = ConfigManagerUtil.getSingleton("xnx3Config.xml").getValue("aliyunOSS.url");
		if(url == null || url.length() == 0){
			url = bucketName+"."+endpoint;
		}
		url = "http://"+url+"/";
	}
	
	/**
	 * 获取 OSSClient 对象
	 * @return {@link OSSClient}
	 */
	public static OSSClient getOSSClient(){
		if(ossClient == null){
			ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
		}
		return ossClient;
	}
	
	/**
	 * 创建文件夹
	 * @param folderName 要创建的文件夹名字，如要创建xnx3文件夹，则传入"xnx3/"。也可以传入"x/n/" 代表建立x文件夹同时其下再建立n文件夹
	 */
	public static void createFolder(String folderName){
		//既然是目录，那就是以/结束，判断此是否是以／结束的，若不是，末尾自动加上
		if(folderName.lastIndexOf("/")<(folderName.length()-1)){
			folderName+="/";
		}
		
		getOSSClient().putObject(bucketName, folderName, new ByteArrayInputStream(new byte[0]));
	}
	
	/**
	 * 上传文件
	 * @param filePath 上传后的文件所在OSS的目录、路径，如 "jar/file/"
	 * @param fileName 上传的文件名，如“xnx3.jar”；主要拿里面的后缀名。也可以直接传入文件的后缀名如“.jar”
	 * @param inputStream {@link InputStream}
	 * @return {@link PutResult} 若失败，返回null
	 */
	public static PutResult put(String filePath,String fileName,InputStream inputStream){
		String fileSuffix=com.xnx3.Lang.subString(fileName, ".", null, 3);	//获得文件后缀，以便重命名
        String name=Lang.uuid()+"."+fileSuffix;
        String path = filePath+name;
		getOSSClient().putObject(bucketName, path, inputStream);
		
		return new PutResult(name, path,url+path);
	}
	
	/**
	 * 删除文件
	 * @param filePath 文件所在OSS的绝对路径，如 "jar/file/xnx3.jpg"
	 */
	public static void deleteObject(String filePath){
		getOSSClient().deleteObject(bucketName, filePath);
	}
	
	/**
	 * 上传文件。上传后的文件名固定
	 * @param path 上传到哪里，包含上传后的文件名，如"image/head/123.jpg"
	 * @param inputStream 文件
	 * @return {@link PutResult}
	 */
	public static PutResult put(String path,InputStream inputStream){
		getOSSClient().putObject(bucketName, path, inputStream);
		String name = Lang.subString(path, "/", null, 3);
		return new PutResult(name, path,url+path);
	}
	
	/**
	 * 上传本地文件
	 * @param filePath 上传后的文件所在OSS的目录、路径，如 "jar/file/"
	 * @param localPath 本地要上传的文件的绝对路径，如 "/jar_file/iw.jar"
	 * @return {@link PutResult} 若失败，返回null
	 */
	public static PutResult put(String filePath, String localPath){
		File file = new File(localPath);
		InputStream input = null;
		try {
			input = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return put(filePath, localPath, input);
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		//测试上传
		PutResult pr = OSSUtil.put("jar/file/", "/jar_file/iw.jar");
		System.out.println(pr);
	}
}
