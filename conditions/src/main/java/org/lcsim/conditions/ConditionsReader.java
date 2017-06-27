package org.lcsim.conditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.lcsim.conditions.ConditionsManager.ConditionsNotFoundException;
import org.lcsim.conditions.readers.BaseClasspathConditionsReader;
import org.lcsim.conditions.readers.DirectoryConditionsReader;
import org.lcsim.conditions.readers.DummyConditionsReader;
import org.lcsim.conditions.readers.ZipConditionsReader;
import org.lcsim.util.cache.FileCache;
import org.lcsim.util.cache.FileCache.Validator;

/**
 * 
 * @author tonyj
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public abstract class ConditionsReader {

    private static Properties aliases;
    private static final File home = new File(FileCache.getCacheRoot(), ".lcsim");
    private static FileCache cache;

    /**
     * Called by {@link ConditionsManager#setDetector} to tell this reader that
     * it needs to prepare itself for reading conditions for the specified
     * detector and run.
     * <p>
     * The implementation provided by this class returns <tt>false</tt> if the
     * specified detector name is equal to the name of the current detector
     * known to the specified {@link ConditionsManager}, and throws
     * <tt>IllegalArgumentException</tt> if it is not. Subclasses need to
     * override this method if conditions might be different for different runs
     * with the same detector.
     * 
     * @return <tt>true</tt> if conditions for the specified detector/run may be
     *         different from conditions for the previous detector/run
     *         combination; <tt>false</tt> otherwise.
     * @throws IllegalArgumentException if this <tt>ConditionsReader</tt> cannot
     *             handle the specified detector/run.
     * @throws IOException if the reader fails to update for any other reason.
     */
    protected boolean update(ConditionsManager manager, String detectorName, int run) throws IOException {
        if (detectorName.equals(manager.getDetector())) {
            return false;
        } else {
        	// ?????
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get a list of available detectors
     */
    // FIXME: This should be removed.
    public static List<String> getDetectorNames() {
        Set<String> set = new HashSet<String>();
        if (aliases == null) {
            aliases = loadAliases();
        }
        for (Object key : aliases.keySet()) {
            set.add(key.toString());
        }

        try {
            if (cache == null) {
                cache = new FileCache(new File(home, "cache"));
            }
            // FIXME: Not even sure this taglist should be supported at all any longer.
            File file = cache.getCachedFile(new URL("http://www.lcsim.org/detectors/taglist.txt"));
            if (file != null) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                for (;;) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    set.add(line);
                }
                reader.close();
            }
        } catch (Exception ex) {
            System.err.println("Error reading file taglist.txt: " + ex);
        }

        List result = new ArrayList(set);
        Collections.sort(result);
        return result;
    }

    public static void addAlias(String alias, String target) {
        if (aliases == null) {
            aliases = loadAliases();
        }
        aliases.setProperty(alias, target);
    }

    public static ConditionsReader createDummy() {
        return new DummyConditionsReader();
    }

    // FIXME: Alias should be a feature that is easily turned off.
    private static String resolveAlias(final String detectorName) throws IOException {
        String name = detectorName;
        for (int i = 0;; i++) {
            String alias = aliases.getProperty(name);
            if (alias == null) {
                break;
            }
            if (i > 100) {
                throw new IOException("Recursive name translation: " + name);
            }
            name = alias;
        }
        return name;
    }

    /**
     * Try to find the conditions associated with this detector. For more
     * details see @link
     * http://confluence.slac.stanford.edu/display/ilc/Conditions+database
     */
    // FIXME: This method should not be present in this class.  It uses the ConditionsReader interface,
    // as well as all its sub-classes.  Probably better put in the ConditionsManagerImplementation.
    static ConditionsReader create(String detectorName, int run) throws ConditionsNotFoundException {
        String name = detectorName;
        try {
            if (cache == null) {
                cache = new FileCache(new File(home, "cache"));
            }
            if (aliases == null) {
                aliases = loadAliases();
            }

            name = resolveAlias(detectorName);
              
            if (name.contains(":")) {
                // Name is a URL.
                URL url = new URL(name);
                if (url.getProtocol().equals("file") && (url.getHost() == null || url.getHost().length() == 0)) {
                    File file = new File(url.getPath());
                    // Check if exists.
                    if (!file.exists()) {
                        throw new RuntimeException("The URL " + url.toString() + " used by detector " + name + " does not exist.");
                    }
                    if (file.isDirectory()) {
                        return new DirectoryConditionsReader(file);
                    } else {
                        return new ZipConditionsReader(file);
                    }
                } else {
                    File file = downloadDetectorDescription(url);
                    return new ZipConditionsReader(file);
                }
            } else {

                // Search the classpath for conditions.
                try {
                    return new BaseClasspathConditionsReader(name);
                } catch (IOException x) {
                    // System.out.println(x.getLocalizedMessage());
                }

                // Search for a local, cached copy.
                File detectorDir = new File(home, "detectors"); // FIXME: Hard-coded directory location.
                File zipFile = new File(detectorDir, name + ".zip");
                if (zipFile.exists()) {
                    return new ZipConditionsReader(zipFile);
                }
                File dirFile = new File(detectorDir, name);
                if (dirFile.exists() && dirFile.isDirectory()) {
                    return new DirectoryConditionsReader(dirFile);
                }

                // Finally, try to pull the detector conditions from the lcsim.org website.
                try {
                    URL url = new URL("http://www.lcsim.org/detectors/" + name + ".zip"); // FIXME: Hard-coded URL.
                    File file = downloadDetectorDescription(url);
                    return new ZipConditionsReader(file);
                } catch (FileNotFoundException x) {
                    throw new ConditionsNotFoundException(name, run);
                }
            }
        } catch (MalformedURLException x) {
            throw new ConditionsNotFoundException(name, run, x);
        } catch (IOException x) {
            throw new ConditionsNotFoundException(name, run, x);
        }
    }

    /**
     * Creates <tt>ConditionsReader</tt> to handle the specified detector and
     * run.
     * @throws ConditionsNotFoundException if creation of the reader fails for
     *             any reason.
     */
    static ConditionsReader create(ConditionsManager manager, String detectorName, int run) throws ConditionsNotFoundException {
        ConditionsReader reader = create(detectorName, run);
        Properties prop = new Properties();
        try {
            InputStream in = reader.open("detector", "properties");
            try {
                prop.load(in);
            } finally {
                in.close();
            }
        } catch (IOException x) {
            // For now: having failed to find or load detector.properties, use
            // unmodified reader.  Uncomment the line below if we decide this should 
            // be treated as an error.
            //throw new ConditionsNotFoundException(detectorName, run, x);
        }
        // FIXME: The detector conditions themselves should not determine what reader is used.  
        // This should be handled by configuring the conditions system itself directly.
        String readerClassName = prop.getProperty("ConditionsReader");
        if (readerClassName != null) {
            try {
                Class readerClass = Class.forName(readerClassName);
                reader = (ConditionsReader) readerClass.getDeclaredConstructor(ConditionsReader.class).newInstance(reader);
                reader.update(manager, detectorName, run);
            } catch (Exception x) {
                throw new ConditionsNotFoundException(detectorName, run, x);
            }
        }
        return reader;
    }

    abstract public InputStream open(String name, String type) throws IOException;

    abstract public void close() throws IOException;

    // FIXME: Aliasing is very dangerous when matching detectors with event data files.  
    // There should be an option to turn this off.
    private static Properties loadAliases() {
        Properties result = new Properties();
        try {
            File f = new File(home, "alias.properties");
            InputStream in = new FileInputStream(f);
            if (in != null) {
                try {
                    result.load(in);
                } finally {
                    in.close();
                }
            }
        } catch (IOException x) {
        }
        return result;
    }

    private static File downloadDetectorDescription(URL url) throws IOException {
        return cache.getCachedFile(url, new DetectorFileValidator());
    }

    private static class DetectorFileValidator implements Validator {

        public void checkValidity(URL url, File file) throws IOException {
            // Check if the file looks good. It should contain a file called
            // detector.properties in the root directory
            ZipFile zip = new ZipFile(file, ZipFile.OPEN_READ);
            try {
                ZipEntry header = zip.getEntry("detector.properties");
                if (header == null) {
                    throw new IOException("No detector.properties entry in file downloaded from " + url);
                }
                Properties props = new Properties();
                props.load(zip.getInputStream(header));
            } finally {
                zip.close();
            }
        }
    }
}
