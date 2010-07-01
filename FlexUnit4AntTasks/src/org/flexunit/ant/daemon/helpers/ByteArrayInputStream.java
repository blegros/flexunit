package org.flexunit.ant.daemon.helpers;

public class ByteArrayInputStream extends java.io.ByteArrayInputStream
{

   public ByteArrayInputStream(byte[] buf)
   {
      super(buf);
   }
   
   public int getLastPosition()
   {
      return (super.pos == super.count) ? -1 : super.pos;
   }

}
