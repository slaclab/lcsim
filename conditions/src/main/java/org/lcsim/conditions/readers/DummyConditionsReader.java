package org.lcsim.conditions.readers;

import java.io.IOException;
import java.io.InputStream;

import org.lcsim.conditions.ConditionsReader;

public class DummyConditionsReader extends ConditionsReader {

    public InputStream open(String name, String type) throws IOException {
        throw new IOException("Conditions " + name + "." + type + " not found");
    }

    public void close() throws IOException {
    }
}