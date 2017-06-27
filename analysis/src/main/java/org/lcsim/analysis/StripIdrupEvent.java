package org.lcsim.analysis;

import java.util.Map;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class StripIdrupEvent extends Driver
{

    private boolean _debug = true;
    private int _idrupToWrite;
    private int _numberToWrite;
    private int _numberWritten;

    protected void process(EventHeader event)
    {
//        System.out.println("in StripIdrupEvent process: number written: "+_numberWritten+" number to write: " +_numberToWrite);
        Map<String, int[]> iparams = event.getIntegerParameters();
        int idrup = 0;
        if (iparams.containsKey("idrup")) {
            idrup = iparams.get("idrup")[0];
        }
        if (iparams.containsKey("_idrup")) {
            idrup = iparams.get("_idrup")[0];
        }

        Map<String, float[]> fparams = event.getFloatParameters();
        float weight = 1.0f;
        if (fparams.containsKey("_weight")) {
            weight = fparams.get("_weight")[0];
        }
        if (_debug) {
            System.out.println("idrup= " + idrup + " : weight= " + weight);
        }
        boolean skipEvent = true;

        if (idrup == _idrupToWrite) {
            if (_numberWritten < _numberToWrite) {
                ++_numberWritten;
                System.out.println("writing event "+_numberWritten+" of "+_numberToWrite+" with idrup= " + idrup + " : weight= " + weight);
                skipEvent = false;
            }
        }

        if (skipEvent) {
            throw new Driver.NextEventException();
        }
        
//        if(_numberWritten >= _numberToWrite)
//        {
//            System.out.println("Wrote "+_numberWritten+" events of idrup= "+_idrupToWrite);
//            System.out.println("Exiting...");
//            throw new Driver.AbortRunException();
//        }
        
    }

    public void setDebug(boolean debug)
    {
        _debug = debug;
    }

    public void setIdrup(int idrup)
    {
        _idrupToWrite = idrup;
    }
    
    public void setNumberToWrite(int numberToWrite)
    {
        _numberToWrite = numberToWrite;
    }
}
