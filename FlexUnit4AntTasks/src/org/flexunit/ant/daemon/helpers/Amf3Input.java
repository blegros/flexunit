package org.flexunit.ant.daemon.helpers;

import java.io.IOException;

import flex.messaging.io.SerializationContext;

public class Amf3Input extends flex.messaging.io.amf.Amf3Input
{
   public Amf3Input(SerializationContext context)
   {
      super(context);
   }
   
   public String getClassName() throws IOException
   {
      String name = null;
      
      int type = in.readByte();  //first byte is AMF type identifier
      
      if(type == kObjectType)
      {
         int ref = readUInt29(); //next byte is in AMF3 29-bit format
         
         if(((ref & 3) != 1))    //verify it's not a reference
         {
            name = readString(); //the next value read off the buffer will be the classname
         }
      }
      
      return name;
   }
}
