package org.flexunit.ant.daemon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf0Input;
import flex.messaging.io.amf.Amf3Output;

public class AMF3Marshaler
{
   private Amf3Output outputHelper;
   private Amf0Input inputHelper;
   
   public AMF3Marshaler()
   {
      this(new SerializationContext());
   }
   
   public AMF3Marshaler(SerializationContext context)
   {
      this.outputHelper = new Amf3Output(context);
      this.inputHelper = new Amf0Input(context);
   }
   
   public AMF3Marshaler(Amf3Output out, Amf0Input in)
   {
      this.outputHelper = out;
      this.inputHelper = in;
   }
   
   public byte[] marshal(Object data) throws IOException
   {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      
      outputHelper.reset();
      outputHelper.setOutputStream(stream);
      outputHelper.writeObject(data);

      return stream.toByteArray();
   }
   
   public Object unmarshal(byte[] data) throws IOException, ClassNotFoundException
   {
      ByteArrayInputStream stream = new ByteArrayInputStream(data);
      inputHelper.reset();
      inputHelper.setInputStream(stream);

      Object object = inputHelper.readObject();

      return object;
   }
}
