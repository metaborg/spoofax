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
			
}
