package de.dfki.vsm.xtesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.util.bin.BINUtilities;

/**
 * @author Gregor Mehlmann
 */
public class TestBINTools {

  // Get The System logger
  private static final Logger sLogger = LoggerFactory.getLogger(TestBINTools.class);;

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  public static void main(String args[]) {
    try {

      //
      long x = System.currentTimeMillis();
      long y, z;
      byte[] a, b;

      //
      a = BINUtilities.LongToBytesBE(x);
      b = BINUtilities.LongToBytesLE(x);

      //
      y = BINUtilities.BytesBEToLong(a);
      z = BINUtilities.BytesLEToLong(b);

      //
      sLogger.info("x=" + x + "(" + BINUtilities.LongToHexString(x) + "," + BINUtilities.LongToOctString(x)
              + ")");
      sLogger.info("x=" + y + "(" + BINUtilities.LongToHexString(y) + "," + BINUtilities.LongToOctString(x)
              + ")");
      sLogger.info("x=" + z + "(" + BINUtilities.LongToHexString(z) + "," + BINUtilities.LongToOctString(x)
              + ")");

      //
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////
  public static void test(String args[]) {

    // The System Logger
    final Logger sLogger = LoggerFactory.getLogger(TestBINTools.class);;

    //
    short s = 3645;
    int i = 47484949;
    long l = 478909020L;
    float f = 9267.9556F;

    //
    sLogger.info("s:" + BINUtilities.ShortToHexString(s));

    byte[] sbe = BINUtilities.ShortToBytesBE(s);
    byte[] sle = BINUtilities.ShortToBytesLE(s);

    sLogger.info("sbe:" + BINUtilities.BytesToHexString(sbe));
    sLogger.info("sle:" + BINUtilities.BytesToHexString(sle));

    short s1 = BINUtilities.BytesBEToShort(sbe);
    short s2 = BINUtilities.BytesLEToShort(sle);

    sLogger.info("s1:" + s1 + " " + BINUtilities.ShortToHexString(s1));
    sLogger.info("s2:" + s2 + " " + BINUtilities.ShortToHexString(s2));
    sLogger.info("l:" + BINUtilities.IntToHexString(i));

    byte[] ibe = BINUtilities.IntToBytesBE(i);
    byte[] ile = BINUtilities.IntToBytesLE(i);

    sLogger.info("ibe:" + BINUtilities.BytesToHexString(ibe));
    sLogger.info("ile:" + BINUtilities.BytesToHexString(ile));

    int i1 = BINUtilities.BytesBEToInt(ibe);
    int i2 = BINUtilities.BytesLEToInt(ile);

    sLogger.info("i1:" + i1 + " " + BINUtilities.IntToHexString(i1));
    sLogger.info("i2:" + i2 + " " + BINUtilities.IntToHexString(i2));
    sLogger.info("l:" + BINUtilities.LongToHexString(l));

    byte[] lbe = BINUtilities.LongToBytesBE(l);
    byte[] lle = BINUtilities.LongToBytesLE(l);

    sLogger.info("lbe:" + BINUtilities.BytesToHexString(lbe));
    sLogger.info("lle:" + BINUtilities.BytesToHexString(lle));

    long l1 = BINUtilities.BytesBEToLong(lbe);
    long l2 = BINUtilities.BytesLEToLong(lle);

    sLogger.info("l1:" + l1 + " " + BINUtilities.LongToHexString(l1));
    sLogger.info("l2:" + l2 + " " + BINUtilities.LongToHexString(l2));
    sLogger.info("f:" + f);

    byte[] fbe = BINUtilities.FloatToBytesBE(f);
    byte[] fle = BINUtilities.FloatToBytesLE(f);

    sLogger.info("fbe:" + BINUtilities.BytesToHexString(fbe));
    sLogger.info("fle:" + BINUtilities.BytesToHexString(fle));

    float f1 = BINUtilities.BytesBEToFloat(fbe);
    float f2 = BINUtilities.BytesLEToFloat(fle);

    sLogger.info("f1:" + f1);
    sLogger.info("f2:" + f2);
    sLogger.info("x:" + BINUtilities.BytesLEToFloat(BINUtilities.FloatToBytesLE(12.57F)));
    sLogger.info("y:" + BINUtilities.BytesBEToFloat(BINUtilities.FloatToBytesBE(52.557F)));
  }
}
