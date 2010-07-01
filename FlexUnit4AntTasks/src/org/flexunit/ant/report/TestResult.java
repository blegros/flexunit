package org.flexunit.ant.report;

import java.text.MessageFormat;

public class TestResult
{
   private static final String SUCCESS = "success";
   private static final String FAILURE = "failure";
   private static final String ERROR = "error";
   private static final String IGNORE = "ignore";
   
   private String className;
   private String name;
   private String status;
   private long time;
   private String failureStackTrace;
   private String failureMessage;
   private String failureType;
   
   public String getClassName()
   {
      return className;
   }
   
   public void setClassName(String className)
   {
      this.className = className;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public void setStatus(String status)
   {
      this.status = status;
   }
   
   public boolean isSuccess()
   {
      return status.equals(SUCCESS);
   }
   
   public boolean isFailure()
   {
      return status.equals(FAILURE);
   }
   
   public boolean isError()
   {
      return status.equals(ERROR);  
   }
   
   public boolean isIgnored()
   {
      return status.equals(IGNORE);
   }
   
   public long getTime()
   {
      return time;
   }
   
   public void setTime(long time)
   {
      this.time = time;
   }
   
   public String getFailureStackTrace()
   {
      return failureStackTrace;
   }
   
   public void setFailureStackTrace(String failureStackTrace)
   {
      this.failureStackTrace = failureStackTrace;
   }
   
   public String getFailureMessage()
   {
      return failureMessage;
   }
   
   public void setFailureMessage(String failureMessage)
   {
      this.failureMessage = failureMessage;
   }
   
   public String getFailureType()
   {
      return failureType;
   }
   
   public void setFailureType(String failureType)
   {
      this.failureType = failureType;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((className == null) ? 0 : className.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)                    return true;
      if (obj == null)                    return false;
      if (getClass() != obj.getClass())   return false;
      
      TestResult other = (TestResult) obj;
      
      if (className == null && other.getClassName() != null)
      {
         return false;
      }
      
      if (!className.equals(other.className))
      {
         return false;
      }
      
      if (name == null && other.name != null)
      {
         return false;
      } 
      else if (!name.equals(other.name))
      {
         return false;
      }
      
      return true;
   }
   
   public String getXmlSummary()
   {
      String template = null;
      Object[] parameters = null;
      
      if(isSuccess())
      {
         template = ReportFormatUtil.TEST_XML_SUCCESS_TEMPLATE;
         parameters = new Object[] { 
               className,
               name,
               ReportFormatUtil.formatTime(time)
            };
      }
      else if(isFailure())
      {
         template = ReportFormatUtil.TEST_XML_FAILURE_TEMPLATE;
         parameters = new Object[] { 
               className,
               name,
               ReportFormatUtil.formatTime(time),
               failureMessage,
               failureType,
               failureStackTrace
            };
      }
      else if(isError())
      {
         template = ReportFormatUtil.TEST_XML_ERROR_TEMPLATE;
         parameters = new Object[] { 
               className,
               name,
               ReportFormatUtil.formatTime(time),
               failureMessage,
               failureType,
               failureStackTrace
            };
      }
      else if(isIgnored())
      {
         template = ReportFormatUtil.TEST_XML_IGNORED_TEMPLATE;
         parameters = new Object[] { 
               className,
               name,
               ReportFormatUtil.formatTime(time)
            };
      }
      else
      {
         template = "XML GEN FAIL: [" + className + ", " + name + "]"; 
      }
      
      return MessageFormat.format(template, parameters);
   }
}
