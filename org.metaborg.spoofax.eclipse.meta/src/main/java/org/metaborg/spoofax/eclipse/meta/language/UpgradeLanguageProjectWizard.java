package org.metaborg.spoofax.eclipse.meta.language;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.eclipse.meta.nature.SpoofaxMetaNature;
import org.metaborg.spoofax.eclipse.nature.SpoofaxNature;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.BuilderUtils;
import org.metaborg.spoofax.eclipse.util.NatureUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.metaborg.util.resource.ContainsFileSelector;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class UpgradeLanguageProjectWizard extends Wizard {
    private final IProject eclipseProject;
    private final FileObject project;
    private final UpgradeLanguageProjectWizardPage page;


    public UpgradeLanguageProjectWizard(IEclipseResourceService resourceService,
        ITermFactoryService termFactoryService, IProject eclipseProject) {
        this.eclipseProject = eclipseProject;
        this.project = resourceService.resolve(eclipseProject);

        String languageName;
        String packageName;
        try {
            final FileObject[] files = project.findFiles(new ContainsFileSelector("packed.esv"));
            if(files.length == 0) {
                languageName = "";
                packageName = "";
            } else {
                final FileObject esvFile = files[0];
                final TermReader reader =
                    new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
                final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
                if(term.getTermType() != IStrategoTerm.APPL) {
                    throw new IllegalStateException("Packed ESV file does not contain a valid ESV term.");
                }
                final IStrategoAppl esvTerm = (IStrategoAppl) term;

                languageName = ESVReader.getProperty(esvTerm, "LanguageName");
                packageName = ESVReader.getProperty(esvTerm, "LanguageId");
            }
        } catch(ParseError | IOException e) {
            languageName = "";
            packageName = "";
        }

        this.page = new UpgradeLanguageProjectWizardPage(languageName, packageName);

        setNeedsProgressMonitor(true);
    }


    @Override public void addPages() {
        addPage(page);
    }

    @Override public boolean performFinish() {
        final String languageName = page.inputLanguageName.getText();
        final String packageName = page.inputPackageName.getText();

        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    doUpgrade(monitor, languageName, packageName);
                } catch(Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, false, runnable);
        } catch(InterruptedException e) {
            return false;
        } catch(InvocationTargetException e) {
            final Throwable t = e.getTargetException();
            MessageDialog.openError(getShell(), "Error: " + t.getClass().getName(), t.getMessage());
            return false;
        }
        return true;
    }

    private void doUpgrade(IProgressMonitor monitor, final String languageName, final String packageName)
        throws Exception {
        final IWorkspaceRunnable upgradeRunnable = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                try {
                    workspaceMonitor.beginTask("Upgrading language project", 5);
                    deleteImpPluginClasses(languageName, packageName);
                    workspaceMonitor.worked(1);
                    deleteAntToolBuilders(languageName);
                    workspaceMonitor.worked(1);
                    upgradeManifest(packageName);
                    workspaceMonitor.worked(1);
                    upgradeProject();
                    workspaceMonitor.worked(1);
                    upgradePlugin(languageName);
                    workspaceMonitor.worked(1);
                } catch(CoreException e) {
                    throw e;
                } catch(Exception e) {
                    throw new CoreException(StatusUtils.error(e));
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(upgradeRunnable, ResourcesPlugin.getWorkspace().getRoot(),
            IWorkspace.AVOID_UPDATE, monitor);
    }

    private void deleteImpPluginClasses(String languageName, String packageName) throws Exception {
        final String impClassesLoc = packageName.replace(".", File.separator);
        final FileObject impClassesDir = project.resolveFile("editor/java/" + impClassesLoc);
        if(!impClassesDir.exists()) {
            return;
        }
        impClassesDir.resolveFile("Activator.java").delete();
        final String className = languageName.replaceAll("[^a-zA-Z0-9\\_\\$]", "");
        impClassesDir.resolveFile(className + "ParseController.java").delete();
        impClassesDir.resolveFile(className + "ParseControllerGenerated.java").delete();
        impClassesDir.resolveFile(className + "Validator.java").delete();
        impClassesDir.refresh();
        if(impClassesDir.getChildren().length == 0) {
            impClassesDir.delete();
        }
    }

    private void deleteAntToolBuilders(String languageName) throws Exception {
        final FileObject antBuilderDir = project.resolveFile(".externalToolBuilders");
        if(!antBuilderDir.exists()) {
            return;
        }
        antBuilderDir.resolveFile(languageName + " build.main.xml.launch").delete();
        antBuilderDir.resolveFile(languageName + " clean-project.xml.launch").delete();
        antBuilderDir.refresh();
        if(antBuilderDir.getChildren().length == 0) {
            antBuilderDir.delete();
        }
    }

    private void upgradeManifest(String packageName) throws Exception {
        final FileObject manifestFile = project.resolveFile("META-INF/MANIFEST.MF");
        if(!manifestFile.exists()) {
            return;
        }
        final Manifest manifest = new Manifest(manifestFile.getContent().getInputStream());
        final Attributes attributes = manifest.getMainAttributes();

        boolean changed = false;

        final Set<String> removedDependencies =
            Sets.newHashSet("org.eclipse.core.runtime", "org.eclipse.core.resources", "org.eclipse.imp.runtime",
                "org.eclipse.ui", "lpg.runtime", "org.eclipse.jface.text", "org.eclipse.ui.editors",
                "org.eclipse.ui.workbench.texteditor", "org.strategoxt.imp.runtime", "org.spoofax.jsglr",
                "org.strategoxt.imp.nativebundle");
        final Set<String> requiredDependencies =
            Sets.newHashSet("org.metaborg.spoofax.eclipse", "org.spoofax.terms", "org.spoofax.interpreter.core",
                "org.spoofax.interpreter.externaldeps", "org.strategoxt.strj");
        changed = upgradeListAttribute(attributes, "Require-Bundle", removedDependencies, requiredDependencies) | changed;
        changed =
            upgradeListAttribute(attributes, "Import-Package", Sets.newHashSet("org.osgi.framework;version=\"1.3.0\""),
                Sets.<String>newHashSet()) | changed;
        changed =
            upgradeListAttribute(attributes, "Export-Package", Sets.newHashSet(packageName), Sets.<String>newHashSet()) | changed;
        changed = (attributes.remove(new Attributes.Name("Bundle-Activator")) == null ? false : true) | changed;

        if(changed) {
            try(final OutputStream out = manifestFile.getContent().getOutputStream()) {
                manifest.write(out);
            }
        }
    }

    private boolean upgradeListAttribute(Attributes attributes, String name, Set<String> removed, Set<String> required) {
        final String attribute = attributes.getValue(name);
        final Set<String> current;
        if(attribute != null) {
            current = Sets.newHashSet(attribute.split(","));
        } else {
            current = Sets.newHashSet();
        }

        boolean changed = current.removeAll(removed);
        changed = current.addAll(required) | changed;

        if(!changed) {
            return false;
        } else if(current.size() == 0) {
            attributes.remove(new Attributes.Name(name));
        } else {
            final String newList = Joiner.on(',').join(current);
            attributes.putValue(name, newList);
        }
        return true;
    }

    private void upgradeProject() throws Exception {
        BuilderUtils.removeFrom("org.eclipse.ui.externaltools.ExternalToolBuilder", eclipseProject);
        NatureUtils.addTo(SpoofaxNature.id, eclipseProject);
        NatureUtils.addTo(SpoofaxMetaNature.id, eclipseProject);
    }

    private void upgradePlugin(String languageName) throws Exception {
        final FileObject pluginFile = project.resolveFile("plugin.xml");
        if(!pluginFile.exists()) {
            return;
        }

        final Document document = openXML(pluginFile);

        final XPath xpath = XPathFactory.newInstance().newXPath();
        boolean changed = false;

        Node pluginNode = (Node) xpath.evaluate("//plugin", document, XPathConstants.NODE);
        if(pluginNode == null) {
            final Element pluginElem = document.createElement("plugin");
            document.appendChild(pluginElem);
            pluginNode = pluginElem;
            changed = true;
        }

        final Node impLanguageExtensionNode =
            (Node) xpath.evaluate("//plugin/extension[@point='org.eclipse.imp.runtime.languageDescription']",
                document, XPathConstants.NODE);
        if(impLanguageExtensionNode != null) {
            pluginNode.removeChild(impLanguageExtensionNode);
            changed = true;
        }

        final Node impParserExtensionNode =
            (Node) xpath.evaluate("//plugin/extension[@point='org.eclipse.imp.runtime.parser']", document,
                XPathConstants.NODE);
        if(impParserExtensionNode != null) {
            pluginNode.removeChild(impParserExtensionNode);
            changed = true;
        }

        final Node newExtensionNode =
            (Node) xpath.evaluate("//plugin/extension[@point='org.metaborg.spoofax.eclipse.language']", document,
                XPathConstants.NODE);
        if(newExtensionNode == null) {
            final Element extensionElem = document.createElement("extension");
            extensionElem.setAttribute("point", "org.metaborg.spoofax.eclipse.language");
            pluginNode.appendChild(extensionElem);
            final Element languageElem = document.createElement("language");
            languageElem.setAttribute("esvFile", "include/" + languageName + ".packed.esv");
            extensionElem.appendChild(languageElem);
            changed = true;
        }
        
        if(changed) {
            writeXML(pluginFile, document);
        }
    }

    private Document openXML(FileObject resource) throws SAXException, IOException, CoreException,
        ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resource.getContent().getInputStream());
    }

    private void writeXML(FileObject resource, Document document) throws TransformerException, IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        final DOMSource source = new DOMSource(document);
        try(final OutputStream out = resource.getContent().getOutputStream()) {
            final StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
    }
}
