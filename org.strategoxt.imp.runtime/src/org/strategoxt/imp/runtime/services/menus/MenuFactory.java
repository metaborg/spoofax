package org.strategoxt.imp.runtime.services.menus;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.resource.ImageDescriptor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.AbstractServiceFactory;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.menus.model.CustomStrategyBuilder;
import org.strategoxt.imp.runtime.services.menus.model.DebugModeBuilder;
import org.strategoxt.imp.runtime.services.menus.model.IBuilder;
import org.strategoxt.imp.runtime.services.menus.model.IMenuContribution;
import org.strategoxt.imp.runtime.services.menus.model.Menu;
import org.strategoxt.imp.runtime.services.menus.model.Separator;
import org.strategoxt.imp.runtime.services.menus.model.StrategoBuilder;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Oskar van Rest
 */
public class MenuFactory extends AbstractServiceFactory<IMenuList> {

	EditorState derivedFromEditor;
	
	private static final int OPTION_OPENEDITOR = 0;
	private static final int OPTION_REALTIME = 1;
	private static final int OPTION_PERSISTENT = 2;
	private static final int OPTION_META = 3;
	private static final int OPTION_CURSOR = 4;
	private static final int OPTION_SOURCE = 5;
	
	public MenuFactory() {
		super(IMenuList.class, false); // not cached; depends on derived editor relation
	}

	@Override
	public IMenuList create(Descriptor d, SGLRParseController controller) throws BadDescriptorException {
		List<Menu> menus = new LinkedList<Menu>();

		derivedFromEditor = getDerivedFromEditor(controller);

		if (d.isATermEditor() && derivedFromEditor != null)
			addDerivedMenus(menus);

		addMenus(d, controller, menus);
		addCustomStrategyBuilder(d, controller, menus);
		if (Environment.allowsDebugging(d)) // Descriptor allows debugging)
		{
			addDebugModeBuilder(d, controller, menus);
		}
		return new MenuList(menus);
	}

	private void addMenus(Descriptor d, SGLRParseController controller, List<Menu> menus) throws BadDescriptorException {

		// BEGIN: 'Transform' menu backwards compatibility
		ArrayList<IStrategoAppl> builders = collectTerms(d.getDocument(), "Builder");
		for (IStrategoAppl b : builders) {
			String caption = termContents(termAt(b, 0));
			List<String> path = createPath(createPath(Collections.<String> emptyList(), MenusServiceConstants.OLD_LABEL), caption);
			IBuilder builder = createBuilder(b, path, d, controller, new boolean[6]);
			if (builder != null) {
				Menu menu = null;
				for (Menu m : menus) {
					if (m.getCaption().equals(MenusServiceConstants.OLD_LABEL)) {
						menu = m;
					}
				}

				if (menu == null) {
					menu = new Menu(MenusServiceConstants.OLD_LABEL);
					menus.add(0, menu);
				}

				menu.addMenuContribution(builder);
			}
		}
		// END: 'Transform' menu backwards compatibility

		for (IStrategoAppl m : collectTerms(d.getDocument(), "ToolbarMenu")) {

			boolean[] options = new boolean[6];
			addOptions(termAt(m, 1), options);
			
			if (!options[OPTION_META] || d.isDynamicallyLoaded()) {
				String caption = termContents(termAt(m, 0)); // caption = label or icon path
				Menu menu = null;
				if (((IStrategoAppl) termAt(m, 0)).getConstructor().getName().equals("Icon")) {
					String iconPath = caption;
					String pluginPath = d.getBasePath().toOSString();
					File iconFile = new File(pluginPath, iconPath);
					if (iconFile.exists()) {
						try {
							ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(URIUtil.toURL(iconFile.toURI()));
							menu = new Menu(caption, imageDescriptor);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
					else {
						menu = new Menu("Can't find icon '" + iconFile.getAbsolutePath() + "'");
					}
				}
				else {
					menu = new Menu(caption);
				}
	
				List<String> path = createPath(Collections.<String> emptyList(), caption);
				addMenuContribs(menu,termAt(m, 2), path, d, controller, options.clone());
				menus.add(menu);
			}
		}
	}

	private List<String> createPath(List<String> init, String last) {
		List<String> result = new LinkedList<String>(init);
		result.add(last);
		return result;
	}

	private void addMenuContribs(Menu menu, IStrategoTerm menuContribs, List<String> path, Descriptor d, SGLRParseController controller, boolean[] options) throws BadDescriptorException {

		for (IStrategoAppl a : collectTerms(menuContribs, "Action", "Separator", "Submenu")) {
			String cons = a.getConstructor().getName();

			if (cons.equals("Action")) {
				String caption = termContents(termAt(a, 0));
				IBuilder builder = createBuilder(a, createPath(path, caption), d, controller, options.clone());
				if (builder != null) {
					menu.addMenuContribution(builder);
				}
			} else if (cons.equals("Separator")) {
				menu.addMenuContribution(new Separator());
			} else if (cons.equals("Submenu")) {
				addOptions(termAt(a, 1), options);
				
				if (!options[OPTION_META] || d.isDynamicallyLoaded()) {
					String caption = termContents(termAt(a, 0));
					Menu submenu = new Menu(caption);
					addMenuContribs(submenu, termAt(a, 2), createPath(path, caption), d, controller, options.clone());
					if (submenu.getMenuContributions().size() > 0) {
						menu.addMenuContribution(submenu);
					}
				}
			}
		}
		
		// guarantee no separator at start or end and no two consecutive separators 
		// (important when language is not dynamically loaded and not all builders are visible)
		IMenuContribution previous = null;
		Iterator<IMenuContribution> it = menu.getMenuContributions().iterator();
		while (it.hasNext()) {
			IMenuContribution contrib = it.next();
			if (contrib instanceof Separator && (previous == null || previous instanceof Separator)) {
				it.remove();
			}
			else {
				previous = contrib;
			}
		}
		if (previous instanceof Separator) {
			menu.getMenuContributions().remove(previous);
		}
	}

	private IBuilder createBuilder(IStrategoTerm action, List<String> path, Descriptor d, SGLRParseController controller, boolean[] options) throws BadDescriptorException {
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		String strategy = termContents(termAt(action, 1));
		addOptions(termAt(action, 2), options);
		if (!options[OPTION_META] || d.isDynamicallyLoaded())
			return new StrategoBuilder(feedback, path, strategy, options[OPTION_OPENEDITOR], options[OPTION_REALTIME], options[OPTION_CURSOR], options[OPTION_SOURCE], options[OPTION_PERSISTENT], derivedFromEditor);
		else
			return null;
	}

	private void addDerivedMenus(List<Menu> menus) throws BadDescriptorException {
		if (derivedFromEditor != null) {
			addMenus(derivedFromEditor.getDescriptor(), derivedFromEditor.getParseController(), menus);
		}
	}

	private void addCustomStrategyBuilder(Descriptor d, SGLRParseController controller, List<Menu> menus) throws BadDescriptorException {

		if (d.isATermEditor() && derivedFromEditor != null) {
			StrategoObserver feedback = derivedFromEditor.getDescriptor().createService(StrategoObserver.class, controller);
			addCustomStrategyBuilderHelper(menus, feedback);
		} else if (d.isDynamicallyLoaded()) {
			StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
			addCustomStrategyBuilderHelper(menus, feedback);
		}
	}

	private void addCustomStrategyBuilderHelper(List<Menu> menus, StrategoObserver feedback) {
		for (Menu menu : menus) {
			List<String> path = new LinkedList<String>();
			path.add(menu.getCaption());
			path.add(CustomStrategyBuilder.CAPTION);
			menu.addMenuContribution(new Separator());
			menu.addMenuContribution(new CustomStrategyBuilder(path, feedback, derivedFromEditor));
		}
	}

	/**
	 * Adds a Debug Mode Builder, if debug mode is allowed the user can choose to enable stratego
	 * debugging. If debugging is enabled, a new JVM is started for every strategy invoke resulting
	 * in major performance drops. The user can also disable Debug mode, without needing to rebuil
	 * the project.
	 */
	private void addDebugModeBuilder(Descriptor d, SGLRParseController controller, List<Menu> menus) throws BadDescriptorException {
		StrategoObserver feedback = d.createService(StrategoObserver.class, controller);
		for (Menu menu : menus) {
			List<String> path = new LinkedList<String>();
			path.add(menu.getCaption());
			path.add(DebugModeBuilder.getCaption(feedback));
			menu.addMenuContribution(new DebugModeBuilder(feedback, path));
		}
	}

	private EditorState getDerivedFromEditor(SGLRParseController controller) {
		if (controller.getEditor() == null || controller.getEditor().getEditor() == null)
			return null;
		UniversalEditor editor = controller.getEditor().getEditor();
		StrategoBuilderListener listener = StrategoBuilderListener.getListener(editor);
		if (listener == null)
			return null;
		UniversalEditor sourceEditor = listener.getSourceEditor();
		if (sourceEditor == null)
			return null;
		return EditorState.getEditorFor(sourceEditor.getParseController());
	}

	public static void eagerInit(Descriptor descriptor, IParseController parser, EditorState lastEditor) {
		// Refresh toolbar menu commands after rebuilding.
		MenusServiceUtil.refreshToolbarMenuCommands();
	}
	
	private void addOptions(IStrategoTerm options, boolean[] parentOptions) throws BadDescriptorException {
		for (IStrategoTerm option : options.getAllSubterms()) {
			String type = cons(option);
			if (type.equals("OpenEditor")) {
				parentOptions[OPTION_OPENEDITOR] = true;
			} else if (type.equals("RealTime")) {
				parentOptions[OPTION_REALTIME] = true;
			} else if (type.equals("Persistent")) {
				parentOptions[OPTION_PERSISTENT] = true;
			} else if (type.equals("Meta")) {
				parentOptions[OPTION_META] = true;
			} else if (type.equals("Cursor")) {
				parentOptions[OPTION_CURSOR] = true;
			} else if (type.equals("Source")) {
				parentOptions[OPTION_SOURCE] = true;
			} else {
				throw new BadDescriptorException("Unknown builder annotation: " + type);
			}
		}
	}
}
