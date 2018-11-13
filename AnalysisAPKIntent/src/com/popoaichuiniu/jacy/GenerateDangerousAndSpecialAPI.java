package com.popoaichuiniu.jacy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.popoaichuiniu.util.MyLogger;
import org.apache.bcel.classfile.Utility;
import org.apache.log4j.Logger;







public class GenerateDangerousAndSpecialAPI {

	private static Logger looger=new MyLogger("AnalysisAPKIntent/GenerateDangerousAndSpecialAPI","exception").getLogger();
	
	public static void main(String[] args) {
		AndroidInfo androidInfo =new AndroidInfo("testSoot.apk",looger);
		
		BufferedWriter bufferedWriter=null;
		try {
			bufferedWriter=new BufferedWriter(new FileWriter(new File("dangerous_api.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		
		Map<String, List<String>> apiPermission= androidInfo.getPermissionDangerousAndSpecialMethods2();
		
		for(Map.Entry<String, List<String>> entry:apiPermission.entrySet())
		{
			String api=entry.getKey();
			System.out.println("*"+api+"*");
			
			String convertedApI=processAPISignatureToString(api);
			try {
				bufferedWriter.write(convertedApI+" "+"-> _SINK_"+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			
			
			
		}
		try {
			bufferedWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
	}

	private static String processAPISignatureToString(String api) {
		String [] temp=api.split(" ");
		assert temp.length==2;
		String classname=temp[0].substring(1,temp[0].length()-1);
		//System.out.println(classname);
		int index1=temp[1].indexOf("(");
		assert index1!=-1;
		 int index2=temp[1].indexOf(")");
		 assert index2!=-1;
		 
		 String methodName=temp[1].substring(0, index1);
		 //System.out.println(methodName);
		 String args=temp[1].substring(index1+1, index2);
		 //System.out.println(args);
		 String ret=temp[1].substring(index2+1,temp[1].length()-1);
		 
		 ret=Utility.signatureToString(ret);
		 //System.out.println(ret);
		 
		 
		 
		
		 
		 
		
		 List<String> arr=new ArrayList<>();
		 
		 int i=0;
		 while(i<args.length())
		 {
			
			 if(args.charAt(i)=='L')
				{ String lstr="";
					while(args.charAt(i)!=';')
					{
						lstr=lstr+args.charAt(i);
						i=i+1;
					}
					
					lstr=lstr+';';
					System.out.println("@@@"+lstr);
					String arg= Utility.signatureToString(lstr,false);
					
					System.out.println("aaa "+arg);
					 
					 arr.add(arg);
				}
			 else if(args.charAt(i)=='[')
			 {
				 String lstr="[";
				 i=i+1;
				 if(args.charAt(i)=='L')
				 {
					 while(args.charAt(i)!=';')
						{
							lstr=lstr+args.charAt(i);
							i=i+1;
						}
						
						lstr=lstr+';';
						System.out.println("@@@"+lstr);
						String arg=Utility.signatureToString(lstr,false);
						System.out.println("aaa "+arg);
						 
						 arr.add(arg);
				 }
				 else
				 {
					 lstr=lstr+args.charAt(i);
					 System.out.println("@@@"+lstr);
					 String arg=Utility.signatureToString(lstr,false);
					 System.out.println("aaa "+arg);
						 
						 arr.add(arg);
				 }
			 }
			 else
			 {
				 System.out.println("@@@"+args.charAt(i));
				 String arg=Utility.signatureToString(String.valueOf(args.charAt(i)),false);
				 
				 System.out.println("aaa "+arg);
				 
				 arr.add(arg);
			 }
			 
			 i=i+1;
			
			 
			 
		 }
		 String str_arg="";
		 for(String arg:arr)
		 {
			 
			 str_arg=str_arg+arg+",";
		 }
		 if(arr.size()>=1)
		 {
			 str_arg=str_arg.substring(0,str_arg.length()-1);
		 }
		 
		 
		 
		 String api_method="<"+classname+": "+ret+" "+methodName+"("+str_arg+")"+">";
		 
		 System.out.println(api_method);
		 return api_method;
		 
		 

		
		
	}

}
