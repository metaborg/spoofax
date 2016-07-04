package org.metaborg.spoofax.meta.core.generator;

import java.io.Writer;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.DefaultMustacheVisitor;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheResolver;
import com.github.mustachejava.MustacheVisitor;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.codes.ValueCode;

public class StrictMustacheFactory extends DefaultMustacheFactory {
    private static class StrictMustacheVisitor extends DefaultMustacheVisitor {
        public StrictMustacheVisitor(DefaultMustacheFactory df) {
            super(df);
        }

        @Override public void value(TemplateContext tc, String variable, boolean encoded) {
            list.add(new StrictValueCode(tc, df, variable, encoded));
        }
    }

    private static class StrictValueCode extends ValueCode {
        public StrictValueCode(TemplateContext tc, DefaultMustacheFactory df, String variable, boolean encoded) {
            super(tc, df, variable, encoded);
        }

        @Override public Writer execute(Writer writer, List<Object> scopes) {
            try {
                // this depends on ValueCode implementation, which uses get(Object) to get the (possibly null) value.
                final Object object = get(scopes);
                if(object != null) {
                    return super.execute(writer, scopes);
                } else {
                    throw new Exception("Null");
                }
            } catch(MustacheException e) {
                throw e;
            } catch(Exception e) {
                throw new MustacheException("Failed to get value for " + name, e, tc);
            }
        }
    }


    public StrictMustacheFactory(MustacheResolver mustacheResolver) {
        super(mustacheResolver);
    }


    @Override public MustacheVisitor createMustacheVisitor() {
        return new StrictMustacheVisitor(this);
    }
}
