package com.automation.utils;

public class CamelCaseString {
	public static String toCamelCase(final String init) {
		if (init == null)
			return null;

		final StringBuilder ret = new StringBuilder(init.length());

		for (final String word : init.split(" ")) {
			if (!word.isEmpty()) {
				ret.append(word.substring(0, 1).toUpperCase());
				ret.append(word.substring(1).toLowerCase());
			}
		}

		return ret.toString();
	}

	public static void main(String[] args) {
		String string = "hi tHiS is   SomE Statement";
		System.out.println(toCamelCase(string));
	}
}
