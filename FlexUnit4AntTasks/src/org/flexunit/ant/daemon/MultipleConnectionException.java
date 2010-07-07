package org.flexunit.ant.daemon;

public class MultipleConnectionException extends Exception
{
   private static final long serialVersionUID = -1831693574824039447L;
   
   public MultipleConnectionException(String message)
   {
      super(message);
   }
   
   public MultipleConnectionException(Throwable throwable)
   {
      super(throwable);
   }
   
   public MultipleConnectionException(String message, Throwable throwable)
   {
      super(message, throwable);
   }
}
