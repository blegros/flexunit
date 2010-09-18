package org.flexunit.ant.daemon.workers;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.AMF3Marshaler;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.report.TestReport;
import org.flexunit.ant.report.TestReportException;
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

   // used by consumer
   private File reportDir;
   private Suites suites;
   private TestReport lastReport;

   public TestResultsWorker(Daemon server, File reportDir)
   {
      this.server = server;
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
      // don't start looking for tests until server is ready
      while (server.isOpen())
      {
         synchronized(queue)
         {
            try
            {
               queue.wait(10);
            }
            catch (InterruptedException ie)
            {
               LoggingUtil.log("TestResult thread done.");
               return;
            }
         }
      }

      //keep looping until server is done and queue is empty.  can't use condition here due to sync req
      server: while (true)
      {
         TestResult testResult = null;

         synchronized (queue)
         {
            while (queue.isEmpty())
            {
               if (server.isDone())
               {
                  // write out summary, stop server, and end
                  LoggingUtil.log(suites.getSummary(), true);
                  server.halt();
                  break server;
               }

               try
               {
                  // wait for more action
                  queue.wait(100);
               }
               catch (InterruptedException ie)
               {
                  break server;
               }
            }

            testResult = queue.remove(0);
         }

         Suite suite = findSuite(testResult);
         
         try
         {
            writeReport(suite);
         }
         catch (TestReportException re) {
            re.printStackTrace();
            server.halt();
            break server;
         }
      }

      LoggingUtil.log("TestResult thread done.");
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

   private void writeReport(Suite suite) throws TestReportException
   {
      // Write out report
      TestReport report = null;

      if (lastReport == null)
      {
         report = new TestReport(suite);
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
            report = new TestReport(suite);
         }
      }

      report.save(reportDir);
      
      // set current report as lastReport to reuse report's channel
      lastReport = report;
   }
}
