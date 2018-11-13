package com.cheng;

public class StackElement {
	private String className;
	private String methodName;
	private String signature;
	private int lineNumber;
	private String sootSignature;
	
	public StackElement(String className,String methodName,String signature,int lineNumber,String sootSignature){
		this.className = className;
		this.methodName = methodName;
		this.signature = signature;
		this.lineNumber = lineNumber;
		this.sootSignature = sootSignature;
	}

	@Override
	public String toString(){
		return "StackElement:" + "{className:" + className + ",methodName:" + methodName +",lineNumber:" + lineNumber+",signature:" + signature+
				",sootSignature:" + sootSignature + "}";
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getSootSignature() {
		return sootSignature;
	}
	public void setSootSignature(String sootSignature) {
		this.sootSignature = sootSignature;
	}
}
