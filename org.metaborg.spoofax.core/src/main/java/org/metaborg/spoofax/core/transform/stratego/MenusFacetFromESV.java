package org.metaborg.spoofax.core.transform.stratego;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.language.ILanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class MenusFacetFromESV {
    private static final Logger logger = LoggerFactory.getLogger(MenusFacetFromESV.class);


    public static MenusFacet create(IStrategoAppl esv, ILanguage inputLanguage) {
        final Iterable<IStrategoAppl> menuTerms = ESVReader.collectTerms(esv, "ToolbarMenu");
        final MenusFacet facet = new MenusFacet();
        for(IStrategoAppl menuTerm : menuTerms) {
            final Menu menu = menu(menuTerm, new ActionFlags(), inputLanguage);
            facet.add(menu);
        }
        return facet;
    }

    private static Menu menu(IStrategoTerm menuTerm, ActionFlags flags, ILanguage inputLanguage) {
        final String name = name(menuTerm.getSubterm(0));
        final ActionFlags extraFlags = flags(menuTerm.getSubterm(1));
        final ActionFlags mergedFlags = ActionFlags.merge(flags, extraFlags);
        final Iterable<IStrategoTerm> items = menuTerm.getSubterm(2);
        final Menu menu = new Menu(name);
        for(IStrategoTerm item : items) {
            final String constructor = Tools.constructorName(item);
            if(constructor == null) {
                logger.error("Could not interpret menu item from term {}", item);
                continue;
            }
            switch(constructor) {
                case "Submenu":
                    final Menu submenu = menu(item, mergedFlags, inputLanguage);
                    menu.add(submenu);
                    break;
                case "Action":
                    final Action action = action(item, mergedFlags, inputLanguage);
                    menu.add(action);
                    break;
                default:
                    logger.warn("Unhandled menu item term {}", item);
                    break;
            }
        }
        return menu;
    }

    private static String name(IStrategoTerm nameTerm) {
        // For some reason, names in menus have different shapes of names that need to be handled:
        // * ToolbarMenu: Label(String("\"Name\""))
        // * Submenu: String("\"Name\"")
        // * Action: String("\"Name\"")
        final String nameQuoted;
        if(Tools.hasConstructor((IStrategoAppl) nameTerm, "Label")) {
            nameQuoted = Tools.asJavaString(nameTerm.getSubterm(0).getSubterm(0));
        } else {
            nameQuoted = Tools.asJavaString(nameTerm.getSubterm(0));
        }
        return nameQuoted.replace("\"", "");
    }

    private static Action action(IStrategoTerm action, ActionFlags flags, ILanguage inputLanguage) {
        final String name = name(action.getSubterm(0));
        final String stategy = Tools.asJavaString(action.getSubterm(1).getSubterm(0));
        final ActionFlags extraFlags = flags(action.getSubterm(2));
        final ActionFlags mergedFlags = ActionFlags.merge(flags, extraFlags);
        return new Action(name, inputLanguage, null, stategy, mergedFlags);
    }

    private static ActionFlags flags(Iterable<IStrategoTerm> flagTerms) {
        final ActionFlags flags = new ActionFlags();
        for(IStrategoTerm flagTerm : flagTerms) {
            final String constructor = Tools.constructorName(flagTerm);
            if(constructor == null) {
                logger.error("Could not interpret flag from term {}", flagTerm);
                continue;
            }
            switch(constructor) {
                case "Source":
                    flags.parsed = true;
                    break;
                case "Meta":
                    flags.meta = true;
                    break;
                case "OpenEditor":
                    flags.openEditor = true;
                    break;
                case "RealTime":
                    flags.realtime = true;
                    break;
                default:
                    logger.warn("Unhandled flag term {}", flagTerm);
                    break;
            }
        }
        return flags;
    }
}
