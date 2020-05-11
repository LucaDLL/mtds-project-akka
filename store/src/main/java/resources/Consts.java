package resources;

import java.math.BigInteger;

public class Consts {
  public static final int idLength = 160;
  public static final BigInteger ringSize = (new BigInteger("2")).pow(idLength);
}