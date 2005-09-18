package core.test.eclipse;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.eclipse.TapestryProject;
import com.iw.plugins.spindle.core.eclipse.adapters.SpindleProjectAdapterFactory;

import core.test.SuiteOfTestCases;

public class ProjectAdapterFactoryTests extends AbstractEclipseTestCase
{
    IProject aaCoreAdaptersProject;

    IProject coreAdaptersProject;

    SpindleProjectAdapterFactory factory;

    public static Test suite()
    {
        return new SuiteOfTestCases.Suite(ProjectAdapterFactoryTests.class);
    }

    public ProjectAdapterFactoryTests(String name)
    {
        super(name);
    }

    public void setUpSuite() throws Exception
    {
        super.setUpSuite();
        aaCoreAdaptersProject = setUpProject("AACoreAdapters");
        coreAdaptersProject = setUpProject("CoreAdapters");
        // factory is stateless so we only need to create it once.
        factory = new SpindleProjectAdapterFactory();
    }

    public void tearDownSuite() throws Exception
    {
        super.tearDownSuite();
        deleteProject("AACoreAdapters");
        deleteProject("CoreAdapters");
    }

    public void testProject()
    {
        assertNotNull(aaCoreAdaptersProject);
        assertNotNull(coreAdaptersProject);
    }

    public void testNull()
    {
        assertNull(factory.getAdapter(null, IJavaProject.class));
        assertNull(factory.getAdapter(null, IProject.class));
        assertNull(factory.getAdapter(null, ITapestryProject.class));
    }

    public void testGetIProjectFromIJavaProject() throws Exception
    {
        IJavaProject coreAdaptersJavaProject = JavaCore.create(coreAdaptersProject);
        IProject adapted = (IProject) factory.getAdapter(coreAdaptersJavaProject, IProject.class);
        assertEquals(coreAdaptersProject, adapted);
        assertTrue(logger.isEmpty());
    }

    public void testGetIProjectFromIType() throws Exception
    {
        IJavaProject coreAdaptersJavaProject = JavaCore.create(coreAdaptersProject);
        // it's binary but only found in CoreAdapters project - should get expected project
        IType testBinaryClass = coreAdaptersJavaProject.findType("tests.TestBinaryClass");
        assertNotNull(testBinaryClass);

        IProject adapted = (IProject) factory.getAdapter(testBinaryClass, IProject.class);
        assertEquals(coreAdaptersProject, adapted);
        assertTrue(logger.isEmpty());

        logger.clear();

        // it's binary but found in both AACoreAdapters and CoreAdapters
        // need to ensure we get the project we expect.

        IType ambiguousBinaryClass = coreAdaptersJavaProject.findType("tests.AmbiguousBinaryClass");
        assertNotNull(ambiguousBinaryClass);

        adapted = (IProject) factory.getAdapter(testBinaryClass, IProject.class);
        assertEquals(coreAdaptersProject, adapted);
        assertTrue(logger.isEmpty());

        logger.clear();

        IType sourceClass = coreAdaptersJavaProject.findType("tests.SourceClass");
        assertNotNull(sourceClass);

        adapted = (IProject) factory.getAdapter(testBinaryClass, IProject.class);
        assertEquals(coreAdaptersProject, adapted);
        assertTrue(logger.isEmpty());

    }

    public void testGetIProjectFromIResource() throws Exception
    {
        IFile fileResource = coreAdaptersProject.getFile(new Path("src/tests/empty.txt"));
        IProject adapted = (IProject) factory.getAdapter(fileResource, IProject.class);
        assertEquals(coreAdaptersProject, adapted);

        assertTrue(logger.isEmpty());

        logger.clear();

        IFolder folderResource = coreAdaptersProject.getFolder("non-existant");
        // even though we know the folder does not exist - we can get the project from it
        adapted = (IProject) factory.getAdapter(folderResource, IProject.class);
        assertEquals(coreAdaptersProject, adapted);

        assertTrue(logger.isEmpty());

    }

    public void testGetIProjectFromJarEntryFiles() throws Exception
    {
        IJavaProject coreAdaptersJavaProject = JavaCore.create(coreAdaptersProject);

        // unambiguous jar entry file
        IStorage jarEntryStorage = obtainJarEntryStorage(
                coreAdaptersJavaProject,
                "tests",
                "unambiguous.txt");

        IProject adapted = (IProject) factory.getAdapter(jarEntryStorage, IProject.class);
        assertEquals(coreAdaptersProject, adapted);

        assertTrue(logger.isEmpty());

        logger.clear();

        jarEntryStorage = obtainJarEntryStorage(coreAdaptersJavaProject, "tests", "ambiguous.txt");

        adapted = (IProject) factory.getAdapter(jarEntryStorage, IProject.class);
        assertEquals(coreAdaptersProject, adapted);

        assertTrue(logger.isEmpty());
    }

    public void testGetIJavaProject() throws Exception
    {
        IJavaProject expected = JavaCore.create(coreAdaptersProject);

        IFile file = coreAdaptersProject.getFile(new Path("/src/tests/SourceClass.java"));
        assertTrue(file.exists());
        IJavaElement element = JavaCore.create(file);

        IJavaProject adapted = (IJavaProject) factory.getAdapter(element, IJavaProject.class);
        assertEquals(expected, adapted);

        assertTrue(logger.isEmpty());
    }

    public void testGetITapestryProject() throws Exception
    {
        TapestryProject.addTapestryNature(coreAdaptersProject, false);
        IJavaProject jproject = JavaCore.create(coreAdaptersProject);

        ITapestryProject adapted = (ITapestryProject) factory.getAdapter(
                jproject,
                ITapestryProject.class);
        assertNotNull(adapted);

        assertTrue(logger.isEmpty());

        logger.clear();

        adapted = (ITapestryProject) factory.getAdapter(
                aaCoreAdaptersProject,
                ITapestryProject.class);
        assertNull(adapted);
        
        assertTrue(logger.isEmpty());
    }

    private IStorage obtainJarEntryStorage(IJavaProject project, String pack, String name)
            throws Exception
    {
        IStorage result = null;
        IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++)
        {

            if (roots[i].getKind() == IPackageFragmentRoot.K_BINARY)
            {
                IPackageFragment frag = roots[i].getPackageFragment(pack);
                if (!frag.exists())
                    continue;

                result = findStorageInPackage(frag, name);
                if (result != null)
                    break;
            }
        }
        System.out.println(result);
        return result;
    }

    private IStorage findStorageInPackage(IPackageFragment frag, String name) throws Exception
    {
        IStorage result = null;
        Object[] njr = frag.getNonJavaResources();
        for (int i = 0; i < njr.length; i++)
        {
            IStorage temp = (IStorage) njr[i];
            if (temp.getName().equals(name))
            {
                result = temp;
                break;
            }
        }
        return result;
    }

}
