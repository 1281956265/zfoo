package com.zfoo.util.gaea;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;


/**
 * 一些日期,时间相关的判断方法
 *
 */
public class XDBTimeUtils {

	public static final long MINUTE_SECOND = 60L;
	public static final long SECOND_MILLS = 1000L;
	public static final long MINUTE_MILLS = SECOND_MILLS * 60;
	public static final long HOUR_MILLS = MINUTE_MILLS * 60;
	public static final long DAY_MILLS = HOUR_MILLS * 24;
	public static final long WEEK_MILLS = DAY_MILLS * 7;
	
	public static final long TIME_ZONE_OFFSET = Calendar.getInstance().getTimeZone().getRawOffset();  // 时区导致的差值
	private static final ZoneOffset zone;
	
	public static final long ADD_MILLS = DAY_MILLS * 3;  // Java时间的第一天（1970.1.1）是周四，这个是把一周的前三天补全的时间（以周一为第一天）
	
	/** yyyy-MM-dd HH:mm:ss */
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat0 = new ThreadLocal<SimpleDateFormat>();
	/** yyyy-MM-dd */
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat1 = new ThreadLocal<SimpleDateFormat>();
	/** yyyy年M月d日   HH:mm:ss */
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat2 = new ThreadLocal<SimpleDateFormat>();
	/** yyyy-MM-dd HH-mm-ss */
	private static final ThreadLocal<SimpleDateFormat> simpleDateFormat3 = new ThreadLocal<SimpleDateFormat>();


	private static final Map<Integer, Function<Random, Integer>> MONTH_RANDOM_DAY = new HashMap<>();

	static {
		Clock clock = Clock.systemDefaultZone();
		Instant now = clock.instant();
		zone = clock.getZone().getRules().getOffset(now);

		Function<Random, Integer> bigMonth = (r) -> 1 + r.nextInt(31);
		Function<Random, Integer> smallMonth = (r) -> 1 + r.nextInt(30);
		Function<Random, Integer> february = (r) -> 1 + r.nextInt(28);
		MONTH_RANDOM_DAY.put(1, bigMonth);
		MONTH_RANDOM_DAY.put(3, bigMonth);
		MONTH_RANDOM_DAY.put(5, bigMonth);
		MONTH_RANDOM_DAY.put(7, bigMonth);
		MONTH_RANDOM_DAY.put(8, bigMonth);
		MONTH_RANDOM_DAY.put(10, bigMonth);
		MONTH_RANDOM_DAY.put(12, bigMonth);
		MONTH_RANDOM_DAY.put(4, smallMonth);
		MONTH_RANDOM_DAY.put(6, smallMonth);
		MONTH_RANDOM_DAY.put(9, smallMonth);
		MONTH_RANDOM_DAY.put(11, smallMonth);
		MONTH_RANDOM_DAY.put(2, february);
	}

	public static int randomDayOfMonth(int month, Random random) {
		return MONTH_RANDOM_DAY.get(month).apply(XDBMiscUtil.getRandom(random));
	}

	public static int randomDayOfMonth(int month) {
		return MONTH_RANDOM_DAY.get(month).apply(XDBMiscUtil.getRandom());
	}

	/**
	 * 将time偏移offsetTime毫秒再计算与1970-01-01 00:00:00相差的天数
	 */
	public static int getOffsetDay(long time, long offsetTime) {
		return getCurrentDay(time + offsetTime);
	}
	
	public static long getCurrentHour(long time) {
		return (time + TIME_ZONE_OFFSET) / HOUR_MILLS;
	}
	
	public static int getCurrentDay(long time) {
		return XDBConvUtil.toInt((time + TIME_ZONE_OFFSET) / DAY_MILLS);
	}

	public static int getCurrentSeconds(long time) {
		return XDBConvUtil.toInt(time / SECOND_MILLS);
	}

	public static int getCurrentWeek(long time) {
		return XDBConvUtil.toInt((time + TIME_ZONE_OFFSET + ADD_MILLS) / WEEK_MILLS) + 1;
	}
	
	/**
	 * @return 获取偏移后的时间
	 * @param time	当前时间
	 * @param n				清数据的时间点
	 * */
	public static long getOffSetTime(long time, int n) {
		long offset = -n * HOUR_MILLS;
		long finalTime = time + offset;
		if(finalTime < 0) {
			finalTime = 0;
		}
		return finalTime;
	}
	/**
	 * 每月1号 n点刷新的判断
	 */
	public static boolean checkPassMonth(long time, int n) {
		long nowOffsetTime = XDBTimeUtils.getOffSetTime(System.currentTimeMillis(), n);
		long lastAcceptOffsetTime = XDBTimeUtils.getOffSetTime(time, n);
		return !XDBTimeUtils.inTheSameMonth(nowOffsetTime, lastAcceptOffsetTime);
	}
	
	/**
	 * 每周一n点刷新的判断
	 */
	public static boolean checkPassWeek(long time, int n) {
		long nowOffsetTime = XDBTimeUtils.getOffSetTime(System.currentTimeMillis(), n);
		long lastAcceptOffsetTime = XDBTimeUtils.getOffSetTime(time, n);
		return !XDBTimeUtils.inTheSameWeek(nowOffsetTime, lastAcceptOffsetTime);
	}
	
	/**
	 * 是否可以做N点清除数据的操作
	 * 
	 * @param lastClearTime	上次清数据的时间
	 * @param n				清数据的时间点
	 * */
	public static boolean canClearAtN(long lastClearTime, int n) {
		if(0 == lastClearTime) {
			return true;
		}
		
		long now = System.currentTimeMillis();
		return !inTheSameDay(getOffSetTime(now, n), getOffSetTime(lastClearTime, n));
	}
	
	public static boolean inTheSameHour(long firstT, long secondT) {
		return getCurrentHour(firstT) == getCurrentHour(secondT);
	}
	
	public static boolean inTheSameDay(long firstT, long secondT) {
		if (getCurrentDay(firstT) == getCurrentDay(secondT))
			return true;
		
		return false;
	}

	/**
	 * 距离上次刷新时间相差多少天
	 */
	public static int getDaysBetweenAtN(long lastClearTime, int n, long endTime) {
		if(0 == lastClearTime) {
			return 0;
		}
		return getCurrentDay(getOffSetTime(endTime, n)) - getCurrentDay(getOffSetTime(lastClearTime, n));
	}

	/**
	 * 距离上次刷新时间相差多少周
	 */
	public static int getWeeksBetweenAtN(long lastClearTime, int n, long endTime) {
		if(0 == lastClearTime) {
			return 0;
		}
		return getCurrentWeek(getOffSetTime(endTime, n)) - getCurrentWeek(getOffSetTime(lastClearTime, n));
	}
	
	public static boolean inTheSameWeek(long firstT, long secondT) {
		if (getCurrentWeek(firstT) == getCurrentWeek(secondT))
			return true;
		
		return false;
	}
	
	// 获取某天的第一秒 add by lc
	public static long getDayFirstSecond(long time) {
		return time - (time + TIME_ZONE_OFFSET) % DAY_MILLS;
	}
	
	// 获取某天的最后一秒
	public static long getDayLastSecond(long time) {
		return getDayFirstSecond(time) + DAY_MILLS;
	}
	
	// 获取某周的第一秒（以 周一算） add by lc
	public static long getWeekFirstSecond(long time) {
		return time - (time + TIME_ZONE_OFFSET + ADD_MILLS) % WEEK_MILLS;
	}
	
	// 获取某周的最后一秒
	public static long getWeekLastSecond(long time) {
		return getWeekFirstSecond(time) + WEEK_MILLS;
	}
	
	//获取时间是周几
	public static int getCurrentWeekDay(long time) {
		return (int)((time - getWeekFirstSecond(time)) / DAY_MILLS + 1);
	}
	
	// 获取该月的第一秒
	public static long getMonthFirstSecond(long time) {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	// 获取该月的最后一秒
	public static long getMonthLastSecond(long time) {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	
	public static int getMonth_of_year(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.MONTH) + 1;
	}
	
	public static int getDay_of_month(long time){
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	public static long getMonthFirstSecondOffset(long time, int n) {
		
		long firstTime = getMonthFirstSecond(time);
		long offsetMills = n * HOUR_MILLS;
		if(time - firstTime <= offsetMills)  // time是这个月的第一天前n个小时 应该算在上个月
			time -= offsetMills;
		return getMonthFirstSecond(time) + offsetMills;
	}
	
	public static long getMonthLastSecondOffset(long time, int n) {
		
		long firstTime = getMonthFirstSecond(time);
		long offsetMills = n * HOUR_MILLS;
		if(time - firstTime <= offsetMills)  // time是这个月的第一天前n个小时 应该算在上个月
			time -= offsetMills;
		return getMonthLastSecond(time) + offsetMills;
	}
	
	public static boolean inTheSameMonth(long firstT, long secondT) {
		
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
			return true;
		
		return false;
	}
	
	public static boolean isLastDayOfMonth(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.DAY_OF_MONTH) == calendar
				.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	public static String formatTimeByType(long time, String format){
		
		String myFormat = null == format ? "yyyy年M月d日    HH:mm:ss" : format;
		SimpleDateFormat fomat = new SimpleDateFormat(myFormat);
		return fomat.format(time);
	}
	
	public static boolean inTheSameYear(long firstT, long secondT){
		final Calendar cal1 = Calendar.getInstance();
		final Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(firstT);
		cal2.setTimeInMillis(secondT);
		
		if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR))
			return true;
		return false;
	}
	
	public static int getHourOfDay(long time) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	/**
	 * 获取当前月一共多少天
	 * */
	public static int getMaxDayOfMonth(long time){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}
	
	/**
	 * 判断当前时间是不是在给定的时间段之间     必须是当天的
	 * end > begin  [0:00  ~ 24:00]
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isInTimeSubsection(String begin, String end){
		try{
			begin = begin.replace("：", ":");
			end = end.replace("：", ":");
			
			String[] beginArray = begin.split(":");
			String[] endArray = end.split(":");
			if(beginArray.length<2 || endArray.length <2){
				throw new RuntimeException("时间段格式错误：");
			}
			
			int beginHour = Integer.parseInt(beginArray[0]);
			int beginMin = Integer.parseInt(beginArray[1]);
			int endHour = Integer.parseInt(endArray[0]);
			int endMin = Integer.parseInt(endArray[1]);
			
			if(beginHour>endHour ||
					beginHour<0 || beginHour>24 ||
					endHour<0 || endHour > 24){
				throw new RuntimeException("结束时间段 应当大于开始时间段");
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.clear(Calendar.HOUR_OF_DAY);
			calendar.clear(Calendar.MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, beginHour);
			calendar.set(Calendar.MINUTE, beginMin);
			long startTime = calendar.getTimeInMillis();
			
			calendar.clear(Calendar.HOUR_OF_DAY);
			calendar.clear(Calendar.MINUTE);
			calendar.set(Calendar.HOUR_OF_DAY, endHour);
			calendar.set(Calendar.MINUTE, endMin);
			long endTime = calendar.getTimeInMillis();
			long now = System.currentTimeMillis();
			if(startTime <=now  && endTime>=now){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 判断当前时间是不是在给定的时间段之间     必须是当天的
	 * end > begin  [0:00:00  ~ 24:00:00]
	 * 当begin和end时间是0或者-1的时候表示没有时间限制
	 * @param begin
	 * @param end
	 * @return
	 */
	public static boolean isBetweenTime(String begin, String end){
		try{
			if(isNullTimeStr(begin) && isNullTimeStr(end)) { //没有时间限制
				return true;
			}
			
			if(isNullTimeStr(begin)) { //没有开始时间限制
				begin = "0:00:00";
			}
			
			if(isNullTimeStr(end)) { //没有结束时间限制
				end = "24:00:00";
			}
			
			begin = begin.replace("：", ":");
			end = end.replace("：", ":");
			
			String[] beginArray = begin.split(":");
			String[] endArray = end.split(":");
			if(beginArray.length<3 || endArray.length <3){
				throw new RuntimeException("时间段格式错误：");
			}
			
			int beginHour = Integer.parseInt(beginArray[0]);
			int beginMin = Integer.parseInt(beginArray[1]);
			int beginSecond = Integer.parseInt(beginArray[2]);
			int endHour = Integer.parseInt(endArray[0]);
			int endMin = Integer.parseInt(endArray[1]);
			int endSecond = Integer.parseInt(endArray[2]);
			
			if(beginHour>endHour ||
					beginHour<0 || beginHour>24 ||
					endHour<0 || endHour > 24){
				throw new RuntimeException("结束时间段 应当大于开始时间段");
			}
			
			Calendar calendar = Calendar.getInstance();
			calendar.clear(Calendar.HOUR_OF_DAY);
			calendar.clear(Calendar.MINUTE);
			calendar.clear(Calendar.SECOND);
			calendar.set(Calendar.HOUR_OF_DAY, beginHour);
			calendar.set(Calendar.MINUTE, beginMin);
			calendar.set(Calendar.SECOND, beginSecond);
			long startTime = calendar.getTimeInMillis();
			
			calendar.clear(Calendar.HOUR_OF_DAY);
			calendar.clear(Calendar.MINUTE);
			calendar.clear(Calendar.SECOND);
			calendar.set(Calendar.HOUR_OF_DAY, endHour);
			calendar.set(Calendar.MINUTE, endMin);
			calendar.set(Calendar.SECOND, endSecond);
			long endTime = calendar.getTimeInMillis();
			long now = System.currentTimeMillis();
			if(startTime <=now  && endTime>=now){
				return true;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * 传入一个开始时间和结束时间，判断当前时间是否在之间
	 * @param startTime 开始时间 =null("")表示没有开始时间
	 * @param endTime 结束时间 =null("")表示没有结束时间
	 * @return
	 */
	public static boolean isRunning(String startTime, String endTime) {
		return isRunning(startTime, endTime, 0);
	}
	
	public static boolean isRunning(String startTime, String endTime, int formatType) {
		long now = System.currentTimeMillis();
		try {
			if(isNullTimeStr(startTime)) {//没有开始时间
				if(isNullTimeStr(endTime)) {//没有结束时间
					return true;
				} else if(now <= parseDate(endTime, formatType)) {//在结束时间之前
					return true;
				} else {//在结束时间之后
					return false;
				}
			} else {//有开始时间
				if(isNullTimeStr(endTime)) {//没有结束时间
					if(now >= parseDate(startTime, formatType)) {//在开始时间之后
						return true;
					} else {//在开始时间之前
						return false;
					}
				} else {//有结束时间
					if(now < parseDate(startTime, formatType)
							|| now > parseDate(endTime, formatType)) {
						return false;
					} else {
						return true;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean isNullTimeStr(String timeStr) {
		return (timeStr == null || timeStr.length() == 0 || "0".equals(timeStr) || "-1".equals(timeStr));
	}
	
	public static String getTodayStr(long cur) {
		return getTimeStr(cur, 1);
	}
	
	public static String getTimeStr(long cur, int formatType) {
		SimpleDateFormat format = getDateFormat(formatType);
		return format.format(new Date(cur));
	}
	
	public static long parseDate(String str) {
		return parseDate(str, 0);
	}
	
	public static long parseDate(String str, int formatType) {
		SimpleDateFormat format = getDateFormat(formatType);
		try {
			str = str.replace("：", ":");
			return format.parse(str).getTime();
		} catch (Exception e) {
			throw new RuntimeException("时间格式错误：" + str, e);
		}
	}
	
	/**
	 * @param formatType 0 1 2 3分别对应的格式  详见格式变量的注释
	 */
	public static SimpleDateFormat getDateFormat(int formatType) {
		SimpleDateFormat format = null;
		switch (formatType) {
		case 1:
			format = simpleDateFormat1.get();
			if (null == format) {
				format = new SimpleDateFormat("yyyy-MM-dd");
				simpleDateFormat1.set(format);
			}
			break;
			
		case 2:
			format = simpleDateFormat2.get();
			if (null == format) {
				format = new SimpleDateFormat("yyyy年M月d日   HH:mm:ss");
				simpleDateFormat2.set(format);
			}
			break;
			
			
		case 3:
			format = simpleDateFormat3.get();
			if (null == format) {
				format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
				simpleDateFormat3.set(format);
			}
			break;
			
		default:
			format = simpleDateFormat0.get();
			if (null == format) {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				simpleDateFormat0.set(format);
			}
			break;
		}
		return format;
	}
	
	/**
	 * 返回2个日期之间的月数
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getMonthSpace(long time1, long time2){
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTimeInMillis(time1);
		calendar2.setTimeInMillis(time2);
		
		int year = calendar1.get(Calendar.YEAR)-calendar2.get(Calendar.YEAR);
		int mounth = calendar1.get(Calendar.MONTH) - calendar2.get(Calendar.MONTH);
		
		int result = year *12 + mounth;
		return Math.abs(result);
	}
	
	
	/**
	 * 获取2个日期之间的天数
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getDaysBetween(long time1, long time2){
		
		int subDays =  XDBConvUtil.toInt((time2 - time1) / DAY_MILLS);
		return Math.abs(subDays);
	}

//	/**
//	 * 获取2个日期跨越的天数，包括2个日期所在的天
//	 * @param time1
//	 * @param time2
//	 * @return
//	 */
//	public static int getCrossDays(long time1, long time2){
//		int clearTime = EnumerManager.getClearTime();
//		long offSetTime1 = getOffSetTime(time1, clearTime);
//		long offSetTime2 = getOffSetTime(time2, clearTime);
//		long firstSecond1 = getDayFirstSecond(offSetTime1);
//		long firstSecond2 = getDayFirstSecond(offSetTime2);
//		return getDaysBetween(firstSecond1, firstSecond2) + 1;
//	}

	/**
	 * 获取2个日期之间的时间数 向上取整
	 * @param time1
	 * @param time2
	 * @return
	 */
	public static int getHoursBetween(long time1, long time2) {
		int hours = 0;
		long diff = time2 - time1;
		if(diff <= XDBTimeUtils.HOUR_MILLS) {
			hours = 1;
			return hours;
		} 
		
		long diffhour = diff / XDBTimeUtils.HOUR_MILLS;
		if(diff % XDBTimeUtils.HOUR_MILLS > 0) {
			hours += 1;	
		}
		hours += diffhour;
		
		return hours;
	}
	
	/**
	 * 获取一天内时间段的描述
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 每日12:00到14:00
	 */
	public static String getDayTimePeriodString(long start, long end) {
		
		if(start > end)
			return null;
		
		if(end >  XDBTimeUtils.DAY_MILLS)
			return null;
		
		return "每日" + getClockTime(start) + "到" + getClockTime(end);
	}
	
	/**
	 * 获取一周内时间段的描述
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return 如：周二12:00到周三14:00
	 */
	public static String getWeekPeriodTimeString(long start, long end) {
		
		if(start > end)
			return null;
		
		if(end >  XDBTimeUtils.WEEK_MILLS)
			return null;
		
		String startStr = getWeekClockTime(start);
		String endStr = getWeekClockTime(end);
		
		if(startStr.substring(0, 2).equals(endStr.substring(0,2)))
			endStr = endStr.substring(2);  // 如果是同一天，省略后一个周X的描述
		
		return startStr+"到"+endStr;
	}
	
	/**
	 * 从时间的毫秒值到时钟值，精确到分
	 * @param time  时间的毫秒值
	 * @return 如：23:05 
	 */
	public static String getClockTime(long time) {
		
		if(time >= XDBTimeUtils.DAY_MILLS)
			return null;
		
		long hourNum = time / XDBTimeUtils.HOUR_MILLS;
		long hourRet = time % XDBTimeUtils.HOUR_MILLS;
		long minuteNum = hourRet / XDBTimeUtils.MINUTE_MILLS;
		
		return String.format("%02d:%02d", hourNum, minuteNum);
	}
	
	/**
	 * 从毫秒值到周几时钟值，精确到分
	 * @param time
	 * @return 如：周三23:05 
	 */
	public static String getWeekClockTime(long time) {
		
		if(time >= XDBTimeUtils.WEEK_MILLS)
			return null;
		
		int daynum = (int)(time /  XDBTimeUtils.DAY_MILLS);
		long dayret = time %  XDBTimeUtils.DAY_MILLS;
		
		String clocktime = getClockTime(dayret);
		switch(daynum) {
		case 0:
			return "周一"+clocktime;
			
		case 1:
			return "周二"+clocktime;
			
		case 2:
			return "周三"+clocktime;
			
		case 3:
			return "周四"+clocktime;
			
		case 4:
			return "周五"+clocktime;
			
		case 5:
			return "周六"+clocktime;
			
		case 6:
			return "周日"+clocktime;
		}
		
		return null;
	}
	
	/**
	 * 获得一段时间的短描述，如: X天X小时X分钟，高级的单位没有时则不存在，例如小于1天则变成X小时X分钟
	 * 
	 * @return
	 */
	public static String getPeriodShortFormat(long period) {
		
		long daynum = period /  XDBTimeUtils.DAY_MILLS;
		long dayret = period %  XDBTimeUtils.DAY_MILLS;
		
		long hournum = dayret / XDBTimeUtils.HOUR_MILLS;
		long hourret = dayret % XDBTimeUtils.HOUR_MILLS;
		
		long minutenum = hourret / XDBTimeUtils.MINUTE_MILLS;
		
		if(daynum > 0)
			return daynum + "天" + hournum + "小时" + minutenum + "分钟";
		else if(hournum > 0)
			return hournum + "小时" + minutenum + "分钟";
		else if(minutenum > 0)
			return minutenum + "分钟";
		else
			return "1分钟";
	}
	
	/**
	 * 根据策划配置制定时间，判断当前时间是否跨周
	 * configTime为距离每日0点的小时数
	 * 当跨周时返回false
	 * */
	public static boolean isBetweenWeek(long time, int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int curWeek = getCurrentWeekByConfig(configTime);
		int week2 = XDBConvUtil.toInt((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1;
		return curWeek == week2; 
	}
	
	/**
	 * 根据策划配置的每周N点为一周的起点，获取当前为一周的第几天
	 * configTime == 1表示周一的1点为本周的第一天。
	 * */
	public static int getWeekDayByConfig(int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int days = (int)(((System.currentTimeMillis() + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / DAY_MILLS) + 1);
		return days % 7;
	}
	/**
	 * 根据策划配置的每周N点为一周的起点，获取time为一周的第几天
	 * @param time
	 * @param configTime == 1表示周一的1点为本周的第一天。
	 * @return
	 */
	public static int getWeekDayByConfig(long time, int configTime) {
		
		long configMillsTime = configTime * HOUR_MILLS;
		int days = (int)(((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / DAY_MILLS) + 1);
		return days % 7;
	}
	
	/**
	 * 根据策划配置，获取当前时间的周数。
	 * @param configTime  每天零点的偏移小时数
	 * */
	public static int getCurrentWeekByConfig(int configTime){
		
		long configMillsTime = configTime * HOUR_MILLS;
		int weekDay = (int)(((System.currentTimeMillis() + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1);
		return weekDay;
	}
	
	/**
	 * 根据策划配置，获取time时间的周数。
	 * 
	 * @param time
	 * @param configTime
	 * @return
	 */
	public static int getCurrentWeekByConfig(long time, int configTime) {

		long configMillsTime = configTime * HOUR_MILLS;
		int weekDay = (int) (((time + TIME_ZONE_OFFSET + ADD_MILLS - configMillsTime) / WEEK_MILLS) + 1);
		return weekDay;
	}
	
	/**
	 * 判断当前时间与给定时间的大小，单位为毫秒
	 * 给定时间大于当前时间，表示超前
	 * */
	public static boolean isHead(long compareTime){
		long now = System.currentTimeMillis();
		return compareTime > now;
	}
	
	/**
	 * 根据配置获取当前的天数
	 * configTime == 8表示早上8点为一天的开始时间
	 * */
	public static int getCurrentDayByConfig(int configTime) {
		int configMillsTime = configTime*60*60*1000;
		long now = System.currentTimeMillis();
		return (int) ((now + TIME_ZONE_OFFSET - configMillsTime) / DAY_MILLS);
	}
	
	/**
	 * 根据配置时间获取当前时间的毫秒值
	 * @param time
	 * @return
	 */
	public static long getCurrentMillisByConfig(String time) {
		return parseDate(getCurrentTimeStringByConfig(time));
	}

	/**
	 * 根据配置表获取时间的字符串
	 * @param time
	 * @return
	 */
	public static String getCurrentTimeStringByConfig(String time) {
		String currentDay = getTodayStr(System.currentTimeMillis());
		String retTimeStr = currentDay + " " +  time;
		return retTimeStr;
	}
	
	
	/**
	 * 指定日期d加上天数
	 * @param d
	 * @param day
	 * @return
	 */
	public static Date addDate(Date d, long day) {
		long time = d.getTime();
		day = day * DAY_MILLS;
		time += day;
		return new Date(time);
	}
	
	/**
	 * 天数转换为毫秒数
	 * @param day 天数
	 * @return 转换后的毫秒数
	 */
	public static long getMillisByDay(int day) {
		return XDBTimeUtils.DAY_MILLS * day;
	}
	
	/**
	 * 获取时间的时分秒数组
	 * @param time
	 * @return
	 */
	public static String[] getTimeArray(String time) {
		time = time.replace("：", ":");
		
		String[] timeArray = time.split(":");
		if(timeArray.length<3){
			throw new RuntimeException("时间段格式错误：");
		}
		return timeArray;
	}
	
	/**
	 * 将当前时间的时分秒置成0
	 * @param time
	 * @return
	 */
	public static long getStandardTimeInMills(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
	
//	/**
//	 * 返回下一个清数据时间点的时间戳
//	 * @param time
//	 * @return
//	 */
//	public static long getNextClearTime(long time) {
//
//		final Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(time);
//		int curHour = cal.get(Calendar.HOUR_OF_DAY);
//
//		int clearHour = EnumerManager.getClearTime();
//		cal.set(Calendar.HOUR_OF_DAY, clearHour);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
//
//		if(curHour < clearHour) {
//			time = cal.getTimeInMillis();
//		} else {
//			time = cal.getTimeInMillis() + XDBTimeUtils.DAY_MILLS;
//		}
//
//		return time;
//	}
//
//    /**
//     * 获取当前时间的上次清除点时间戳
//     * */
//    public static long getBeforeClranTime(long time) {
//        return getNextClearTime(time) - DAY_MILLS;
//    }

	public static long getDelayMills(int hour, int minute, int second) {
		Calendar till = Calendar.getInstance();
		long now = till.getTimeInMillis();
		till.set(Calendar.HOUR_OF_DAY, hour);
		till.set(Calendar.MINUTE, minute);
		till.set(Calendar.SECOND, second);
		long mtill = till.getTimeInMillis();
		return (mtill - now) < 0 ? (mtill - now + XDBTimeUtils.DAY_MILLS) : (mtill - now);
	}

	//计算两个时间戳之间相差多少秒
	public static long getSecondMills(long time1, long time2) {
		return (time2 - time1) / SECOND_MILLS;
	}

	public static LocalDateTime getLocalDate(long mill) {
		return LocalDateTime.ofInstant(new Timestamp(mill).toInstant(), zone);
	}

	/**
	 * 获取Int型日期值
	 *
	 * @param milliTs 毫秒时间戳
	 * @return 如 20211024
	 */
	public static int getDateValue(long milliTs) {
		var dt = getLocalDate(milliTs);
		return dt.getYear() * 10000 + dt.getMonthValue() * 100 + dt.getDayOfMonth();
	}

	/**
	 * 当前日期值
	 */
	public static int nowDateValue() {
		return getDateValue(System.currentTimeMillis());
	}
}
