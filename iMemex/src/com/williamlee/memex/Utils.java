package com.williamlee.memex;

import java.util.Arrays;
import java.util.List;

public class Utils {
	private static Long[] arrIntervals = new Long[] {
		60 * 1000L,
		5 * 60 * 1000L,
		30 * 60 * 1000L,
		60 * 60 * 1000L,
		12 * 60 * 60 * 1000L,
		24 * 60 * 60 * 1000L,
		2 * 24 * 60 * 60 * 1000L,
		4 * 24 * 60 * 60 * 1000L,
		7 * 24 * 60 * 60 * 1000L,
		15 * 24 * 60 * 60 * 1000L,
		30 * 24 * 60 * 60 * 1000L,
	};
	private static List<Long> notifIntervals = Arrays.asList(arrIntervals);
	
	public static List<Long> getNotifIntervals() {
		return notifIntervals;
	}
	
	public static void addInterval(Long newInterval, int pos) {
		notifIntervals.add(newInterval);
	}
}
