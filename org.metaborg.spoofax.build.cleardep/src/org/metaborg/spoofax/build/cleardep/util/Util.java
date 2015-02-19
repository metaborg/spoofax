package org.metaborg.spoofax.build.cleardep.util;

import java.lang.reflect.Array;

public class Util{
	public static <T> T[] arrayConcat(T[] ar1, T[] ar2) {
		@SuppressWarnings("unchecked")
		T[] ar = (T[]) Array.newInstance(ar1.getClass().getComponentType(), ar1.length + ar2.length);
		
		System.arraycopy(ar1, 0, ar, 0, ar1.length);
		System.arraycopy(ar2, 0, ar, ar1.length, ar2.length);
		return ar;
	}

	public static <T> T[] arrayAdd(T t, T[] ar) {
		@SuppressWarnings("unchecked")
		T[] arRes = (T[]) Array.newInstance(ar.getClass().getComponentType(), ar.length + 1);
		arRes[0] = t;
		System.arraycopy(ar, 0, arRes, 1, ar.length);
		return arRes;
	}
			
}
