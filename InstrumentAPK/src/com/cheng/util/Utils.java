package com.cheng.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.cheng.GlobalValue;

public class Utils {
	public static void main(String[] args){
		loadConfig();
		System.out.println(getDexPaths(GlobalValue.DEX_DIRECTORY));
	}
	
	//获取动态运行得到的所有dex文件的路径
	public static List<String> getDexPaths(String dexDirectoryString){
		File dexDirectory = new File(dexDirectoryString);
		if(!dexDirectory.exists() || dexDirectory.isFile())
			return null;
		Set<String> set = new HashSet<>();
		List<String> paths = new LinkedList<>();
		for(File file : dexDirectory.listFiles()){
			if(file.isFile() && (file.getName().endsWith(".dex") || file.getName().endsWith(".apk")
					|| file.getName().endsWith(".jar"))){
				String sha256Hash = getSHAHash(file.getAbsolutePath());
				if(!set.contains(sha256Hash)){
					set.add(sha256Hash);
					paths.add(file.getAbsolutePath());
				}
			}
		}
		return paths;
	}
	//加载配置文件，包含动态运行的结果目录
	public static void loadConfig(){
		try {
			InputStream inputStream = new FileInputStream("config.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			String STADYNA_RESULT_KEY = "stadyna_result";
			String DEX_DIRECTORY_KEY = "dex_directory";
			String WORKSPACE_KEY = "workspace";
			GlobalValue.STADYNA_RESULT = properties.getProperty(STADYNA_RESULT_KEY);
			GlobalValue.DEX_DIRECTORY = properties.getProperty(DEX_DIRECTORY_KEY);
			GlobalValue.WORKSPACE = properties.getProperty(WORKSPACE_KEY);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//计算给定文件的SHA-256摘要
	public static String getSHAHash(String filename){
		File file = new File(filename);
		if(!file.exists() || file.isDirectory()){
			return null;
		}
		InputStream inputStream = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			inputStream = new FileInputStream(filename);
			byte[] buffer = new byte[4096];
			int length;
			while((length = inputStream.read(buffer)) != -1){
				md.update(buffer,0,length);
			}
			StringBuffer hex = new StringBuffer();
			for(byte b : md.digest()){
				hex.append(Integer.toHexString(b & 0xFF));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
