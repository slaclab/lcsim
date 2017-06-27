package org.lcsim.util.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.lcsim.util.cache.FileCache;

public abstract class TestUtil {

    private final static String lcioTestFileURL = "http://www.lcsim.org/test/lcio";

    public static String getLCIOTestURL(String filename) {
        return lcioTestFileURL + "/" + filename;
    }

    public static String getTestOutputDir() {
        return "target/test-output/";
    }

    public static class TestOutputFile extends File {

        public TestOutputFile(String filename) {
            super(getTestOutputDir() + filename);
            File dir = new File(getTestOutputDir());
            if (!dir.exists())
                dir.mkdir();
        }
    }

    public static class CachedInputFile {

        File file;

        public CachedInputFile(String url) {
            try {
                file = (new FileCache()).getCachedFile(new URL(url));
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

        public File getFile() {
            return file;
        }
    }
}