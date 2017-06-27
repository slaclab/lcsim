package org.lcsim.analysis;

import java.util.Map;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: WhizardEventFilter.java,v 1.1 2011/02/02 17:40:57 ngraf Exp $
 */
public class WhizardEventFilter extends Driver
{

    protected void process(EventHeader event)
    {
        /*
         *LOI qqbar files for Ecm=500 GeV
         uu    11775   e-L e+R
         uu    11776   e-R e+L

         cc    11779   e-L e+R
         cc    11780   e-R e+L

         dd    11795   e-L e+R
         dd    11796   e-R e+L

         ss     11799   e-L e+R
         ss     11800   e-R e+L

         bb    11803   e-L e+R
         bb    11804   e-R e+L

         */
        int[] lightQQbarProcesses
                = {
                    11775, 11776, 11779, 11780, 11795, 11796, 11799, 11800, 11803, 11804
                };
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

        System.out.println("idrup= " + idrup + " : weight= " + weight);
        boolean skipEvent = true;
        for (int i = 0; i < lightQQbarProcesses.length; ++i) {
            if (idrup == lightQQbarProcesses[i]) {
                skipEvent = false;
            }
        }
        if (skipEvent) {
            throw new Driver.NextEventException();
        }
    }
}
