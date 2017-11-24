package com.ipd.org.msgpack.template.builder;

import java.lang.reflect.Field;

import com.ipd.org.msgpack.MessageTypeException;

public class FieldBean {

	private Field field;
	
	private Class<?> declareClass;
	
	public FieldBean(Field field){
		this.field = field;
		this.field.setAccessible(true);
		this.declareClass = field.getDeclaringClass();
	}
	
	public Object get(Object target){
		try {
	    	if(!declareClass.equals(target.getClass())){
	    		target = declareClass.cast(target);
	    	}
	    	return field.get(target);
    	} catch (Exception e) {
            throw new MessageTypeException(e);
    	}
	}
	
	public void set(Object target, Object value){
		try {
	    	if(!declareClass.equals(target.getClass())){
	    		target = declareClass.cast(target);
	    	}
	    	field.set(target, value);
    	} catch (Exception e) {
            throw new MessageTypeException(e);
    	}
	}
	
	public Class<?> getType(){
		return field.getType();
	}
	
	public String getName(){
		return field.getName();
	}
}
