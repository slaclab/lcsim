package org.lcsim.job;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A simple class loader that accepts URLs and extends the system class loader.
 * 
 * @author jeremym
 * @version $id: $
 */
class LCSimClassLoader extends URLClassLoader {

    LCSimClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader());
    }

    public void addURL(URL url) {
        this.addURL(url);
    }
}