package org.flexunit.ant.daemon.workers;

public class WorkerException extends Exception
{
   private static final long serialVersionUID = 3939492120769889639L;
   
   public WorkerException(String message)
   {
      super(message);
   }
   
   public WorkerException(Throwable throwable)
   {
      super(throwable);
   }
   
   public WorkerException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

}
