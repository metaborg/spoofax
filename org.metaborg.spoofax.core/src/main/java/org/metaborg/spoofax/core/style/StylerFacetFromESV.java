package org.metaborg.spoofax.core.style;

import java.awt.Color;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.core.style.IStyle;
import org.metaborg.core.style.Style;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class StylerFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(StylerFacetFromESV.class);


    public static @Nullable StylerFacet create(IStrategoAppl esv) {
        final StylerFacet facet = new StylerFacet();

        final Iterable<IStrategoAppl> styleDefs = ESVReader.collectTerms(esv, "ColorDef");
        final Map<String, IStyle> namedStyles = Maps.newHashMap();
        for(IStrategoAppl styleDef : styleDefs) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleDef.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final IStyle style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = Tools.asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style definition " + styleDef);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style definition " + styleDef);
                continue;
            }

            namedStyles.put(Tools.asJavaString(styleDef.getSubterm(0)), style);
        }

        final Iterable<IStrategoAppl> styleRules = ESVReader.collectTerms(esv, "ColorRule");
        if(Iterables.isEmpty(styleRules)) {
            return null;
        }
        for(IStrategoAppl styleRule : styleRules) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleRule.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final IStyle style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = Tools.asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style rule " + styleRule);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style rule " + styleRule);
                continue;
            }

            final IStrategoAppl node = (IStrategoAppl) styleRule.getSubterm(0);
            final IStrategoConstructor nodeCons = node.getConstructor();
            if(nodeCons.getName().equals("SortAndConstructor")) {
                final String sort = Tools.asJavaString(node.getSubterm(0).getSubterm(0));
                final String cons = Tools.asJavaString(node.getSubterm(1).getSubterm(0));
                facet.mapSortConsToStyle(sort, cons, style);
            } else if(nodeCons.getName().equals("ConstructorOnly")) {
                final String cons = Tools.asJavaString(node.getSubterm(0).getSubterm(0));
                facet.mapConsToStyle(cons, style);
            } else if(nodeCons.getName().equals("Sort")) {
                final String sort = Tools.asJavaString(node.getSubterm(0));
                facet.mapSortToStyle(sort, style);
            } else if(nodeCons.getName().equals("Token")) {
                final IStrategoAppl tokenAppl = (IStrategoAppl) node.getSubterm(0);
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
        final Color color = color((IStrategoAppl) attribute.getSubterm(0));
        final Color backgroundColor = color((IStrategoAppl) attribute.getSubterm(1));
        final boolean bold;
        final boolean italic;
        final boolean underline = false;
        final IStrategoAppl fontSetting = (IStrategoAppl) attribute.getSubterm(2);
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
        return new Style(color, backgroundColor, bold, italic, underline);
    }

    private static Color color(IStrategoAppl color) {
        final String colorCons = color.getConstructor().getName();
        switch (colorCons) {
            case "ColorRGB":
                final int r = Integer.parseInt(Tools.asJavaString(color.getSubterm(0)));
                final int g = Integer.parseInt(Tools.asJavaString(color.getSubterm(1)));
                final int b = Integer.parseInt(Tools.asJavaString(color.getSubterm(2)));
                return new Color(r, g, b);
            case "ColorDefault":
                return new Color(0, 0, 0);
            default:
                return null;
        }
    }
}
