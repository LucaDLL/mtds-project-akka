package resources;

import java.math.BigInteger;

public class Consts {
  public static final BigInteger TWO_BIG_INTEGER = new BigInteger("2");
  public static final int ID_LENGTH = 160;
  public static final BigInteger RING_SIZE = TWO_BIG_INTEGER.pow(ID_LENGTH); //2^160

}