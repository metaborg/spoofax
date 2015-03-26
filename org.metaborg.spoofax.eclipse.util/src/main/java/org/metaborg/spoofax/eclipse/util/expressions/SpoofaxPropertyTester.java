package org.metaborg.spoofax.eclipse.util.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

public class SpoofaxPropertyTester extends PropertyTester {
    @Override public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        switch(property) {
            case "hasNature":
                return hasNature(receiver, expectedValue.toString());
            case "isOpen":
                return isOpen(receiver);
        }

        return false;
    }


    private boolean hasNature(Object receiver, String expectedNature) {
        final IProject project = AbstractHandlerUtils.getProjectFromElement(receiver);

        if(project == null) {
            return false;
        }

        try {
            return project.hasNature(expectedNature);
        } catch(CoreException e) {
            return false;
        }
    }

    private boolean isOpen(Object receiver) {
        final IProject project = AbstractHandlerUtils.getProjectFromElement(receiver);

        if(project == null) {
            return false;
        }

        return project.isOpen();
    }
}
