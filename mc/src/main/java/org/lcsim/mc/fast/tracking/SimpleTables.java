package org.lcsim.mc.fast.tracking;

import org.lcsim.conditions.ConditionsSet;

/**
 *
 * @author Daniel
 */
public class SimpleTables {

    private double ConstantTerm;
    private double ThetaTerm;
    private double TanLambdaErrorScale;
    private double PhiErrorScale;
    private double D0ErrorScale;
    private double Z0ErrorScale;

    /** Creates a new instance of SimpleTables */
    public SimpleTables(ConditionsSet set) {
        ConstantTerm = set.getDouble("ConstantTerm");
        ThetaTerm = set.getDouble("ThetaTerm");
        TanLambdaErrorScale = set.getDouble("TanLambdaErrorScale");
        PhiErrorScale = set.getDouble("PhiErrorScale");
        D0ErrorScale = set.getDouble("D0ErrorScale");
        Z0ErrorScale = set.getDouble("Z0ErrorScale");
    }

    public double getConstantTerm() {
        return ConstantTerm;
    }

    public double getThetaTerm() {
        return ThetaTerm;
    }

    public double getTanLambdaErrorScale() {
        return TanLambdaErrorScale;
    }

    public double getPhiErrorScale() {
        return PhiErrorScale;
    }

    public double getD0ErrorScale() {
        return D0ErrorScale;
    }

    public double getZ0ErrorScale() {
        return Z0ErrorScale;
    }

}
