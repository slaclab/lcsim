package org.lcsim.conditions.readers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.lcsim.conditions.ConditionsReader;

public class DirectoryConditionsReader extends ConditionsReader {

    private File dir;

    public DirectoryConditionsReader(File file) throws IOException {
        this.dir = file;
    }

    public InputStream open(String name, String type) throws IOException {
        File file = new File(dir, name + "." + type);
        if (!file.exists()) {
            throw new IOException("Conditions " + name + "." + type + " not found, because directory " + file.getAbsolutePath() + " does not exist.");
        }
        return new BufferedInputStream(new FileInputStream(file));
    }

    public void close() throws IOException {
    }
}