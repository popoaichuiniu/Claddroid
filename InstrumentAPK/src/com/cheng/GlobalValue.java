package com.cheng;

import java.util.HashSet;
import java.util.Set;

public class GlobalValue {
	
	public static String STADYNA_RESULT = null;
	public static String DEX_DIRECTORY = null;
	public static String WORKSPACE = null;
	
	public final static int OP_NEW_INSTANCE       = 1;
	public final static int OP_METHOD_INVOKE      = 2;
	public final static int OP_DEX_LOAD           = 3;
	public final static int OP_FIELD_INVOKE       = 4;
	
	public final static String METHOD_INVOKE_SIGNATURE = 
			"<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>";
	public final static String CLASS_NEW_INSTANCE_SIGNATURE = 
			"<java.lang.Class: java.lang.Object newInstance()>";
	public final static String CONSTRUCTOR_NEW_INSTANCE_SIGNATURE = 
			"<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>";
	public final static Set<String> FIELD_CALL_SET = new HashSet<>();
	static{
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setBoolean(java.lang.Object,boolean)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setByte(java.lang.Object,byte)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setChar(java.lang.Object,char)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setShort(java.lang.Object,short)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setInt(java.lang.Object,int)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setLong(java.lang.Object,long)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setFloat(java.lang.Object,float)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: void setDouble(java.lang.Object,Double)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: boolean getBoolean(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: byte getByte(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: char getChar(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: short getShort(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: int getInt(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: long getLong(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: float getFloat(java.lang.Object)>");
		FIELD_CALL_SET.add("<java.lang.reflect.Field: double getDouble(java.lang.Object)>");
	}
	
}
