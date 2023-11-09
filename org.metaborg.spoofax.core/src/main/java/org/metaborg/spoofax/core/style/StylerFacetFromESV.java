package org.metaborg.spoofax.core.style;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;

import org.spoofax.terms.util.TermUtils;

public class StylerFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(StylerFacetFromESV.class);


    public static @Nullable StylerFacet create(IStrategoAppl esv) {
        final StylerFacet facet = new StylerFacet();

        final Iterable<IStrategoAppl> styleDefs = ESVReader.collectTerms(esv, "ColorDef");
        final Map<String, IStyle> namedStyles = new HashMap<>();
        for(IStrategoAppl styleDef : styleDefs) {
            final IStrategoAppl styleTerm = TermUtils.toApplAt(styleDef, 1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final IStyle style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = TermUtils.toJavaStringAt(styleTerm, 0);
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style definition " + styleDef);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style definition " + styleDef);
                continue;
            }

            namedStyles.put(TermUtils.toJavaStringAt(styleDef, 0), style);
        }

        final Collection<IStrategoAppl> styleRules = ESVReader.collectTerms(esv, "ColorRule");
        if(styleRules.isEmpty()) {
            return null;
        }
        for(IStrategoAppl styleRule : styleRules) {
            final IStrategoAppl styleTerm = TermUtils.toApplAt(styleRule, 1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final IStyle style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = TermUtils.toJavaStringAt(styleTerm, 0);
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style rule " + styleRule);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style rule " + styleRule);
                continue;
            }

            final IStrategoAppl node = TermUtils.toApplAt(styleRule, 0);
            final IStrategoConstructor nodeCons = node.getConstructor();
            if(nodeCons.getName().equals("SortAndConstructor")) {
                final String sort = TermUtils.toJavaStringAt(node.getSubterm(0), 0);
                final String cons = TermUtils.toJavaStringAt(node.getSubterm(1), 0);
                facet.mapSortConsToStyle(sort, cons, style);
            } else if(nodeCons.getName().equals("ConstructorOnly")) {
                final String cons = TermUtils.toJavaStringAt(node.getSubterm(0), 0);
                facet.mapConsToStyle(cons, style);
            } else if(nodeCons.getName().equals("Sort")) {
                final String sort = TermUtils.toJavaStringAt(node, 0);
                facet.mapSortToStyle(sort, style);
            } else if(nodeCons.getName().equals("Token")) {
                final IStrategoAppl tokenAppl = TermUtils.toApplAt(node, 0);
                final String token = tokenAppl.getConstructor().getName();
                facet.mapTokenToStyle(token, style);
            } else {
                logger.error("Unhandled node " + nodeCons + " in style rule " + styleRule);
                continue;
            }
        }

        return facet;
    }

    private static IStyle style(IStrategoAppl attribute) {
        final Color color = color(TermUtils.toApplAt(attribute, 0));
        final Color backgroundColor = color(TermUtils.toApplAt(attribute, 1));
        final boolean bold;
        final boolean italic;
        final boolean underline = false;
        final boolean strikeout = false;
        final IStrategoAppl fontSetting = TermUtils.toApplAt(attribute, 2);
        final String fontSettingCons = fontSetting.getConstructor().getName();
        switch (fontSettingCons) {
            case "BOLD":
                bold = true;
                italic = false;
                break;
            case "ITALIC":
                bold = false;
                italic = true;
                break;
            case "BOLD_ITALIC":
                bold = true;
                italic = true;
                break;
            default:
                bold = false;
                italic = false;
                break;
        }
        return new Style(color, backgroundColor, bold, italic, underline, strikeout);
    }

    private static Color color(IStrategoAppl color) {
        final String colorCons = color.getConstructor().getName();
        switch (colorCons) {
            case "ColorRGB":
                final int r = Integer.parseInt(TermUtils.toJavaStringAt(color, 0));
                final int g = Integer.parseInt(TermUtils.toJavaStringAt(color, 1));
                final int b = Integer.parseInt(TermUtils.toJavaStringAt(color, 2));
                return new Color(r, g, b);
            case "ColorDefault":
                return new Color(0, 0, 0);
            default:
                return null;
        }
    }
}
