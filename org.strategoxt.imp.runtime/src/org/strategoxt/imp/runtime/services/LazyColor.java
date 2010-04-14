package org.strategoxt.imp.runtime.services;

import org.eclipse.swt.graphics.Color;

/**
 * A class that lazily loads an SWT color object,
 * ensuring that it is only loaded at an appropriate time
 * (i.e., when in the main thread). Doing so can avoid 
 * deadlocks when Color is instantiated from another thread
 * and the main thread is waiting for a lock.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LazyColor {
	
	private final int red, green, blue;
	
	private Color color;
	
	public LazyColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	public Color get() {
		if (color == null) color = new Color(null, red, green, blue);
		return color;
	}
}
