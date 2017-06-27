package org.lcsim.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Resolves URIs to class resources.
 * 
 * For instance, the URI <code>http://www.lcsim.org/example/example.txt</code> would 
 * be resolved to <code>org/lcsim/example/example.txt</code>.
 *
 * @author Jeremy McCormick
 * @version $Id: ClasspathEntityResolver.java,v 1.5 2010/04/14 17:12:08 jeremy Exp $
 */
public class ClasspathEntityResolver
implements EntityResolver
{    	
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {                        

        //System.out.println("ClasspathEntityResolver.resolveEntity");
        //System.out.println("  publicId: " + publicId);
        //System.out.println("  systemId: " + systemId);
	
    	URL url = new URL(systemId);
    	String[] hostTokens = url.getHost().split("\\.");
    	String org = "";
    	for (int i = hostTokens.length - 1; i >= 0; i--) {
    		if (!hostTokens[i].equals("www"))
    			org += "/" + hostTokens[i];
    	}
    	org = org.substring(1);
    	String fullpath = org + (new URL(systemId)).getPath();     
    		
    	InputStream in = this.getClass().getClassLoader().getResourceAsStream(fullpath);
        
        InputSource src = new InputSource(in);
        src.setSystemId(systemId);
        src.setPublicId(publicId);
        
        return src;
    }
}
