package org.flexunit.ant.daemon.workers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.AMF3Marshaler;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.report.Report;
import org.flexunit.ant.report.Suite;
import org.flexunit.ant.report.Suites;
import org.flexunit.ant.report.TestResult;

public class TestResultsWorker implements Worker, Runnable
{
   // used by producer
   private Daemon server;
   private AMF3Marshaler marshaler;

   // shared
   private List<TestResult> queue;
   private CyclicBarrier barrier;

   // used by consumer
   private File reportDir;
   private Suites suites;
   private Report lastReport;

   public TestResultsWorker(Daemon server, File reportDir, CyclicBarrier barrier)
   {
      this.server = server;
      this.barrier = barrier;
      this.reportDir = reportDir;
      
      this.marshaler = new AMF3Marshaler();
      this.queue = new LinkedList<TestResult>();
      this.suites = new Suites();
   }

   public boolean canProcess(byte[] message)
   {
      try
      {
         // TODO: Redo message format to send AMF byte length before message, so
         // we can do a quick check instead of unmarshal
         Object actual = marshaler.unmarshal(message);
         return actual instanceof TestResult;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public byte[] process(byte[] message) throws WorkerException
   {
      try
      {
         TestResult object = (TestResult) marshaler.unmarshal(message);

         synchronized (queue)
         {
            queue.add((TestResult) object);
            queue.notify();
         }
      }
      catch (Exception e)
      {
         throw new WorkerException("Could not unmarshal AMF object when was worker approved...", e);
      }

      // send back bytes we didn't use
      int lastPosition = marshaler.getLastPosition();
      if (lastPosition != -1)
      {
         try
         {
            return WorkerUtil.slice(message, lastPosition);
         }
         catch (Exception e)
         {
            throw new WorkerException("Slicing testresult from input stream failed...", e);
         }

      }

      return null;
   }

   public void run()
   {
      //TODO: Need to use check condition to kill loop rather than infinite loop
      while (!server.shuttingDown())
      {
         if (server.isReady()) // can I start checking for queued results?
         {
            TestResult testResult = null;

            synchronized (queue)
            {
               while (queue.isEmpty())
               {
                  try
                  {
                     //has an end of run been reached?
                     if(barrier.getNumberWaiting() == 1)
                     {
                        //if a end of run has been sent, it's waiting on me; let it know I'm done
                        try
                        {
                           LoggingUtil.log(suites.getSummary(), true);
                           barrier.await();
                        }
                        catch(BrokenBarrierException e)
                        {
                           LoggingUtil.log("TestResult worker done due to barrier issue.");
                           return;
                        }
                        catch(InterruptedException ie)
                        {
                           LoggingUtil.log("TestResult worker done.");
                           return;
                        }
                     }
                     else
                     {
                        //wait for more action
                        queue.wait();
                     }
                  }
                  catch (InterruptedException ie)
                  {
                     LoggingUtil.log("TestResult worker done.");
                     return;
                  }
               }

               testResult = queue.remove(0);
            }

            if (testResult != null)
            {
               Suite suite = findSuite(testResult);
               writeReport(suite);
            }
         }
      }
      
      LoggingUtil.log("TestResult worker done.");
   }

   private Suite findSuite(TestResult testResult)
   {
      // Find suite to add test to
      Suite suite = null;
      if (!suites.containsKey(testResult.getClassName()))
      {
         suite = new Suite(testResult.getClassName());
         suites.put(suite.getClassName(), suite);
      }
      else
      {
         suite = suites.get(testResult.getClassName());
      }

      suite.addTest(testResult);

      return suite;
   }

   private void writeReport(Suite suite)
   {
      // Write out report
      Report report = null;

      if (lastReport == null)
      {
         report = new Report(suite);
      }
      else
      {
         // do we need to switch reports?
         if (lastReport.getClassName().equals(suite.getClassName()))
         {
            report = lastReport;
         }
         else
         {
            report = new Report(suite);
         }
      }

      report.save(reportDir);

      lastReport = report; // set current report as lastReport to reuse report's
                           // channel
   }
}
