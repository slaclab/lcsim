package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.physics.vec.BasicHep3Vector;

import java.io.IOException;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseCalorimeterHit;

/**
 * SIO-based I/O implementation of the CalorimeterHit interface
 *
 * @author Guilherme Lima
 * @version $Id: SIOCalorimeterHit.java,v 1.18 2013/02/05 00:18:34 jeremy Exp $
 */
class SIOCalorimeterHit extends BaseCalorimeterHit {

    SIOCalorimeterHit(SIOInputStream in, int flags, int version, LCMetaData meta) throws IOException {
        int cellid0 = in.readInt();
        int cellid1 = 0;
        if (LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_ID1) || version == 8) {
            cellid1 = in.readInt();
        }
        this.id = ((long) cellid1) << 32 | cellid0;
        this.correctedEnergy = in.readFloat();
        if (version >= 1051 && LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_ENERGY_ERROR)) {
            energyError = in.readFloat();
        }

        if (version > 1002 && (flags & (1 << LCIOConstants.RCHBIT_TIME)) != 0) {
            time = in.readFloat();
        }
        if ((flags & (1 << LCIOConstants.RCHBIT_LONG)) != 0) {
            positionVec = new BasicHep3Vector(in.readFloat(), in.readFloat(), in.readFloat());
        }

        if (version > 1002) {
            type = in.readInt();
            Object tempHit = in.readPntr();

            // the logic has been reverted in v1.3 !
            if ((flags & (1 << LCIOConstants.RCHBIT_NO_PTR)) == 0) {
                in.readPTag(this);
            }
        } else {
            if ((flags & (1 << LCIOConstants.RCHBIT_NO_PTR)) != 0) {
                in.readPTag(this);
            }
        }
        setMetaData(meta);
    }

    static void write(CalorimeterHit hit, SIOOutputStream out, int flags) throws IOException {
        long cellID = hit.getCellID();
        out.writeInt((int) cellID);

        if (LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_ID1)) {
            out.writeInt((int) (cellID >> 32));
        }

        out.writeFloat((float) hit.getCorrectedEnergy());
        if (LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_ENERGY_ERROR)) {
            out.writeFloat((float) hit.getEnergyError());
        }

        if (LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_TIME)) {
            out.writeFloat((float) hit.getTime());
        }

        if ((flags & (1 << LCIOConstants.CHBIT_LONG)) != 0) {
            double[] pos;
            if (hit.getPosition() != null) {
                pos = hit.getPosition();
            } else {
                pos = new double[3];
            }
            out.writeFloat((float) pos[0]);
            out.writeFloat((float) pos[1]);
            out.writeFloat((float) pos[2]);
        }

        out.writeInt(hit.getType());

        // FIXME: Java CalorimeterHit interface does not support getRawHit()
        if (!LCIOUtil.bitTest(flags, LCIOConstants.RCHBIT_NO_PTR)) {
            //assert false : "Pointer to raw hit not implemented!";
            //out.writePntr( hit.getRawHit() );
            out.writePntr(null);
        }

        out.writePTag(hit);
    }
}
