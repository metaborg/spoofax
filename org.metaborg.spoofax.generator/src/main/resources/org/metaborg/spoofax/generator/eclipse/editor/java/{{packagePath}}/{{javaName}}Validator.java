package {{packageName}}.eclipse;

import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.MetaFileLanguageValidator;

public class {{javaName}}Validator extends MetaFileLanguageValidator {
    @Override
    public Descriptor getDescriptor() {
        // Lazily get the descriptor
        return {{javaName}}ParseController.getDescriptor();
    }
}
