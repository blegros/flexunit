package org.flexunit.ant.report;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Aggregate class representing a collection of Suite stored in a Map<String, Suite>
 */
public class Suites extends HashMap<String, Suite>
{
   private static final long serialVersionUID = 2078272511659655555L;

   public Suites()
   {
      super();
   }
   
   /**
    * String version of all suites.
    */
   public String getSummary()
   {
      StringBuilder summary = new StringBuilder();
      
      int runs = 0;
      int errors = 0;
      int failures = 0;
      int ignores = 0;
      long time = 0;
      
      for(Suite suite : this.values())
      {
         runs += suite.getTestCount();
         errors += suite.getErrorCount();
         failures += suite.getFailureCount();
         ignores += suite.getIgnoreCount();
         time += suite.getTotalTime();
         
         summary.append(suite.getSummary());
         summary.append('\n');
      }
      
      summary.append("\nResults :\n\n");
      
      summary.append(MessageFormat.format(ReportFormatUtil.TEST_RUN_PLAIN_TEMPLATE, new Object[] { 
            new Integer(runs), 
            new Integer(failures), 
            new Integer(errors),
            new Integer(ignores),
            ReportFormatUtil.formatTime(time)
         }));
      
      summary.append('\n');
      
      return summary.toString();
   }
   
   /**
    * Determines if any reports have failures
    */
   public boolean hasFailures()
   {
      for(Suite suite : this.values())
      {
         if(suite.hasFailures())
         {
            return true;
         }
      }
      
      return false;
   }
}
