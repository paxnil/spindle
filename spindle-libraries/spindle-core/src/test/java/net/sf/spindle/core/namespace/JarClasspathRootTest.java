package net.sf.spindle.core.namespace;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import net.sf.spindle.core.util.Files;

import org.apache.hivemind.Resource;

public class JarClasspathRootTest extends TestCase
{

    JarClasspathRoot root;
    
    @Override
    protected void setUp() throws Exception
    {
        root = getTapestryTestRoot();
    }

    private JarClasspathRoot getTapestryTestRoot() throws IOException, URISyntaxException
    {
        return new JarClasspathRoot("tapestryTest.jar");
    }
    
    public void test() throws IOException {
        Resource framework = root.getRelativeResource("org/apache/tapestry/Framework.library");
        
        URL url = framework.getResourceURL();
        
        assertNotNull(url);
        
        InputStream input = url.openStream();
        
        String all = Files.readFileToString(input, null);
        
        //TODO remove
        System.out.println(all);
    }
    
   

}
