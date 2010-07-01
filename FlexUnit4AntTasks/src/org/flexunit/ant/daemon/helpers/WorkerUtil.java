package org.flexunit.ant.daemon.helpers;

import java.nio.charset.Charset;

public class WorkerUtil
{
   public static final char NULL_BYTE = '\u0000';
   
   public static final String readUtf8String(byte[] value)
   {
        String result = null;
        
        if(value.length > 2)
        {
           int length = (int) value[1];
           result = new String(value, 2, length, Charset.forName("UTF-8"));
        }
        
        return result;
   }
   
   public static final byte[] toUtf8Bytes(String value)
   {
      byte[] array = value.getBytes(Charset.forName("UTF-8"));
      byte[] utf = new byte[array.length + 2];
      
      utf[0] = NULL_BYTE;
      utf[1] = new Integer(array.length).byteValue();
      System.arraycopy(array, 0, utf, 2, array.length);
      
      return utf;
   }
   
   public static final byte[] append(byte[] array1, byte[] array2)
   {
      if(array1 == null || array1.length == 0)
      {
         return array2;
      }
      
      if(array2 == null || array2.length == 0)
      {
         return array1;
      }
      
      byte[] copy = new byte[array1.length + array2.length];
      System.arraycopy(array1, 0, copy, 0, array1.length);
      System.arraycopy(array2, 0, copy, array1.length + 1, array2.length);
      
      return copy;
   }
   
   public static final byte[] slice(byte[] data, int offset)
   {
      return slice(data, offset, data.length - offset);
   }
   
   public static final byte[] sliceUtf(String value, byte[] data)
   {
      return slice(data, value.length() + 2, data.length - value.length() - 2);
   }
   
   public static final byte[] slice(byte[] data, int offset, int length)
   {
      byte[] subdata = new byte[length];
      System.arraycopy(data, offset, subdata, 0, length);
      
      return subdata;
   }
}
