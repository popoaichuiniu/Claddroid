package com.cheng;

import java.util.List;

public class Element {
	
	private int operation;
	private String className;
	private String methodName;
	private String source;
	private String output;
	private String sootSignature;
	private List<StackElement> stackElements;
	
	@Override
	public String toString(){
		return "[operation:"+operation+",className:" + className + ",signature:"+sootSignature + "]\n"+stackElements;
		
	}
	public int getOperation() {
		return operation;
	}
	public void setOperation(int operation) {
		this.operation = operation;
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
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getOutput() {
		return output;
	}
	public void setOutput(String output) {
		this.output = output;
	}
	public String getSootSignature() {
		return sootSignature;
	}
	public void setSootSignature(String sootSignature) {
		this.sootSignature = sootSignature;
	}
	public List<StackElement> getStackElements() {
		return stackElements;
	}
	public void setStackElements(List<StackElement> stackElements) {
		this.stackElements = stackElements;
	}
}
