package org.lcsim.conditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * 
 * @author Tony Johnson
 */
class RawConditionsImplementation extends ConditionsImplementation implements RawConditions {
    /** Creates a new instance of RawConditionsImplementation */
    RawConditionsImplementation(ConditionsManagerImplementation manager, String name) {
        super(manager, name);
    }

    public InputStream getInputStream() throws IOException {
        String type;
        String name = getName();
        int pos = name.lastIndexOf('.');
        if (pos < 0)
            type = "ini";
        else {
            type = name.substring(pos + 1);
            name = name.substring(0, pos);
        }
        return getManager().open(name, type);
    }

    public Reader getReader() throws IOException {
        return new InputStreamReader(getInputStream());
    }
}
