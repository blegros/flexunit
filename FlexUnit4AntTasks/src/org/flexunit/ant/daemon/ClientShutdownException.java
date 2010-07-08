package org.flexunit.ant.daemon;

public class ClientShutdownException extends Exception
{
   private static final long serialVersionUID = -8288417289588366467L;
   
   public ClientShutdownException()
   {
      super();
   }
   
   public ClientShutdownException(String message)
   {
      super(message);
   }
   
   public ClientShutdownException(Throwable throwable)
   {
      super(throwable);
   }
   
   public ClientShutdownException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

}
