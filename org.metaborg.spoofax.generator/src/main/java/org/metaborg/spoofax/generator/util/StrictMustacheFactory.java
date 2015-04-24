package org.metaborg.spoofax.generator.util;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.codes.ValueCode;

public class StrictMustacheFactory extends DefaultMustacheFactory {

    public StrictMustacheFactory(MustacheResolver mustacheResolver) {
        super(mustacheResolver);
    }

    @Override
    public MustacheVisitor createMustacheVisitor() {
        return new StrictMustacheVisitor(this);
    }
    
    private static class StrictMustacheVisitor extends DefaultMustacheVisitor {

        public StrictMustacheVisitor(DefaultMustacheFactory df) {
            super(df);
        }

        @Override
        public void value(TemplateContext tc, String variable, boolean encoded) {
            list.add(new ValueCode(tc, df, variable, encoded));
        }

        
    }

}
