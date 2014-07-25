package org.strategoxt.imp.metatooling.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class UpgradeSpoofaxProjectWizard extends Wizard {
    private final IProject project;
    private final UpgradeSpoofaxProjectWizardPage page = new UpgradeSpoofaxProjectWizardPage();

    public UpgradeSpoofaxProjectWizard(IProject project) {
        this.project = project;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    doFinish(monitor);
                } catch (Exception e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, false, runnable);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            final Throwable t = e.getTargetException();
            MessageDialog.openError(getShell(), "Error: " + t.getClass().getName(), t.getMessage());
            return false;
        }
        return true;
    }

    private void doFinish(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Upgrading Spoofax Project", 5);

        UpgradeMainXML(monitor);
        monitor.worked(1);
        UpgradeClasspath(monitor);
        monitor.worked(1);
        UpgradeManifest(monitor);
        monitor.worked(1);
        UpgradeBuildProperties(monitor);
        monitor.worked(1);
        AddSpoofaxNature(monitor);
        monitor.worked(1);
    }

    private void UpgradeMainXML(IProgressMonitor monitor) throws Exception {
        final IFile file = (IFile) project.findMember("build.main.xml");
        if (file == null)
            return;
        final Document document = OpenXML(file);
        final XPath xpath = XPathFactory.newInstance().newXPath();
        boolean changed = false;

        // Change build property
        final Node buildPropertyAttr = (Node) xpath.evaluate("//project/property[@name='build']/@location", document,
                XPathConstants.NODE);
        if (!buildPropertyAttr.getTextContent().equals("target/classes")) {
            buildPropertyAttr.setTextContent("target/classes");
            changed = true;
        }

        // Add targets for Maven build
        final Node projectNode = (Node) xpath.evaluate("//project", document, XPathConstants.NODE);
        final String antTarget = (String) xpath.evaluate(
                "string(//project/target[@name='all' and contains(@depends, 'spoofaximp.default')]/@depends)",
                document, XPathConstants.STRING);

        final Node generateSourcesNode = (Node) xpath.evaluate("//project/target[@name='generate-sources']", document,
                XPathConstants.NODE);
        final Node packageNode = (Node) xpath.evaluate("//project/target[@name='package']", document,
                XPathConstants.NODE);

        if (antTarget.equals("spoofaximp.default") || antTarget.equals("spoofaximp.default.ctree")) {
            if (generateSourcesNode == null) {
                final Element generateSourcesElem = document.createElement("target");
                generateSourcesElem.setAttribute("name", "generate-sources");
                generateSourcesElem.setAttribute("depends", "generate-sources-ctree");
                projectNode.appendChild(generateSourcesElem);
                changed = true;
            }

            if (packageNode == null) {
                final Element packageElem = document.createElement("target");
                packageElem.setAttribute("name", "package");
                packageElem.setAttribute("depends", "package-ctree");
                projectNode.appendChild(packageElem);
                changed = true;
            }
        } else if (antTarget.equals("spoofaximp.default.jar")) {
            if (generateSourcesNode == null) {
                final Element generateSourcesElem = document.createElement("target");
                generateSourcesElem.setAttribute("name", "generate-sources");
                generateSourcesElem.setAttribute("depends", "generate-sources-java");
                projectNode.appendChild(generateSourcesElem);
                changed = true;
            }

            if (packageNode == null) {
                final Element packageElem = document.createElement("target");
                packageElem.setAttribute("name", "package");
                packageElem.setAttribute("depends", "package-java");
                projectNode.appendChild(packageElem);
                changed = true;
            }
        }

        // Write results back
        if (changed) {
            WriteXML(file, document, monitor);
        }
    }

    private void UpgradeClasspath(IProgressMonitor monitor) throws Exception {
        final IFile file = (IFile) project.findMember(".claspath");
        if (file == null)
            return;
        final Document document = OpenXML(file);
        final XPath xpath = XPathFactory.newInstance().newXPath();
        boolean changed = false;

        // Remove trans exclude
        final Node sourceExcludeAttr = (Node) xpath.evaluate("//classpath/classpathentry[@kind='src']/@excluding",
                document, XPathConstants.NODE);
        if (sourceExcludeAttr != null) {
            sourceExcludeAttr.getParentNode().removeChild(sourceExcludeAttr);
            changed = true;
        }

        // Set output directory to target/classes
        final Node outputPathAttr = (Node) xpath.evaluate("//classpath/classpathentry[@kind='output']/@path", document,
                XPathConstants.NODE);
        if (outputPathAttr != null) {
            outputPathAttr.setTextContent("target/classes");
            changed = true;
        }

        if (changed) {
            WriteXML(file, document, monitor);
        }
    }

    private void UpgradeManifest(IProgressMonitor monitor) throws Exception {
        final IFile file = (IFile) project.findMember("META-INF/MANIFEST.MF");
        if (file == null)
            return;

        final Manifest manifest = new Manifest(file.getContents());
        final Attributes attributes = manifest.getMainAttributes();
        final Set<String> dependencies = Sets.newHashSet(attributes.getValue("Require-Bundle").split(","));
        final Set<String> requiredDependencies = Sets.newHashSet("org.spoofax.interpreter.externaldeps",
                "org.spoofax.interpreter.core", "org.spoofax.terms", "org.strategoxt.strj", "org.spoofax.jsglr",
                "org.strategoxt.imp.runtime");
        final Set<String> newDependencies = Sets.union(dependencies, requiredDependencies);
        final String dependenciesString = Joiner.on(',').join(newDependencies);
        attributes.putValue("Require-Bundle", dependenciesString);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        manifest.write(out);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        file.setContents(in, 0, monitor);
    }

    private void UpgradeBuildProperties(IProgressMonitor monitor) throws Exception {
        final IFile file = (IFile) project.findMember("build.properties");
        if (file == null)
            return;

        final Properties prop = new Properties();
        prop.load(file.getContents());

        prop.setProperty("source..", "editor/java/");
        prop.setProperty("output..", "target/classes/");
        prop.setProperty("bin.includes", "META-INF/,plugin.xml,include/,icons/,.");
        prop.setProperty("bin.excludes", "trans/");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        prop.store(out, null);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        file.setContents(in, 0, monitor);
    }

    private void AddSpoofaxNature(IProgressMonitor monitor) throws CoreException {
        final IProjectDescription description = project.getDescription();
        final String[] natures = description.getNatureIds();
        boolean addNature = true;
        for (String nature : natures) {
            if (nature.equals("org.strategoxt.imp.metatooling.nature")) {
                addNature = false;
            }
        }

        if (addNature) {
            final String[] newNatures = new String[natures.length + 1];
            System.arraycopy(natures, 0, newNatures, 0, natures.length);
            newNatures[natures.length] = "org.strategoxt.imp.metatooling.nature";
            description.setNatureIds(newNatures);
            // project.setDescription(description, monitor);
        }
    }

    private Document OpenXML(IFile file) throws SAXException, IOException, CoreException, ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.getContents());
    }

    private void WriteXML(IFile file, Document document, IProgressMonitor monitor) throws TransformerException,
            CoreException, IOException {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        final DOMSource source = new DOMSource(document);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
        final ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        try {
            file.setContents(in, 0, monitor);
        } finally {
            in.close();
            out.close();
        }
    }
}
