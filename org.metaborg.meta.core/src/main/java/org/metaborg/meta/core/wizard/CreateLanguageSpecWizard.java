package org.metaborg.meta.core.wizard;

import java.util.Collection;
import java.util.List;

import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.base.Splitter;

/**
 * Helps with the validation and UI of a 'create language specification' wizard.
 */
public abstract class CreateLanguageSpecWizard extends UpgradeLanguageSpecWizard {
    public String projectName() {
        return inputProjectName();
    }

    public Collection<String> extensions() {
        return Splitter.on(',').trimResults().omitEmptyStrings().splitToList(inputExtensions());
    }


    protected abstract boolean inputProjectNameModified();

    protected abstract String inputProjectName();

    protected abstract boolean inputExtensionsModified();

    protected abstract String inputExtensions();


    protected abstract void setName(String name);

    protected abstract void setId(String id);

    protected abstract void setExtensions(String extensions);


    public void distributeProjectName() {
        if(!inputNameModified() || inputName().isEmpty()) {
            final String name = toName(inputProjectName());
            setName(name);
            distributeLanguageName();
        }
        if(!inputIdModified() || inputId().isEmpty()) {
            final String id = toId(inputProjectName());
            setId(id);
        }
    }

    public void distributeLanguageName() {
        if(!inputExtensionsModified() || inputExtensions().isEmpty()) {
            final String extensions = toExtension(inputName());
            setExtensions(extensions);
        }
    }

    public ValidationResult validate() {
        final ValidationResult superResult = super.validate();

        boolean complete = superResult.complete;
        final List<String> errors = superResult.errors;

        final String projectName = inputProjectName();
        if(inputProjectNameModified()) {
            if(projectName.isEmpty()) {
                errors.add("Project name must be specified");
            } else if(!LanguageIdentifier.validId(projectName)) {
                errors.add("Project name is invalid; " + LanguageIdentifier.errorDescription);
            }
        } else if(projectName.isEmpty()) {
            complete = false;
        }

        final String extensions = inputExtensions();
        if(extensions.isEmpty() || extensions().isEmpty()) {
            complete = false;
            if(inputExtensionsModified()) {
                errors.add("At least one extension must be specified");
            }
        }

        complete = complete && errors.isEmpty();

        return new ValidationResult(complete, errors);
    }


    /**
     * Converts a project name to a language name.
     * 
     * @param name
     *            Project name.
     * @return Language name.
     */
    public static String toName(String name) {
        final char[] input = name.replace(' ', '-').toCharArray();
        final StringBuilder output = new StringBuilder();

        int i = 0;
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetter(c) || c == '-' || c == '_') {
                output.append(c);
                break;
            }
        }
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                output.append(c);
            }
        }

        return output.toString();
    }

    /**
     * Converts a project name to a language identifier.
     * 
     * @param name
     *            Project name.
     * @return Language identifier.
     */
    public static String toId(String name) {
        final char[] input = name.replace(' ', '-').toCharArray();
        final StringBuilder output = new StringBuilder();

        int i = 0;
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetter(c) || c == '.' || c == '_') {
                output.append(c);
                break;
            }
        }
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetterOrDigit(c) || c == '.' || c == '_')
                output.append(c);
        }

        final String result = output.toString().replaceAll("\\.(?=\\.|[0-9]|\\Z)", "");
        return result;
    }

    /**
     * Converts a language name to an extension.
     * 
     * @param name
     *            Language name.
     * @return Extension.
     */
    public static String toExtension(String name) {
        final String input = name.toLowerCase().replace("-", "").replace(".", "").replace(" ", "").replace(":", "");
        final String prefix = input.substring(0, Math.min(input.length(), 3));
        if(input.length() == 0) {
            return "";
        }

        for(int i = input.length() - 1;; i--) {
            if(!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
                return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
            } else if(i == prefix.length()) {
                return prefix + input.substring(i);
            }
        }
    }
}
