package com.cheng;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONTest {
	public static void main(String[] args) throws FileNotFoundException{
		String jsonFileName = "/home/cheng/stadyna_result/app-debug_msg.txt";
		File jsonFile = new File(jsonFileName);
		StringBuffer buffer = new StringBuffer();
		Scanner scanner = new Scanner(jsonFile);
		while(scanner.hasNext()){
			buffer.append(scanner.nextLine());
		}
		JSONArray jsonArray = new JSONArray(buffer.toString());
		int length = jsonArray.length();
		System.out.println("number of json objects: " + length);
		for(int i = 0;i < length;i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			//System.out.println(jsonObject.toString());
			int operation = jsonObject.getInt("operation");
			System.out.println("operation: " + operation);
			JSONArray stacks = jsonObject.getJSONArray("stack");
			//System.out.println(stacks.toString());
			int l = stacks.length();
			for(int j = 0;j < l;j++){
				String stack = stacks.getString(j);
				System.out.println(stack);
			}
		}
	}
}
