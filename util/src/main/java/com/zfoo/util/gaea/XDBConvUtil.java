package com.zfoo.util.gaea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 数值类型转换的工具类，避免使用强制类型转换带来的未知错误 当转换溢出时，直接抛出异常
 */
public class XDBConvUtil {
	public static Logger logger = LoggerFactory.getLogger(XDBConvUtil.class);
	
	public static long toLong(int v) {
		return v;
	}
	public static long toLong(short v) {
		return v;
	}
	
	public static long toLong(byte v) {
		return v;
	}
	
	public static long toLong(float v) {
		
		if(!(v >= Long.MIN_VALUE && v<= Long.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Float to Long overflow! Float=" + v);
		}
		return (long)v;
	}
	
	public static long toLong(double v) {
		
		if(!(v >= Long.MIN_VALUE && v<= Long.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Double to Long overflow! Float=" + v);
		}
		return (long)v;
	}
	
	
	public static int toInt(long v) {

		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Long to Int overflow! Long=" + v);
		}
		return (int)v;
	}
	
	public static int toInt(short v) {
		return v;
	}
	
	public static int toInt(byte v) {
		return v;
	}
	
	public static int toInt(float v) {
		
		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Float to Int overflow! Float=" + v);
		}
		return (int)v;
	}
	
	public static int toInt(double v) {
		
		if(!(v >= Integer.MIN_VALUE && v<= Integer.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Double to Int overflow! Double=" + v);
		}
		return (int)v;
	}
	
	public static short toShort(int v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Int to Short overflow! Int=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(long v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Long to Short overflow! Long=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(float v) {
		
		if(!(v >= Short.MIN_VALUE && v<= Short.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Float to Short overflow! Float=" + v);
		}
		return (short)v;
	}
	
	public static short toShort(byte v) {
		return v;
	}
	
	public static byte toByte(int v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Int to Byte overflow! Int=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(long v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Long to Byte overflow! Long=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(float v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Float to Byte overflow! Float=" + v);
		}
		return (byte)v;
	}
	
	public static byte toByte(short v) {
		
		if(!(v >= Byte.MIN_VALUE && v<= Byte.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Short to Byte overflow! Short=" + v);
		}
		return (byte)v;
	}
	
	public static float toFloat(double v) {
		
		if(!(v >= -Float.MAX_VALUE && v <= Float.MAX_VALUE)) {
			throw new RuntimeException("XDBConvUtil Double to Float overflow! Double=" + v);
		}
		return (float)v;
	}

	/**
	 * 获取指定小数点后面位数的值，使用的是四舍五入
	 * @param newScale 保留小数点后面几位，newScale=0，则保存小数点后面零位，newScale=2，则保留小数点后面两位
	 * @param v 需要转换的值
	 * @return
	 */
	public static float toFloatWithAssignDecimalPlaces(int newScale, float v) {
		BigDecimal bigDecimal = new BigDecimal(String.valueOf(v));
		return bigDecimal.setScale(newScale, BigDecimal.ROUND_HALF_UP).floatValue();
	}

	/**
	 * 将double的小数部分按照概率取整
	 */
	public static long doubleToLongProperty(Double d) {
		long resultt = d.longValue();
		if (ThreadLocalRandom.current().nextDouble() < (d - resultt)) {
			resultt++;
		}
		return  resultt;
	}
}
