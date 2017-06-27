package org.lcsim.util.loop;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOWriter;

/**
 * A driver for writing out LCIO events.
 * 
 * By default this will write out the entire event, but you can control what collections are written 
 * out by using {@link #setIgnoreCollection(String)}, {@link #setIgnoreCollections(String[])},
 * {@link #setWriteOnlyCollection(String)}, and {@link #setWriteOnlyCollections(String[])}.
 * 
 * @author tonyj
 * @see org.lcsim.util.lcio.LCIOWriter
 */
public class LCIODriver extends Driver {
    private LCIOWriter writer;
    private Set<String> listIgnore = new HashSet<String>();
    private Set<String> listKeep = new HashSet<String>();
    private File outputFile;

    public LCIODriver(String file) {
        this(addFileExtension(file), null);
    }

    public LCIODriver(File file) {
        this(file, null);
    }

    public LCIODriver(String file, Collection<String> listIgnore) {
        this(new File(addFileExtension(file)), listIgnore);
    }

    public LCIODriver(File file, Collection<String> listIgnore) {
        this.outputFile = file;
        if (listIgnore != null) {
            this.listIgnore.addAll(listIgnore);
        }
    }

    public LCIODriver() {
    }

    public void setOutputFilePath(String filePath) {
        outputFile = new File(addFileExtension(filePath));
    }

    public void setIgnoreCollections(String[] ignoreCollections) {
        listIgnore.addAll(Arrays.asList(ignoreCollections));
    }

    public void setWriteOnlyCollections(String[] keepCollections) {
        listKeep.addAll(Arrays.asList(keepCollections));
    }

    public void setIgnoreCollection(String ignoreCollection) {
        listIgnore.add(ignoreCollection);
    }

    public void setWriteOnlyCollection(String writeOnlyCollection) {
        listKeep.add(writeOnlyCollection);
    }

    private void setupWriter() {
        // Cleanup existing writer.
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
                writer = null;
            } catch (IOException x) {
                System.err.println(x.getMessage());
            }
        }

        // Setup new writer.
        try {
            writer = new LCIOWriter(outputFile);
        } catch (IOException x) {
            throw new RuntimeException("Error creating writer", x);
        }
        writer.addAllIgnore(listIgnore);
        writer.addAllWriteOnly(listKeep);

        try {
            writer.reOpen();
        } catch (IOException x) {
            throw new RuntimeException("Error rewinding LCIO file", x);
        }
    }

    protected void startOfData() {
        setupWriter();
    }

    protected void endOfData() {
        try {
            writer.close();
        } catch (IOException x) {
            throw new RuntimeException("Error rewinding LCIO file", x);
        }
    }

    protected void process(EventHeader event) {
        try {
            writer.write(event);
        } catch (IOException x) {
            throw new RuntimeException("Error writing LCIO file", x);
        }
    }

    protected void suspend() {
        try {
            writer.flush();
        } catch (IOException x) {
            throw new RuntimeException("Error flushing LCIO file", x);
        }
    }

    private static String addFileExtension(String filePath) {
        if (!filePath.endsWith(".slcio")) {
            return filePath + ".slcio";
        } else
            return filePath;
    }
}
