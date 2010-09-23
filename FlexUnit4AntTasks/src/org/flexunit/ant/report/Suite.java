package org.flexunit.ant.report;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.util.DateUtils;
import org.flexunit.ant.LoggingUtil;

public class Suite
{
   private String className;
   private List<TestResult> tests;
   
   private int testCount = 0;
   private int failureCount = 0;
   private int errorCount = 0;
   private int successCount = 0;
   private int ignoreCount = 0;
   private long totalTime = 0L;

   public Suite(String className)
   {
      this.className = className;
      this.tests = new ArrayList<TestResult>();
   }

   public String getClassName()
   {
      return className;
   }
   
   public int getTestCount()
   {
      return testCount;
   }

   public int getFailureCount()
   {
      return failureCount;
   }

   public int getErrorCount()
   {
      return errorCount;
   }

   public int getSuccessCount()
   {
      return successCount;
   }

   public int getIgnoreCount()
   {
      return ignoreCount;
   }

   public long getTotalTime()
   {
      return totalTime;
   }

   public void addTest(TestResult testResult)
   {
      if(className.equals(testResult.getClassName()))
      {
         //set the test result's name to the class name to report issues outside of test (i.e. - Before, After, etc)
         if(testResult.getName() == null)
         {
            testResult.setName(testResult.getClassName());
         }
         
         //filter for duplicates
         if(!tests.contains(testResult))
         {
            tests.add(testResult);
            incrementCounts(testResult);  //keep running counts to avoid unneeded iteration
         }
      }
   }
   
   private void incrementCounts(TestResult result)
   {
      if(!result.getName().equals(className))
      {
         testCount++;
      }
      
      String template = null;
      if(result.isSuccess())
      {
         successCount++;
      }
      else if(result.isFailure())
      {
         failureCount++;
         template = ReportFormatUtil.FAILED_PLAIN_TEMPLATE;
      }
      else if(result.isError())
      {
         errorCount++;
         template = ReportFormatUtil.ERRORED_PLAIN_TEMPLATE;
      }
      else if(result.isIgnored())
      {
         ignoreCount++;
         template = ReportFormatUtil.IGNORED_PLAIN_TEMPLATE;
      }
      
      totalTime += result.getTime();
      
      //Log as failures, errors and ignores come in
      if(template != null)
      {
         LoggingUtil.log(MessageFormat.format(template, new Object[]{
               ReportFormatUtil.sanitizeClassName(className),
               result.getName()
            }));
      }
   }

   public boolean hasFailures()
   {
      return failureCount > 0 || errorCount > 0;
   }

   public String getXmlSummary()
   {
      StringBuilder builder = new StringBuilder();
      for(TestResult result : tests)
      {
         builder.append(result.getXmlSummary());
         builder.append('\n');
      }
      
      final String timestamp = DateUtils.format(new Date(), DateUtils.ISO8601_DATETIME_PATTERN);
      
      return MessageFormat.format(ReportFormatUtil.SUITE_XML_TEMPLATE, new Object[] { 
            new String(ReportFormatUtil.sanitizeClassName(className)), 
            new Integer(testCount),
            new Integer(failureCount),
            new Integer(errorCount), 
            new Integer(ignoreCount),
            ReportFormatUtil.formatTime(totalTime),
            getHostName(),
            timestamp,
            builder.toString()
         });
   }
   
   private String getHostName()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e)
      {
         return "localhost";
      }
   }
   
   public String getSummary()
   {
      return MessageFormat.format(ReportFormatUtil.SUITE_PLAIN_TEMPLATE, new Object[] { 
            new String(ReportFormatUtil.sanitizeClassName(className)), 
            new Integer(testCount),
            new Integer(failureCount),
            new Integer(errorCount), 
            new Integer(ignoreCount),
            ReportFormatUtil.formatTime(totalTime)
         });
   }
}
