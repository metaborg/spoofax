package org.strategoxt.imp.runtime.services;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;

/**
 * A TextAttribute class that supports {@ref LazyColor} colors.
 * 
 * @see LazyColor
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LazyTextAttribute extends TextAttribute {
	
	private final LazyColor foreground;
	
	private final LazyColor background;

	public LazyTextAttribute(LazyColor foreground, LazyColor background, int style) {
		super(null, null, style);
		this.foreground = foreground;
		this.background = background;
	}
	
	@Override
	public Color getForeground() {
		return foreground == null ? null : foreground.get();
	}
	
	@Override
	public Color getBackground() {
		return background == null ? null : background.get();
	}
	
}
