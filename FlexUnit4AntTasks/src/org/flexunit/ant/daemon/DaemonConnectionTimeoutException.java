package org.flexunit.ant.daemon;

public class DaemonConnectionTimeoutException extends RuntimeException
{
   private static final long serialVersionUID = -1831693574824039447L;
   
   public DaemonConnectionTimeoutException(String message)
   {
      super(message);
   }
   
   public DaemonConnectionTimeoutException(Throwable throwable)
   {
      super(throwable);
   }
   
   public DaemonConnectionTimeoutException(String message, Throwable throwable)
   {
      super(message, throwable);
   }
}
