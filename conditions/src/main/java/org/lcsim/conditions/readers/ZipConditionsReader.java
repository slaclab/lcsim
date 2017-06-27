package org.lcsim.conditions.readers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.lcsim.conditions.ConditionsReader;

public class ZipConditionsReader extends ConditionsReader {

    private ZipFile zip;

    public ZipConditionsReader(File file) throws IOException {
        this.zip = new ZipFile(file, ZipFile.OPEN_READ);
    }

    public InputStream open(String name, String type) throws IOException {
        ZipEntry entry = zip.getEntry(name + "." + type);
        if (entry == null) {
            throw new IOException("Conditions " + name + "." + type + " not found");
        }
        return zip.getInputStream(entry);
    }

    public void close() throws IOException {
        zip.close();
    }
}