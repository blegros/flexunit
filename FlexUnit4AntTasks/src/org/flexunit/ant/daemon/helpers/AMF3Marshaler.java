package org.flexunit.ant.daemon.helpers;

import java.io.IOException;

import flex.messaging.io.SerializationContext;

public class AMF3Marshaler
{
   private Amf3Input inputHelper;
   private ByteArrayInputStream inputStream;
   
   public AMF3Marshaler()
   {
      this(new SerializationContext());
   }
   
   public AMF3Marshaler(SerializationContext context)
   {
      this.inputHelper = new Amf3Input(context);
   }
   
   public AMF3Marshaler(Amf3Input in)
   {
      this.inputHelper = in;
   }
   
   public String getClassName(byte[] data) throws IOException
   {
      inputStream = new ByteArrayInputStream(data);
      inputHelper.reset();
      inputHelper.setInputStream(inputStream);

      return inputHelper.getClassName();
   }
   
   /**
    * NOT THREAD-SAFE.  Calls will return the last position of the last set input stream 
    */
   public int getLastPosition()
   {
      return inputStream.getLastPosition();
   }
   
   public Object unmarshal(byte[] data) throws IOException, ClassNotFoundException
   {
      inputStream = new ByteArrayInputStream(data);
      inputHelper.reset();
      inputHelper.setInputStream(inputStream);

      Object object = inputHelper.readObject();

      return object;
   }

}
