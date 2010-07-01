package org.flexunit.ant.daemon.workers;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.AMF3Marshaler;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.report.TestResult;

public class TestResultsWorker implements Worker, Runnable
{
   private Daemon server;
   private AMF3Marshaler marshaler;

   private List<TestResult> queue;

   public TestResultsWorker(Daemon server)
   {
      this.server = server;
      this.marshaler = new AMF3Marshaler();
      this.queue = new LinkedList<TestResult>();
   }

   public boolean canProcess(byte[] message)
   {
      try
      {
         String actual = marshaler.getClassName(message);
         return  TestResult.class.getCanonicalName().equals(actual);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return false;
      }
   }

   public byte[] process(byte[] message)
   {
      try
      {
         TestResult object = (TestResult) marshaler.unmarshal(message);

         synchronized (queue)
         {
            LoggingUtil.log("Queuing test [" + ((TestResult)object).getName() + "] and waking up queue...");
            queue.add((TestResult) object);
            queue.notify();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      int lastPosition = marshaler.getLastPosition();
      if(lastPosition != -1)
      {
         try
         {
            return WorkerUtil.slice(message, lastPosition);
         }
         catch(Exception e)
         {
            return null;
         }
         
      }
      
      return null;
   }

   public void run()
   {
      TestResult testResult = null;

      while (true)   //keep the thread alive
      {
         if(server.isReady()) //can I start checking for queued results?
         {
            synchronized (queue)
            {
               while (queue.isEmpty())
               {
                  try
                  {
                     queue.wait();
                  }
                  catch (InterruptedException e)
                  {
                  }
               }

               testResult = queue.remove(0);
               // TODO: Delegate to something building reports
            }
         }
      }
   }

}
