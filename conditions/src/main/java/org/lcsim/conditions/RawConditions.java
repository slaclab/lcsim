package org.lcsim.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * 
 * @author Tony Johnson
 */
public interface RawConditions extends Conditions {
    InputStream getInputStream() throws IOException;

    Reader getReader() throws IOException;
}
