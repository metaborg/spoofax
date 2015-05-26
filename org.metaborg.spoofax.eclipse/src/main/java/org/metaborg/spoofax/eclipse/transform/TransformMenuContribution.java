package org.metaborg.spoofax.eclipse.transform;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.transform.stratego.menu.Action;
import org.metaborg.spoofax.core.transform.stratego.menu.Menu;
import org.metaborg.spoofax.core.transform.stratego.menu.MenusFacet;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEclipseEditor;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;

public class TransformMenuContribution extends CompoundContributionItem implements IWorkbenchContribution {
    public static final String transformId = SpoofaxPlugin.id + ".command.transform";
    public static final String actionNameParam = "action-name";

    private static final Logger logger = LoggerFactory.getLogger(TransformMenuContribution.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;
    private final ISpoofaxEditorListener latestEditorListener;

    private IServiceLocator serviceLocator;


    public TransformMenuContribution() {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageIdentifier = injector.getInstance(ILanguageIdentifierService.class);
        this.latestEditorListener = injector.getInstance(ISpoofaxEditorListener.class);
    }


    @Override public void initialize(IServiceLocator newServiceLocator) {
        this.serviceLocator = newServiceLocator;
    }

    @Override protected IContributionItem[] getContributionItems() {
        final ISpoofaxEclipseEditor editor = latestEditorListener.previousEditor();
        if(editor == null) {
            logger.debug("Cannot create menu items; there is no latest active editor");
            return new IContributionItem[0];
        }

        final FileObject resource = resourceService.resolve(editor.input());
        if(resource == null) {
            logger.error("Cannot create menu items; cannot resolve input resource for {}", editor);
            return new IContributionItem[0];
        }

        final ILanguage language = languageIdentifier.identify(resource);
        if(language == null) {
            logger.error("Cannot create menu items; cannot identify language for {}", resource);
            return new IContributionItem[0];
        }

        final MenusFacet facet = language.facet(MenusFacet.class);
        if(facet == null) {
            logger.error("Cannot create menu items; cannot find menus facet in {}", language);
            return new IContributionItem[0];
        }

        final Collection<IContributionItem> items = Lists.newLinkedList();
        for(Menu menu : facet.menus()) {
            items.add(createItem(menu));
        }
        return items.toArray(new IContributionItem[0]);
    }

    private IContributionItem createItem(Menu menu) {
        final MenuManager menuManager = new MenuManager(menu.name());
        for(Menu submenu : menu.submenus()) {
            final IContributionItem submenuItem = createItem(submenu);
            menuManager.add(submenuItem);
        }
        for(Action action : menu.actions()) {
            final IContributionItem actionItem = createItem(action);
            menuManager.add(actionItem);
        }
        return menuManager;
    }

    private IContributionItem createItem(Action action) {
        final CommandContributionItemParameter itemParams =
            new CommandContributionItemParameter(serviceLocator, null, transformId, CommandContributionItem.STYLE_PUSH);
        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put(actionNameParam, action.name);
        itemParams.parameters = parameters;
        itemParams.label = action.name;

        return new CommandContributionItem(itemParams);
    }
}
