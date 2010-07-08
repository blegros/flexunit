package org.flexunit.ant.report;

public class ReportException extends Exception
{
   private static final long serialVersionUID = -237210882820658618L;

   public ReportException()
   {
      super();
   }
   
   public ReportException(String message)
   {
      super(message);
   }
   
   public ReportException(Throwable throwable)
   {
      super(throwable);
   }
   
   public ReportException(String message, Throwable throwable)
   {
      super(message, throwable);
   }
}
