package com.cheng;

import java.util.List;


public class Test {
	public static void main(String[] args) throws Exception{
		List<Element> elements = MessageResolver.fetchElements("");
		for(Element element : elements){
			System.out.println(element);
		}
	}
	
}
