package com.cheng;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import lu.uni.snt.droidra.ClassDescription;
import lu.uni.snt.droidra.model.StmtKey;
import lu.uni.snt.droidra.model.StmtType;
import lu.uni.snt.droidra.model.StmtValue;
import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class MessageResolver {
	
	public static Map<StmtKey,StmtValue> extractStmtKeyValue(List<Element> elements){
		Map<StmtKey,StmtValue> map = new HashMap<>();
		for(Element element : elements){
			int operation = element.getOperation();
			switch(operation){
			case GlobalValue.OP_METHOD_INVOKE:
				List<StackElement> stackElements = element.getStackElements();
				StackElement calleeElement = stackElements.get(1);
				StackElement callerElement = stackElements.get(2);
				SootMethod callerMethod = Scene.v().getMethod(callerElement.getSootSignature());
				if(callerMethod == null){
					continue;
				}
				Body body = callerMethod.retrieveActiveBody();
				for (Iterator<Unit> iter = body.getUnits().snapshotIterator(); iter.hasNext(); ){
					Stmt stmt = (Stmt) iter.next();
					//System.out.println("linumber:"+stmt.getJavaSourceStartLineNumber()+stmt.toString());
					if(stmt.getJavaSourceStartLineNumber() == callerElement.getLineNumber()
					   && stmt.containsInvokeExpr() && stmt.toString().contains(GlobalValue.METHOD_INVOKE_SIGNATURE)){//找到匹配的Stmt
						StmtKey stmtKey = new StmtKey(callerMethod,stmt);
						StmtValue stmtValue = map.get(stmtKey);
						if(stmtValue == null){
							stmtValue = new StmtValue(StmtType.METHOD_CALL);
							map.put(stmtKey, stmtValue);
						}
						ClassDescription classDescription = new ClassDescription();//可能会有重复的classDescription
						classDescription.cls = element.getClassName();
						classDescription.name = element.getMethodName();
						stmtValue.getClsSet().add(classDescription);
					}
				}
				break;
			default:
				break;
			}
		}
		
		return map;
	}
	public static List<Element> fetchElements(String msgPath){
		File jsonFile = new File(msgPath);
		StringBuffer buffer = new StringBuffer();
		Scanner scanner = null;
		try {
			scanner = new Scanner(jsonFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(scanner.hasNext()){
			buffer.append(scanner.nextLine());
		}
		JSONArray jsonArray = new JSONArray(buffer.toString());
		int length = jsonArray.length();
		System.out.println("number of json objects: " + length);
		List<Element> elements = new LinkedList<>();
		for(int i = 0;i < length;i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			//System.out.println(jsonObject.toString());
			int operation = jsonObject.getInt("operation");
			if(operation == GlobalValue.OP_METHOD_INVOKE
			   || operation == GlobalValue.OP_FIELD_INVOKE
			   || operation == GlobalValue.OP_NEW_INSTANCE){
				String className = jsonObject.getString("class").trim();
				String clsName = toSootType(className);
				String methodName = jsonObject.getString("method").trim();
				String proto = jsonObject.getString("proto").trim();
				List<StackElement> stackElements = new LinkedList<>();
				JSONArray stackElementArray = jsonObject.getJSONArray("stack");
				int size = 5;
				if(stackElementArray.length() < 5){
					size = stackElementArray.length();
				}
				for(int k = 0;k < size;k++){
					String line = stackElementArray.getString(k).trim();
					String[] data = line.split(",");
					String cn = toSootType(data[0].trim());
					String mn = data[1].trim();
					String signature = data[2].trim();
					int lineNumber = Integer.parseInt(data[3].trim());
					//System.out.println(lineNumber);
					System.out.println("****");
					System.out.println(className);
					System.out.println(methodName);
					System.out.println(signature);
					String sootSignature = toFullMethodSignature(data[0].trim(),mn,signature);
					StackElement stackElement = new StackElement(cn,mn,signature,lineNumber,sootSignature);
					stackElements.add(stackElement);
				}
				
				Element element = new Element();
				element.setOperation(operation);
				element.setClassName(clsName);
				element.setMethodName(methodName);
				if(element.getOperation() == GlobalValue.OP_METHOD_INVOKE
				  || element.getOperation() == GlobalValue.OP_NEW_INSTANCE){
					element.setSootSignature(toFullMethodSignature(className,methodName,proto));
				}else if(element.getOperation() == GlobalValue.OP_FIELD_INVOKE){
					element.setSootSignature(proto);
				}
				
				element.setStackElements(stackElements);
				elements.add(element);
			}
		}
		return elements;
	}
	public static String toFullMethodSignature(String className,String methodName,String signature){
		String cls = toSootType(className);
		String proto = toSootMethodSignature(signature,methodName);
		return "<" + cls + ": " + proto + ">";
	}
	public static String toSootMethodSignature(String signature,String methodName){
		String parameters = signature.substring(signature.indexOf('(') + 1,signature.indexOf(')'));
		String returnType = signature.substring(signature.indexOf(')') + 1);
		List<String> parameterList = new LinkedList<>();
		int length = parameters.length();
		int i = 0;
		while(i < length){
			if(parameters.charAt(i) == 'L'){//引用类型
				String type = parameters.substring(i, parameters.indexOf(';', i) + 1);
				parameterList.add(toSootType(type));
				i += type.length();
			}
			else if(parameters.charAt(i) == '['){//数组类型
				int j = i + 1;
				while(parameters.charAt(j) == '[')
					j++;
				if(parameters.charAt(j) == 'L'){//引用数组类型
					while(parameters.charAt(j) != ';')
						j++;
				}
				j++;
				String type = parameters.substring(i, j);
				parameterList.add(toSootType(type));
				i += type.length();
			}else{//基本类型
				parameterList.add(toSootType(parameters.charAt(i) + ""));
				i++;
			}
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(toSootType(returnType))
			  .append(" ")
			  .append(methodName)
			  .append("(");
		int size = parameterList.size();
		if(size > 0){
			buffer.append(parameterList.get(0));
			for(i = 1;i < size;i++){
				buffer.append(",")
				      .append(parameterList.get(i));
			}
		}
		buffer.append(")");
		return buffer.toString();
	}
	
	//将日志中的类型转换成soot中的类型，如将[[Ljava/lang/Object转换成java.lang.Object[][]
	public static String toSootType(String type){
		if(type == null){
			return null;
		}
		int arrayLevels = 0;
		while(type.charAt(arrayLevels) == '['){
			arrayLevels++;
		}
		type = type.substring(arrayLevels);
		
		String typeBase = null;
		if(type.startsWith("L")){
			typeBase = type.substring(1).replace("/", ".");
		}else if(type.length() == 1){
			switch (type.charAt(0)) {
			case 'V':
				typeBase = "void";
				break;
			case 'Z':
				typeBase = "boolean";
				break;
			case 'B':
				typeBase = "byte";
				break;
			case 'S':
				typeBase = "short";
				break;
			case 'I':
				typeBase = "int";
				break;
			case 'J':
				typeBase = "long";
				break;
			case 'F':
				typeBase = "float";
				break;
			case 'D':
				typeBase = "double";
				break;
			default:
				return null;
			}
		}
		if(typeBase.equals("void") && arrayLevels > 0){
			return null;
		}
		String sootType = typeBase;
		for(int i = 0;i < arrayLevels;i++){
			sootType += "[]";
		}
		
		return sootType.replace(";", "");
	}
	
}
