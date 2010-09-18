package org.flexunit.ant.report;

public class TestReportException extends Exception
{
   private static final long serialVersionUID = -237210882820658618L;

   public TestReportException()
   {
      super();
   }
   
   public TestReportException(String message)
   {
      super(message);
   }
   
   public TestReportException(Throwable throwable)
   {
      super(throwable);
   }
   
   public TestReportException(String message, Throwable throwable)
   {
      super(message, throwable);
   }
}
