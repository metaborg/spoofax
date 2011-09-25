package org.strategoxt.imp.runtime.services;

import org.eclipse.imp.ui.DefaultPartListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;

/**
 * Implements a dropdown button with builder actions.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class BuilderButtonDelegate extends ToolbarButtonDelegate implements IWorkbenchWindowPulldownDelegate {
	
	@Override
	public void init(IWorkbenchWindow window) {
		// Initialized using getMenu()
		window.getPartService().addPartListener(new DefaultPartListener() {
			@Override			
			public void partActivated(IWorkbenchPart part) {
				EditorState editor = EditorState.getActiveEditor();
				if (editor != null) {
					Descriptor d = editor.getDescriptor();
					String caption = d.getBuilderCaption();
					// TODO: Spoofax/1: Transform menu customization for Spoofax/IMP editor builders
					if (caption == null) {
						// set "Transform" as caption
					} else {
						// set caption as caption
					}
				}
			}
		});
	}

	@Override
	public void run(IAction action) {		
		EditorState editor = EditorState.getActiveEditor();
		if (editor == null) {
			openError("No builders defined for this editor");
			return;
		}
		IBuilderMap builders = getBuilders(editor);
		IBuilder builder = builders.get(lastAction);
		if (builder == null && builders.getAll().size() > 0) {
			builder = builders.getAll().iterator().next();
		}
		if (builder == null) {
			openError("No builders defined for this editor");
		} else {
			builder.scheduleExecute(editor, null, null, false);
		}
	}

	@Override
	protected void populateMenu(Menu menu) {
		MenuItem dummy = new MenuItem(menu, SWT.PUSH);
		dummy.setText("No builders defined for this editor");
		
		final EditorState editor = EditorState.getActiveEditor();
		if (editor == null) return;
		
		IBuilderMap builders = getBuilders(editor);
		if (builders.getAll().size() == 0) return;
		
		for (final IBuilder builder : builders.getAll()) {
			IAction action = new Action(builder.getCaption()) {
				@Override
				public void run() {
					lastAction = builder.getCaption();
					builder.scheduleExecute(editor, null, null, false);
				}
			};
			ActionContributionItem item = new ActionContributionItem(action);
			// item.fill(menu, Action.AS_PUSH_BUTTON);
			item.fill(menu, menu.getItemCount());
		}
		// TODO: only add the debug menu-item to the transform menu when it is defined in the language description
		//addDebugModeMenuItem();
		
		dummy.dispose();
	}
	
	private IBuilderMap getBuilders(EditorState editor) {
		IBuilderMap builders;
		try {
			builders = editor.getDescriptor().createService(IBuilderMap.class, editor.getParseController());
		} catch (BadDescriptorException e) {
			Environment.logException("Could not load builder", e);
			openError("Could not load builders");
			throw new RuntimeException(e);
		}
		return builders;
	}
}
