package org.lcsim.job;

import java.io.IOException;

import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

public class AidaSaveDriver extends Driver {

    String outputFileName = "plots.aida";
    boolean verbose = false;

    public AidaSaveDriver() {
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void endOfData() {
        try {
            if (verbose)
                System.out.println("Saving AIDA file to " + outputFileName + " ...");
            AIDA.defaultInstance().saveAs(outputFileName);
        } catch (IOException x) {
            throw new RuntimeException("Problem saving AIDA file to " + outputFileName + ".", x);
        }
    }
}
