package org.flexunit.ant.daemon.workers;

import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.WorkerUtil;

public class TestCompletionWorker implements Worker
{
   private static final String END_OF_RUN_NOTICE = "!EOR!";
   //private static final String END_OF_RUN_RECEIPT = "!EOR!";
   
   private Daemon server;

   public TestCompletionWorker(Daemon server)
   {
      this.server = server;
   }
   
   public boolean canProcess(byte[] message)
   {
      boolean accept = false;
      
      try
      {
         String endOfRun = WorkerUtil.readUtf8String(message);
         accept = END_OF_RUN_NOTICE.equals(endOfRun);
      }
      catch(Exception e)
      {
         accept = false;
      }
      
      return accept;
   }

   //tell the server it's done and return null to ignore any trailing bytes
   public byte[] process(byte[] message) throws WorkerException
   {
      server.done();
      return null;
   }
}
