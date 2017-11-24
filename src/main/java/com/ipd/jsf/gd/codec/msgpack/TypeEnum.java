/**
 * Copyright 2004-2048 .
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.codec.msgpack;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * 基本类型枚举
 * 
 * Class名称和编号的映射，减少序列化结果的长度
 *
 */
public class TypeEnum {
    
	private static Map<String, Class<?>> indexMap = new HashMap<String, Class<?>>();
	
	private static Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
	
	static {
		classMap.put(java.lang.Boolean.class, "0");
		classMap.put(java.lang.Byte.class, "1");
		classMap.put(java.lang.Short.class, "2");
		classMap.put(java.lang.Integer.class, "3");
		classMap.put(java.lang.Long.class, "4");
		classMap.put(java.lang.Float.class, "5");
		classMap.put(java.lang.Double.class, "6");
		classMap.put(java.math.BigInteger.class, "7");
		classMap.put(java.lang.Character.class, "8");
		classMap.put(java.lang.String.class, "9");
		classMap.put(java.nio.ByteBuffer.class, "10");
		classMap.put(java.math.BigDecimal.class, "11");
		classMap.put(java.util.Date.class, "12");
		classMap.put(boolean.class, "13");
		classMap.put(byte.class, "14");
		classMap.put(short.class, "15");
		classMap.put(int.class, "16");
		classMap.put(long.class, "17");
		classMap.put(float.class, "18");
		classMap.put(double.class, "19");
		classMap.put(char.class, "20");
		classMap.put(java.lang.String[].class, "21");
		classMap.put(boolean[].class, "22");
		classMap.put(byte[].class, "23");
		classMap.put(short[].class, "24");
		classMap.put(int[].class, "25");
		classMap.put(long[].class, "26");
		classMap.put(float[].class, "27");
		classMap.put(double[].class, "28");
		classMap.put(char[].class, "29");
		classMap.put(java.util.List.class, "30");
		classMap.put(java.util.ArrayList.class, "31");
		classMap.put(java.util.Set.class, "32");
		classMap.put(java.util.HashSet.class, "33");
		classMap.put(java.util.Map.class, "34");
		classMap.put(java.util.HashMap.class, "35");
		classMap.put(java.sql.Date.class, "36");
		classMap.put(Time.class, "37");
		classMap.put(Timestamp.class, "38");
		
		indexMap.put("0", java.lang.Boolean.class);
		indexMap.put("1", java.lang.Byte.class);
		indexMap.put("2", java.lang.Short.class);
		indexMap.put("3", java.lang.Integer.class);
		indexMap.put("4", java.lang.Long.class);
		indexMap.put("5", java.lang.Float.class);
		indexMap.put("6", java.lang.Double.class);
		indexMap.put("7", java.math.BigInteger.class);
		indexMap.put("8", java.lang.Character.class);
		indexMap.put("9", java.lang.String.class);
		indexMap.put("10", java.nio.ByteBuffer.class);
		indexMap.put("11", java.math.BigDecimal.class);
		indexMap.put("12", java.util.Date.class);
		indexMap.put("13", boolean.class);
		indexMap.put("14", byte.class);
		indexMap.put("15", short.class);
		indexMap.put("16", int.class);
		indexMap.put("17", long.class);
		indexMap.put("18", float.class);
		indexMap.put("19", double.class);
		indexMap.put("20", char.class);
		indexMap.put("21", java.lang.String[].class);
		indexMap.put("22", boolean[].class);
		indexMap.put("23", byte[].class);
		indexMap.put("24", short[].class);
		indexMap.put("25", int[].class);
		indexMap.put("26", long[].class);
		indexMap.put("27", float[].class);
		indexMap.put("28", double[].class);
		indexMap.put("29", char[].class);
		indexMap.put("30", java.util.List.class);
		indexMap.put("31", java.util.ArrayList.class);
		indexMap.put("32", java.util.Set.class);
		indexMap.put("33", java.util.HashSet.class);
		indexMap.put("34", java.util.Map.class);
		indexMap.put("35", java.util.HashMap.class);
		indexMap.put("36", java.sql.Date.class);
		indexMap.put("37", Time.class);
		indexMap.put("38", Timestamp.class);
	}
    // 普通方法  
    public static Class<?> getType(String index) {
        return indexMap.get(index);
    }
    
    public static String getIndex(Class<?> type){
    	return classMap.get(type);
    }
}