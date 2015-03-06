package org.metaborg.spoofax.core.style;

import java.awt.Color;
import java.util.Map;

import org.metaborg.spoofax.core.esv.ESVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;

import com.google.common.collect.Maps;

public class StylerFacetFromESV {
    private static final Logger logger = LoggerFactory.getLogger(StylerFacetFromESV.class);


    public static StylerFacet create(IStrategoAppl esv) {
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
        if(fontSettingCons.equals("BOLD")) {
            bold = true;
            italic = false;
        } else if(fontSettingCons.equals("ITALIC")) {
            bold = false;
            italic = true;
        } else if(fontSettingCons.equals("BOLD_ITALIC")) {
            bold = true;
            italic = true;
        } else {
            bold = false;
            italic = false;
        }
        return new Style(color, backgroundColor, bold, italic, underline);
    }

    private static Color color(IStrategoAppl color) {
        final String colorCons = color.getConstructor().getName();
        if(colorCons.equals("ColorRGB")) {
            final int r = Integer.parseInt(Tools.asJavaString(color.getSubterm(0)));
            final int g = Integer.parseInt(Tools.asJavaString(color.getSubterm(1)));
            final int b = Integer.parseInt(Tools.asJavaString(color.getSubterm(2)));
            return new Color(r, g, b);
        } else if(colorCons.equals("ColorDefault")) {
            return new Color(0, 0, 0);
        } else {
            return null;
        }
    }
}
