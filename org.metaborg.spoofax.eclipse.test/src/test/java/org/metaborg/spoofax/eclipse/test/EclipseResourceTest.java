package org.metaborg.spoofax.eclipse.test;

import static org.junit.Assert.*;
import static org.metaborg.util.test.Assert2.*;

import java.util.zip.ZipInputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.junit.Test;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EclipseResourceTest extends SpoofaxEclipseTest {
    @Test public void createFolder() throws Exception {
        final FileObject folder = resourceService.resolve("Folder/");
        assertEquals(FileType.IMAGINARY, folder.getType());
        folder.createFolder();
        assertEquals(FileType.FOLDER, folder.getType());
        folder.delete(Selectors.SELECT_ALL);
        assertEquals(FileType.IMAGINARY, folder.getType());
    }

    @Test public void createNestedFolder() throws Exception {
        final FileObject folder = resourceService.resolve("Folder/nested");
        assertEquals(FileType.IMAGINARY, folder.getType());
        folder.createFolder();
        assertEquals(FileType.FOLDER, folder.getType());
        folder.delete();
        assertEquals(FileType.IMAGINARY, folder.getType());
        final FileObject parent = folder.getParent();
        assertEquals(FileType.FOLDER, parent.getType());
        parent.delete(Selectors.SELECT_ALL);
        assertEquals(FileType.IMAGINARY, parent.getType());
    }

    @Test public void createFile() throws Exception {
        final FileObject file = resourceService.resolve("Folder/file");
        assertEquals(FileType.IMAGINARY, file.getType());
        file.createFile();
        assertEquals(FileType.FILE, file.getType());
        file.delete();
        assertEquals(FileType.IMAGINARY, file.getType());
        final FileObject parent = file.getParent();
        assertEquals(FileType.FOLDER, parent.getType());
        parent.delete(Selectors.SELECT_ALL);
        assertEquals(FileType.IMAGINARY, parent.getType());
    }

    @Test(expected = FileSystemException.class) public void cannotCreateFileAtRoot() throws Exception {
        final FileObject file = resourceService.resolve("file");
        file.createFile();
    }

    @Test public void getChildren() throws Exception {
        final FileObject folder = resourceService.resolve("Folder/");
        folder.createFolder();
        final FileObject childFile = folder.resolveFile("file");
        childFile.createFile();
        final FileObject childFolder = folder.resolveFile("folder");
        childFolder.createFolder();

        FileObject[] childs = folder.getChildren();
        assertContains(childFile, childs);
        assertContains(childFolder, childs);

        childFile.delete();
        childs = folder.getChildren();
        assertNotContains(childFile, childs);

        childFolder.delete();
        childs = folder.getChildren();
        assertNotContains(childFolder, childs);

        folder.delete(Selectors.SELECT_ALL);
    }

    @Test public void copyTestProject() throws Exception {
        final FileObject testProject = resourceService.resolve("res:Entity");
        assertEquals(FileType.FOLDER, testProject.getType());

        final FileObject workspaceLocation = resourceService.resolve("Entity");
        workspaceLocation.createFolder();
        workspaceLocation.copyFrom(testProject, Selectors.SELECT_ALL);

        final FileObject includeLocation = workspaceLocation.resolveFile("include");
        assertEquals(FileType.FOLDER, includeLocation.getType());

        final FileObject jar = includeLocation.resolveFile("entity-java.jar");
        assertEquals(FileType.FILE, jar.getType());
        try(final ZipInputStream jarStream = new ZipInputStream(jar.getContent().getInputStream())) {
            assertNotNull(jarStream.getNextEntry());
        }

        final org.spoofax.terms.io.binary.TermReader reader =
            new org.spoofax.terms.io.binary.TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(
                IStrategoTerm.MUTABLE));

        final FileObject ctree = includeLocation.resolveFile("entity.ctree");
        assertEquals(FileType.FILE, ctree.getType());
        final IStrategoTerm ctreeTerm = reader.parseFromStream(ctree.getContent().getInputStream());
        assertNotNull(ctreeTerm);

        final FileObject esv = includeLocation.resolveFile("Entity.packed.esv");
        assertEquals(FileType.FILE, esv.getType());
        final IStrategoTerm esvTerm = reader.parseFromStream(esv.getContent().getInputStream());
        assertNotNull(esvTerm);

        final FileObject parseTable = includeLocation.resolveFile("Entity.tbl");
        assertEquals(FileType.FILE, parseTable.getType());
        final IStrategoTerm parseTableTerm = reader.parseFromStream(parseTable.getContent().getInputStream());
        assertNotNull(parseTableTerm);
    }
}
