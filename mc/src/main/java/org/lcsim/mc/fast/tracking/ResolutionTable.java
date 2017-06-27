package org.lcsim.mc.fast.tracking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ResolutionTable {
    private Map hash = new HashMap();

    public ResolutionTable(Reader in) throws IOException {
        BufferedReader inn = new TrimReader(in);
        setupTable(inn);
        inn.close();
    }

    LookupTable findTable(String name) {
        return (LookupTable) hash.get(name);
    }

    public LookupTable findTable(int i, int j) {
        String name = String.format("(%s,%s):", i + 1, j + 1);
        // System.out.printf("%s\n", name);
        return findTable(name);
    }

    void setupTable(BufferedReader in) throws IOException {
        // constant string to read Bruce's resolution table ini files
        String tokenLine = "Cov matrix entry";

        // Start by reading the parameter file
        for (;;) {
            String line = in.readLine();
            if (line == null)
                break;

            //
            // ww, 10/05/2000
            //
            // transform Bruce's cov matrix element index format into LCDs
            // ie "Cov matrix entry  n,  n"
            // to "(n,n):"
            //
            int elementI = 0;
            int elementJ = 0;
            if (line.indexOf(tokenLine) > -1) {
                String nLine = line.substring(line.indexOf(tokenLine) + tokenLine.length());
                StringBuffer sb = new StringBuffer("(");
                StringTokenizer tokenizer = new StringTokenizer(nLine, " ,");
                if (tokenizer.countTokens() == 2) {
                    String token = tokenizer.nextToken();
                    elementI = Integer.parseInt(token) - 1; // lcdtrk starts from 1
                    sb.append(token);
                    sb.append(",");
                    token = tokenizer.nextToken();
                    elementJ = Integer.parseInt(token) - 1; // lcdtrk starts from 1
                    sb.append(token);
                    sb.append("):");
                } else {
                    sb.append("--none--)");
                }
                line = sb.toString();
            }

            // end of addition
            if (!line.endsWith(":")) {
                throw new IOException("Syntax error in ResolutionTable");
            }
            hash.put(line, new LookupTable(in, elementI, elementJ));
        }
    }

    private class TrimReader extends BufferedReader {
        TrimReader(Reader source) {
            super(source);
        }

        public String readLine() throws IOException {
            for (;;) {
                String line = super.readLine();
                if (line == null)
                    return null;
                line = line.trim();
                if (line.length() > 0)
                    return line;
            }
        }
    }
}
