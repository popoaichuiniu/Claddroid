package com.cheng;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		String filename = "/home/cheng/tools/classes.dex";
		System.out.println(getSHAHash(filename));
	}
	
	public static String getSHAHash(String filename){
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA-256");
			InputStream inputStream = new FileInputStream(filename);
			int length = 0;
			byte[] buffer = new byte[4096];
			while((length = inputStream.read(buffer)) != -1){
				md.update(buffer, 0, length);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		byte[] digestBytes = md.digest();
		System.out.println(digestBytes.length);
		StringBuffer hex = new StringBuffer();
		for(byte b : digestBytes){
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}
}
