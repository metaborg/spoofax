/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.strategoxt.imp.runtime.services.outline;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.internal.misc.StringMatcher;

/**
 * Abstract class for showing a filtered tree in a lightweight popup dialog.
 */
public abstract class FilteringInfoPopup extends PopupDialog implements DisposeListener {

	/**
	 * The NamePatternFilter selects the elements which
	 * match the given string patterns.
	 */
	protected class NamePatternFilter extends ViewerFilter {

		public NamePatternFilter() {
		}

		/* (non-Javadoc)
		 * Method declared on ViewerFilter.
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if(matcher==null) {
				return true;
			}
			
			TreeViewer treeViewer= (TreeViewer) viewer;

			String matchName = getMatchName(element);
			if (matchName != null && matcher.match(matchName))
				return true;

			return hasUnfilteredChild(treeViewer, element);
		}

		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
			Object[] children = ((ITreeContentProvider) viewer
					.getContentProvider()).getChildren(element);
			for (int i = 0; i < children.length; i++)
				if (select(viewer, element, children[i]))
					return true;
			return false;
		}
	}

	/** The control's text widget */
	private Text filterText;
	/** The control's tree widget */
	private TreeViewer treeViewer;

	/**
	 * Fields that support the dialog menu
	 */
	private Composite viewMenuButtonComposite;

	/**
	 * Field for tree style since it must be remembered by the instance.
	 */
	private int treeStyle;
	private StringMatcher matcher;

	/**
	 * Creates a tree information control with the given shell as parent. The given
	 * styles are applied to the shell and the tree widget.
	 *
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param treeStyle the additional styles for the tree widget
	 * @param showStatusField <code>true</code> iff the control has a status field at the bottom
	 */
	public FilteringInfoPopup(Shell parent, int shellStyle, int treeStyle, boolean showStatusField) {
		super(parent, shellStyle, true, true, true, true, null, null);
		this.treeStyle= treeStyle;
		// Title and status text must be set to get the title label created, so force empty values here. 
		if (hasHeader())
			setTitleText(""); //$NON-NLS-1$
		setInfoText(""); //  //$NON-NLS-1$

		// Status field text can only be computed after widgets are created.
		setInfoText(getStatusFieldText());
	}

	/**
	 * Create the main content for this information control.
	 * 
	 * @param parent The parent composite
	 * @return The control representing the main content.
	 */
	protected Control createDialogArea(Composite parent) {
		treeViewer= createTreeViewer(parent, treeStyle);

		final Tree tree= treeViewer.getTree();
		tree.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)  {
				if (e.character == SWT.ESC)
					dispose();
			}
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		tree.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				Object selectedElement = getSelectedElement();
				close();
                handleElementSelected(selectedElement);
			}
		});

		tree.addMouseMoveListener(new MouseMoveListener()	 {
			public void mouseMove(MouseEvent e) {
				TreeItem[] selectedItems = tree.getSelection();
				TreeItem selectedItem = selectedItems.length == 0 ? null : selectedItems[0];
				TreeItem itemUnderPointer = tree.getItem(new Point(e.x, e.y));
				if (itemUnderPointer != null) {
					if (itemUnderPointer != selectedItem) {
						select(itemUnderPointer);
					} else if (e.y < tree.getItemHeight() / 4) {
						// Scroll up
						Point p= tree.toDisplay(e.x, e.y);
						TreeItem item= (TreeItem) treeViewer.scrollUp(p.x, p.y);
						if (item != null) {
							select(item);
						}
					} else if (e.y > tree.getBounds().height - tree.getItemHeight() / 4) {
						// Scroll down
						Point p= tree.toDisplay(e.x, e.y);
						TreeItem item= (TreeItem) treeViewer.scrollDown(p.x, p.y);
						if (item != null) {
							select(item);
						}
					}
				}
			}

			private void select(TreeItem item) {
				// set selection on viewer instead of directly on tree so that
				// selection listeners are notified
				Object element = item.getData();
				if (element != null) {
					treeViewer.setSelection(new StructuredSelection(element));
				}
			}
		});

		tree.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {

				if (tree.getSelectionCount() < 1)
					return;

				if (e.button != 1)
					return;

				if (tree.equals(e.getSource())) {
					Object o= tree.getItem(new Point(e.x, e.y));
					TreeItem selection= tree.getSelection()[0];
					if (selection.equals(o)) {
						Object selectedElement = getSelectedElement();
						close();
                        handleElementSelected(selectedElement);
					}
				}
			}
		});

		installFilter();

		addDisposeListener(this);
		return treeViewer.getControl();
	}
	
	/**
	 * Creates a tree information control with the given shell as parent. The given
	 * styles are applied to the shell and the tree widget.
	 *
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param treeStyle the additional styles for the tree widget
	 */
	public FilteringInfoPopup(Shell parent, int shellStyle, int treeStyle) {
		this(parent, shellStyle, treeStyle, false);
	}

	protected abstract TreeViewer createTreeViewer(Composite parent, int style);

	/**
	 * Returns the name of the dialog settings section.
	 *
	 * @return the name of the dialog settings section
	 */
	protected abstract String getId();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.PopupDialog#getFocusControl()
	 */
	protected Control getFocusControl() {
		return filterText;
	}

	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}

	/**
	 * Returns <code>true</code> if the control has a header, <code>false</code> otherwise.
	 * <p>
	 * The default is to return <code>false</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if the control has a header
	 */
	protected boolean hasHeader() {
		// default is to have no header
		return false;
	}

	protected Text getFilterText() {
		return filterText;
	}

	protected Text createFilterText(Composite parent) {
		filterText= new Text(parent, SWT.NONE);

		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		GC gc= new GC(parent);
		gc.setFont(parent.getFont());
		FontMetrics fontMetrics= gc.getFontMetrics();
		gc.dispose();

		data.heightHint= Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
		data.horizontalAlignment= GridData.FILL;
		data.verticalAlignment= GridData.CENTER;
		filterText.setLayoutData(data);

		filterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					// return
					Object selectedElement = getSelectedElement();
					close();
					handleElementSelected(selectedElement);
				}
				if (e.keyCode == SWT.ARROW_DOWN)
					treeViewer.getTree().setFocus();
				if (e.keyCode == SWT.ARROW_UP)
					treeViewer.getTree().setFocus();
				if (e.character == 0x1B) // ESC
					dispose();
			}
			public void keyReleased(KeyEvent e) {
				// do nothing
			}
		});

		return filterText;
	}

	protected void createHorizontalSeparator(Composite parent) {
		Label separator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void updateStatusFieldText() {
		setInfoText(getStatusFieldText());
	}

	/**
	 * Handles click in status field.
	 * <p>
	 * Default does nothing.
	 * </p>
	 */
	protected void handleStatusFieldClicked() {
	}

	protected String getStatusFieldText() {
		return ""; //$NON-NLS-1$
	}

	private void installFilter() {
		filterText.setText(""); //$NON-NLS-1$

		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text= ((Text) e.widget).getText();
				int length= text.length();
				if (length > 0 && text.charAt(length -1 ) != '*') {
					text= text + '*';
				}
				setMatcherString(text, true);
			}
		});
		
		getTreeViewer().addFilter(new NamePatternFilter());
	}

	/**
	 * The string matcher has been modified. The default implementation
	 * refreshes the view and selects the first matched element
	 */
	protected void stringMatcherUpdated() {
		// refresh viewer to re-filter
		treeViewer.getControl().setRedraw(false);
		treeViewer.refresh();
		treeViewer.expandAll();
		selectFirstMatch();
		treeViewer.getControl().setRedraw(true);
	}

	/**
	 * Sets the patterns to filter out for the receiver.
	 * <p>
	 * The following characters have special meaning:
	 *   ? => any character
	 *   * => any string
	 * </p>
	 *
	 * @param pattern the pattern
	 * @param update <code>true</code> if the viewer should be updated
	 */
	protected void setMatcherString(String pattern, boolean update) {
		if (pattern.length() == 0) {
			matcher= null;
		} else {
			boolean ignoreCase= pattern.toLowerCase().equals(pattern);
			matcher= new StringMatcher(pattern, ignoreCase, false);
		}
		
		if (update)
			stringMatcherUpdated();
	}

	protected StringMatcher getMatcher() {
		return matcher;
	}

	/**
	 * Implementers can modify
	 *
	 * @return the selected element
	 */
	protected Object getSelectedElement() {
		if (treeViewer == null)
			return null;

		return ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
	}

	abstract protected void handleElementSelected(Object selectedElement);

	/**
	 * Selects the first element in the tree which
	 * matches the current filter pattern.
	 */
	protected void selectFirstMatch() {
		Tree tree= treeViewer.getTree();
		Object element= findElement(tree.getItems());
		if (element != null)
			treeViewer.setSelection(new StructuredSelection(element), true);
		else
			treeViewer.setSelection(StructuredSelection.EMPTY);
	}

	private Object findElement(TreeItem[] items) {
		for (int i= 0; i < items.length; i++) {
			Object element= items[i].getData();
			if (matcher == null)
				return element;

			if (element != null) {
				String label= getMatchName(element);
				if (matcher.match(label))
					return element;
			}

			element= findElement(items[i].getItems());
			if (element != null)
				return element;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInformation(String information) {
		// this method is ignored, see IInformationControlExtension2
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract void setInput(Object information);

	protected void inputChanged(Object newInput, Object newSelection) {
		filterText.setText(""); //$NON-NLS-1$
		treeViewer.setInput(newInput);
		if (newSelection != null) {
			treeViewer.setSelection(new StructuredSelection(newSelection));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			open();
		} else {
			saveDialogBounds(getShell());
			getShell().setVisible(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void dispose() {
		close();
	}

	/**
	 * {@inheritDoc}
	 * @param event can be null
	 * <p>
	 * Subclasses may extend.
	 * </p>
	 */
	public void widgetDisposed(DisposeEvent event) {
		treeViewer= null;
		filterText= null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasContents() {
		return treeViewer != null && treeViewer.getInput() != null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		// ignore
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSizeHint() {
		// return the shell's size - note that it already has the persisted size if persisting
		// is enabled.
		return getShell().getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLocation(Point location) {
		/*
		 * If the location is persisted, it gets managed by PopupDialog - fine. Otherwise, the location is
		 * computed in Window#getInitialLocation, which will center it in the parent shell / main
		 * monitor, which is wrong for two reasons:
		 * - we want to center over the editor / subject control, not the parent shell
		 * - the center is computed via the initalSize, which may be also wrong since the size may 
		 *   have been updated since via min/max sizing of AbstractInformationControlManager.
		 * In that case, override the location with the one computed by the manager. Note that
		 * the call to constrainShellSize in PopupDialog.open will still ensure that the shell is
		 * entirely visible.
		 */
		if (!getPersistBounds() || getDialogSettings() == null)
			getShell().setLocation(location);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setForegroundColor(Color foreground) {
		applyForegroundColor(foreground, getContents());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBackgroundColor(Color background) {
		applyBackgroundColor(background, getContents());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isFocusControl() {
		return treeViewer.getControl().isFocusControl() || filterText.isFocusControl();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFocus() {
		getShell().forceFocus();
		filterText.setFocus();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addFocusListener(FocusListener listener) {
		getShell().addFocusListener(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeFocusListener(FocusListener listener) {
		getShell().removeFocusListener(listener);
	}

	/*
	 * Overridden to insert the filter text into the title and menu area.
	 * 
	 * @since 3.2
	 */
	protected Control createTitleMenuArea(Composite parent) {
		viewMenuButtonComposite= (Composite) super.createTitleMenuArea(parent);

		// If there is a header, then the filter text must be created
		// underneath the title and menu area.

		if (hasHeader()) {
			filterText= createFilterText(parent);
		}

		return viewMenuButtonComposite;
	}

	/*
	 * Overridden to insert the filter text into the title control
	 * if there is no header specified.
	 * @since 3.2
	 */
	protected Control createTitleControl(Composite parent) {
		if (hasHeader()) {
			return super.createTitleControl(parent);
		}
		filterText= createFilterText(parent);
		return filterText;
	}
	
	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#setTabOrder(org.eclipse.swt.widgets.Composite)
	 */
	protected void setTabOrder(Composite composite) {
		if (hasHeader()) {
			composite.setTabList(new Control[] { filterText, treeViewer.getTree() });
		} else {
			viewMenuButtonComposite.setTabList(new Control[] { filterText });
			composite.setTabList(new Control[] { viewMenuButtonComposite, treeViewer.getTree() });
		}
	}

	/**
	 * Returns the name of the given element used for matching. The default
	 * implementation gets the name from the tree viewer's label provider.
	 * Subclasses may override.
	 * 
	 * @param element the element
	 * @return the name to be used for matching
	 */
	protected String getMatchName(Object element) {
		return ((ILabelProvider) getTreeViewer().getLabelProvider()).getText(element);
	}
}
