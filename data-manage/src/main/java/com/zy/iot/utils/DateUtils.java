package com.zy.iot.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 时间操作
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
	static final public long SECOND_OF_MS = 1000L;
	static final public long MINUTE_OF_MS = SECOND_OF_MS * 60L;
	static final public long HOUR_OF_MS = MINUTE_OF_MS * 60L;
	static final public long DAY_OF_MS = HOUR_OF_MS * 24L;
	static final public long WEEK_OF_MS = DAY_OF_MS * 7L;
	static final public long NORMAL_YEAR_OF_MS = DAY_OF_MS * 365L;
	static final public long LEAP_YEAR_OF_MS = DAY_OF_MS * 366L;
	
	static final public String FORMAT_BAR_LONG_DATETIME = "yyyy-MM-dd HH:mm:ss";
	static final public String FORMAT_BAR_SHORT_DATETIME = "yy-MM-dd HH:mm:ss";
	static final public String FORMAT_DOT_LONG_DATETIME = "yyyy.MM.dd HH:mm:ss";
	static final public String FORMAT_DOT_SHORT_DATETIME = "yy-MM-dd HH:mm:ss";
	static final public String FORMAT_RFC3399 = "yyyy-MM-dd'T'HH:mm:ss";
	static final public String FORMAT_YYYYMMDD = "yyyyMMdd";
	static final public String FORMAT_YYYYMMDDHHMM = "yyyyMMddHHmm";
	static final public String FORMAT_YYYY_MM_DD_HHMM = "yyyy-MM-dd HH:mm";
	
	static final public String FORMAT_BAR_LONG_DATE = "yyyy-MM-dd";
	static final public String FORMAT_BAR_SHORT_DATE = "yy-MM-dd";
	static final public String FORMAT_DOT_LONG_DATE = "yyyy.MM.dd";
	static final public String FORMAT_DOT_SHORT_DATE = "yy-MM-dd";	
	
	static final public String[] weekNames=
		{
		"星期日",
		"星期一",
		"星期二",
		"星期三",
		"星期四",
		"星期五",
		"星期六"		
		};
	
	static final public String[] weekShortNames=
		{
		"日",
		"一",
		"二",
		"三",
		"四",
		"五",
		"六"
		
		};	
	static final public String FORMAT_TIME = "HH:mm:ss";	

	protected DateUtils() {
	}
	
	/**当前日期对应的星期是本月的第几个星期
	 * @return
	 */
	public static int nowWeekOfMonth(){
		return weekOfMonth(new Date());
	}
	
	public static int weekOfMonth(int y, int m, int d){
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(y, m, d, 0, 0, 0);
		return c.get(Calendar.WEEK_OF_MONTH);
	}
	
	public static int weekOfMonth(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.get(Calendar.WEEK_OF_MONTH);
	}
	
	public static Date onlyDate(Date date)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int y = c.get(Calendar.YEAR);
		int d = c.get(Calendar.DATE);
		int m = c.get(Calendar.MONTH);
		c.clear();
		c.set(y, m, d, 0, 0, 0);
		return c.getTime();
	}

	/**
	 * 将字符串转达换成日期<br>
	 * 日期为java.util.Date，字符串格式如：YYYY-MM-SS或yyyyMMdd<br>
	 * @param str
	 * @param dft
	 * @return Date 
	 */
	static public Date toDate(String fmt, String str, Date dft) {
		try {
			return toDate(fmt, str);
		} catch (Exception e) {
			return dft;
		}
	}
	/**
	 * 将字符串转达换成日期<br>
	 * 日期是在的配置文件中设定的，字符串格式如：YYYY-MM-SS<br>
	 * @param str
	 * @return Date
	 */
	static public Date toDate(String fmt, String str){
		return parse(fmt, str);
	}
	/**
	 * 将字符串转达换成日期时间<br>
	 * 日期时间是在的配置文件中设定的，字符串格式如：yyyyMMddHHmmss<br>
	 * @param str
	 * @return Date
	 */
	static public Date toDateTime(String fmt, String str){
		return parse(fmt, str);
	}
	/**
	 * 得到当前日期的方法<br>
	 * @return Date
	 */
	static public Date now() {
		return new Date();
	}

	/**
	 * 得到当前日期字符串的方法<br>
	 * @return String
	 */
	static public  String nowToString() {
		return format(FORMAT_BAR_LONG_DATETIME,new Date());
	}

	/**
	 * 返回当前时间，格式为：yyyyMMddHHmmss，比如：20050702123045
	 * 
	 * @return Long
	 */
	static public Long Now() {
		return new Long(toLong());
	}
	/**
	 * 得到当前日期的方法
	 * @return Date
	 */
	static public java.sql.Date getLstDate() {
		return new java.sql.Date(new Date().getTime());
	}

	/**
	 * 返回当前时间，格式为：yyyyMMddHHmmss，比如：20050702123045
	 * 
	 * @return long
	 */
	static public long toLong() {
		return toLong(new Date());
	}
	
	static public DateFormat[] getDateFormats()
	{
		DateFormat[] dfs = {
			new SimpleDateFormat(FORMAT_BAR_LONG_DATETIME),
			new SimpleDateFormat(FORMAT_BAR_SHORT_DATETIME),
			new SimpleDateFormat(FORMAT_DOT_LONG_DATETIME),
			new SimpleDateFormat(FORMAT_DOT_SHORT_DATETIME), 
			new SimpleDateFormat(FORMAT_RFC3399), 
			new SimpleDateFormat(FORMAT_BAR_LONG_DATE),
			new SimpleDateFormat(FORMAT_BAR_SHORT_DATE),
			new SimpleDateFormat(FORMAT_DOT_LONG_DATE),
			new SimpleDateFormat(FORMAT_DOT_SHORT_DATE), 
			new SimpleDateFormat(FORMAT_TIME)
		};
		return dfs;		
	}

	/**
	 * 转化字符串时间为java.util.Date 比如java.util.Date<br>
	 * d=DateUtil.parse('yyyy-MM-dd','2005-07-02');<br>
	 * 
	 * @param fmt
	 *            String
	 * @param dateStr
	 *            String
	 * @throws Exception
	 * @return Date
	 */
	static public Date parse(String fmt, String dateStr)
	{
		if (dateStr==null || dateStr.length()==0)
		{
			return null;
		}
		if (fmt!=null && fmt.length()!=0)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat(
				fmt);
			try {
				return sdf.parse(dateStr);
			} catch (ParseException e) {
			}
		}
		Date check = null;
		DateFormat[] dfs = getDateFormats();
		for (DateFormat df1 : dfs) {
			try {
				check = df1.parse(dateStr);
				if (check != null) {
					break;
				}
			} catch (ParseException ignore) {
			}
		}
		return check;
	}

	/**
	 * 按照fmt所定义的格式进行时间格式话<br>
	 * 		System.out.println(
				format(DateUtils.FORMAT_BAR_LONG_DATE, new Date())<br>
				);<br>
			输出：2013-09-24
	 * @param fmt
	 *            String
	 * @param date
	 *            Date
	 * @return String
	 */
	static public String format(String fmt, Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat(fmt);
		return sdf.format(date);
	}
	
	static public String formatLongTime(String fmt, long date){
		final SimpleDateFormat sdf = new SimpleDateFormat(
				fmt);
		return sdf.format(date);		
	}

	/**
	 * 按照fmt所定义的格式进行时间格式话<br>
	 * 		System.out.println(
				format(DateUtils.FORMAT_BAR_LONG_DATE, 1L)<br>
				);<br>
			输出：1900-01-01
	 * @param fmt
	 *            String
	 * @param date
	 *            Long
	 * @return String
	 */
	static public String format(String fmt, Long date) {
		return format(fmt, date.longValue());
	}

	/**
	 * 按照fmt所定义的格式进行时间格式话
	 * 
	 * @param fmt
	 *            String
	 * @param date
	 *            long
	 * @return String
	 */
	static public String format(String fmt, long date) {
		try {
			return format(fmt, toDate(date));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 按照fmt所定义的格式进行时间格式话
	 * 
	 * @param fmt
	 *            String
	 * @param date
	 *            Integer
	 * @return String
	 */
	static public String format(String fmt, Integer date) {
		return format(fmt, date.intValue());
	}

	/**
	 * 按照fmt所定义的格式进行时间格式话
	 * 
	 * @param fmt
	 *            String
	 * @param date
	 *            int
	 * @return String
	 */
	static public String format(String fmt, int date) {
		try {
			return format(fmt, toDate(date));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 把日期转换为long类型，long的格式为yyyyMMddHHmmss，eg:20050102121330
	 * 
	 * @param date
	 *            Date
	 * @return long
	 */
	static public long toLong(Date date) {
		return date == null ? 19000101000000L : Long.parseLong(format(
				"yyyyMMddHHmmss", date));
	}

	/**
	 * 返回当前日期：格式为：yyyyMMdd，例如：20050102
	 * 
	 * @return int
	 */
	static public int toInteger() {
		return toInteger(new Date());
	}

	/**
	 * 格式为：yyyyMMdd，例如：20050102
	 * 
	 * @param date
	 *            Date
	 * @return int
	 */
	static public int toInteger(Date date) {
		return date == null ? 19000101 : Integer.parseInt(format("yyyyMMdd",
				date));
	}

	/**
	 * 返回本月的第一天：格式为：yyyyMM01，例如今天是20050102，则返回20050101
	 * 
	 * @return int
	 */
	static public int yyyymm01() {
		return yyyymm01(null);
	}

	/**
	 * 返回给定日期Date的当月的第一天，例如：日期为20050102，则返回20050101
	 * 
	 * @param date
	 *            Date
	 * @return int
	 */
	static public int yyyymm01(Date date) {
		return Integer.parseInt(format("yyyyMM01",
				date == null ? (new Date()) : date));
	}

	static public int yyyymm() {
		return yyyymm(null);
	}

	static public int yyyymm(Date date) {
		return Integer.parseInt(format("yyyyMM",
				date == null ? (new Date()) : date));
	}
	/**
	 * 返回当天日期
	 * @return
	 */
	static public Date today()
	{
		Date now=new Date();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(now);
		cal.set(Calendar.AM_PM,Calendar.AM);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	/**
	 * 把时间格式为yyyyMMddHHmmss、yyyyMMdd、HHmmss的时间转化为java.util.Date对象
	 * 
	 * @param date
	 *            Long
	 * @return Date
	 */
	static public Date toDate(Long date) {
		try {
			return toDate(date == null ? 0 : date.longValue());
		} catch (Exception e) {
			return new Date(0);
		}
	}

	/**
	 *yyyyMMdd + y = [yyyy+y] MM dd 如果[yyyy+y] MM dd为非法时间则 dd取[yyyy+y] MM的最后一天
	 * 
	 * @param date
	 *            int
	 * @param yyyy
	 *            int
	 * @throws Exception
	 * @return int
	 */
	static public int incYear(int date, int yyyy) throws Exception {
		return toInteger(Add(toDate(date), yyyy, 0, 0, 0, 0, 0));
	}
	/**
	 * yyyyMMdd + y = [yyyy+y] MM dd 如果[yyyy+y] MM dd为非法时间则 dd取[yyyy+y] MM的最后一天
	 * @param date
	 * @param yyyy
	 * @return
	 * @throws Exception
	 */
	static public Integer incYear(Integer date, int yyyy) throws Exception {
		return Integer.valueOf(toInteger(Add(toDate(date.longValue()), yyyy, 0, 0)));
	}
	/**
	 * yyyyMMdd + y = [yyyy+y] MM dd 如果[yyyy+y] MM dd为非法时间则 dd取[yyyy+y] MM的最后一天
	 * @param date
	 * @param yyyy
	 * @return
	 * @throws Exception
	 */
	static public Long incYear(Long date, int yyyy) throws Exception {
		return Long.valueOf(toLong(Add(toDate(date), yyyy, 0, 0)));
	}
	/**
	 * yyyyMMdd + y = [yyyy+y] MM dd 如果[yyyy+y] MM dd为非法时间则 dd取[yyyy+y] MM的最后一天
	 * @param date
	 * @param yyyy
	 * @return
	 * @throws Exception
	 */
	static public Long incYear(long date, int yyyy) throws Exception {
		return new Long(toLong(Add(toDate(date), yyyy, 0, 0)));
	}
	/**
	 * yyyyMMdd + m = yyyy [MM+m] dd 如果yyyy [MM+m] dd为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param month
	 * @return
	 * @throws Exception
	 */
	static public int incMonth(int date, int month) throws Exception {
		return toInteger(Add(toDate(date), 0, month, 0, 0, 0, 0));
	}
	/**
	 * yyyyMMdd + m = yyyy [MM+m] dd 如果yyyy [MM+m] dd为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param month
	 * @return
	 * @throws Exception
	 */
	static public Integer incMonth(Integer date, int month) throws Exception {
		return new Integer(toInteger(Add(toDate(date.longValue()), 0, month, 0,
				0, 0, 0)));
	}
	/**
	 * yyyyMMdd + m = yyyy [MM+m] dd 如果yyyy [MM+m] dd为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param month
	 * @return
	 * @throws Exception
	 */
	static public Long incMonth(Long date, int month) throws Exception {
		return new Long(toLong(Add(toDate(date), 0, month, 0, 0, 0, 0)));
	}
	/**
	 * yyyyMMdd + m = yyyy [MM+m] dd 如果yyyy [MM+m] dd为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param month
	 * @return
	 * @throws Exception
	 */
	static public long incMonth(long date, int month) throws Exception {
		return toLong(Add(toDate(date), 0, month, 0, 0, 0, 0));
	}
	/**
	 * yyyyMMdd + D = yyyy MM [dd+D] 如果yyyy MM [dd+D]为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param day
	 * @return
	 * @throws Exception
	 */
	static public Long incDay(Long date, int day) throws Exception {
		return new Long(toLong(Add(toDate(date), 0, 0, day, 0, 0, 0)));
	}
	/**
	 * yyyyMMdd + D = yyyy MM [dd+D] 如果yyyy MM [dd+D]为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param day
	 * @return
	 * @throws Exception
	 */
	static public long incDay(long date, int day) throws Exception {
		return toLong(Add(toDate(date), 0, 0, day, 0, 0, 0));
	}
	/**
	 * yyyyMMdd + D = yyyy MM [dd+D] 如果yyyy MM [dd+D]为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param day
	 * @return
	 * @throws Exception
	 */
	static public Integer incDay(Integer date, int day) throws Exception {
		return new Integer(toInteger(Add(toDate(date.longValue()), 0, 0, day,
				0, 0, 0)));
	}
	/**
	 * yyyyMMdd + D = yyyy MM [dd+D] 如果yyyy MM [dd+D]为非法时间则 dd取yyyy [MM+m]的最后一天
	 * @param date
	 * @param day
	 * @return
	 * @throws ParseException
	 */
	static public int incDay(int date, int day) throws ParseException {
		return toInteger(Add(toDate(date), 0, 0, day));
	}
	
	
	/** 针对date，加上多少年（year）多少（month）多少（day)<br>
	 * 		System.out.println(<br>
				format(DateUtils.FORMAT_BAR_LONG_DATE, Add(new Date(), 0, 0, 1))<br>
				);<br>
			在今天的基础上加一天
	 * @param date
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	static public Date Add(Date date, int year, int month,
			int day) {
		return Add(date, year, month, day, 0, 0, 0);
	}

	static public boolean inIt(long value, long bValue, long eValue) {
		return value >= bValue && value <= eValue;
	}
	


	/**针对date，加上多少年（year）多少（month）多少（day) 多少（hour）多少（minute）多少（second）<br>
	 * 		System.out.println(<br>
				format(DateUtils.FORMAT_BAR_LONG_DATETIME, Add(new Date(), 0, 0, 0, 1,0,0))<br>
				);<br>
			在当前时间上加一个小时
	 * @param date
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	static public Date Add(Date date, int year, int month,
			int day, int hour, int minute, int second) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.YEAR, year);
		cal.add(Calendar.MONTH, month);
		cal.add(Calendar.DATE, day);
		cal.add(Calendar.HOUR, hour);
		cal.add(Calendar.MINUTE, minute);
		cal.add(Calendar.SECOND, second);
		return cal.getTime();
	}

	static public Date toDate(long date) throws ParseException {
		// yyyyMMdd
		// yyyyMMddHHmmss
		// yyyyMMddHHmmssSSS
		// HHmmss
		final String str = Long.toString(date);
		final int len = str.length();
		switch (len) {
		case 17:// yyyyMMddHHmmssSSS
		{
			return new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.parse(str);
		}
		case 14:// yyyyMMddHHmmss
		{
			return new SimpleDateFormat("yyyyMMddHHmmss").parse(str);
		}
		case 8: // yyyyMMdd
		{
			return new SimpleDateFormat("yyyyMMdd").parse(str);
		}
		default:// HHmmss
		{
			date = 19000101000000L + date;
			return new SimpleDateFormat("yyyyMMddHHmmss").parse(Long
					.toString(date));
		}
		}
	}

	static public final Date Date(int year, int month, int date) {
		return Date(year, month, date, 0, 0, 0, 0);
	}

	static public final Date Time(int hrs, int min, int sec) {
		return Date(1900, 1, 1, hrs, min, sec, 0);
	}

	static public final Date Date(int year, int month, int day, int hrs,
			int min, int sec, int msec) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(year, month - 1, day, hrs, min, sec);
		cal.set(GregorianCalendar.MILLISECOND, msec);
		return cal.getTime();
	}

	static public int dayOfWeek(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setGregorianChange(date);
		int week_day = cal.get(GregorianCalendar.DAY_OF_WEEK) - 1;
		return week_day == 0 ? 7 : week_day;
	}
	
	static public String dayNameOfWeek(Date date) {
		int r = dayOfWeek(date);
		return weekNames[r];
	}
	
	static public String shortDayNameOfWeek(Date date) {
		int r = dayOfWeek(date);
		return weekShortNames[r];
	}
	
	static public int nowOfWeek(){
		return dayOfWeek(new Date());
	}

	static public Date firstDateOfWeek(Date date) {
		int week = dayOfWeek(date);
		week = week - 1;
		return Add(date, 0, 0, -week, 0, 0, 0);
	}

	static public Date diff(Date d1, Date d2) {
		long diff = d1.getTime() - d2.getTime();
		return new Date(diff<0 ? -diff : diff);
	}

	static public int diffDay(Date d1, Date d2)
	{
		Date dd=diff(d1,d2);
		long diffMillis = dd.getTime();
		return (int) (diffMillis / 1000 / 60 / 60 / 24);
	}
	/**
	 * 计算指定的两个日期之间的天数
	 * 
	 * @param postDate
	 *            之前的时间
	 * @param afterDate
	 *            之后的时间
	 * @return
	 */
	public static final int diffDay2(Date postDate, Date afterDate) {
		Calendar past = Calendar.getInstance();
		past.setTime(postDate);
		Calendar after = new GregorianCalendar();
		after.setTime(afterDate);
		long diffMillis = after.getTimeInMillis() - past.getTimeInMillis();
		return (int) (diffMillis / 1000 / 60 / 60 / 24);
	}
	public static int daysOfMonthly(Date date ) throws Exception{
		final int[] m1 ={31,28,31,30,31,30,31,31,30,31,30,31};
		final int[] m2 ={31,29,31,30,31,30,31,31,30,31,30,31};
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		int y = c.get(GregorianCalendar.YEAR);
		int m = c.get(GregorianCalendar.MONTH);	
		if (c.isLeapYear(y)){
			return m2[m-1];
		}
		return m1[m-1];
	}
	public static int daysOfMonthly(String datestr ) throws Exception{
		Date date = toDate("yyyyMMdd", datestr);
		final int[] m1 ={31,28,31,30,31,30,31,31,30,31,30,31};
		final int[] m2 ={31,29,31,30,31,30,31,31,30,31,30,31};
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);
		int y = c.get(GregorianCalendar.YEAR);
		int m = c.get(GregorianCalendar.MONTH);	
		if (c.isLeapYear(y)){
			return m2[m];
		}
		return m1[m];
	}
	/**
	 * 返回两个日期相差的月数，如果两个日期在同一个月，则返回1
	 * @param date1 
	 * @param date2 
	 * @return int
	 * @throws ParseException
	 */
	public static int getMonthSpace(Date date1, Date date2)
			throws ParseException {

		int result = 0;

//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(date1);
		c2.setTime(date2);

		result = c2.get(Calendar.MONDAY) - c1.get(Calendar.MONTH);

		return result == 0 ? 1 : Math.abs(result);

	}
	/**
	 * 返回两个日期相差的年数，如果两个日期在同一年，则返回1
	 * @param date1 
	 * @param date2 
	 * @return int
	 * @throws ParseException
	 */
	public static int getYearSpace(Date date1, Date date2)
			throws ParseException {
		int result = 0;

		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();

		c1.setTime(date1);
		c2.setTime(date2);

		result = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);

		return result == 0 ? 1 : Math.abs(result);

	}
}
