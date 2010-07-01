package org.flexunit.ant.daemon.workers;

import org.apache.tools.ant.BuildException;
import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.WorkerUtil;

public class HeartbeatWorker implements Worker, Runnable
{
   private static final String HEARTBEAT_REQUEST = "tha";      //sent by worker
   private static final String HEARTBEAT_RESPONSE = "thump";   //received by worker
   private static final long TIMEOUT = 5000;                   //approx timeout period to wait until failure
   private static final int RETRY_LIMIT = 3;                   //used as a divisor to determine timeout if heartbeat hiccups

   private Daemon server;
   private Boolean clientAlive = false;   //needs to be thread safe
   private int retries = 0;

   public HeartbeatWorker(Daemon server)
   {
      this.server = server;
   }

   public boolean canProcess(byte[] message)
   {
      boolean accept = false;

      synchronized (clientAlive)
      {
         //if client isn't alive, check to see if it's telling me it is
         if (!clientAlive)
         {
            String keepAlive = WorkerUtil.readUtf8String(message);
            accept = keepAlive.equals(HEARTBEAT_RESPONSE);
         }
      }

      return accept;
   }

   public byte[] process(byte[] message)
   {
      //got pass verification, so record the client is alive for thread
      synchronized (clientAlive)
      {
         clientAlive = true;
      }

      try
      {
         return WorkerUtil.sliceUtf(HEARTBEAT_RESPONSE, message);
      }
      catch(Exception e)
      {
         return null;
      }
   }

   public void run()
   {
      //calculate timeout based on # of retry periods
      long retryTimeout = TIMEOUT / RETRY_LIMIT;
      
      while (true)
      {
         if (server.isReady())
         {
            LoggingUtil.log("Sending heartbeat.");
            
            // send a request asking if the client is still alive
            byte[] response = WorkerUtil.toUtf8Bytes(HEARTBEAT_REQUEST);
            server.send(response);

            synchronized (clientAlive)
            {
               try
               {
                  //give the client a chance to respond
                  clientAlive.wait(retryTimeout);
               }
               catch (InterruptedException ie)
               {
               }
               
               //after waiting, did the client say it was alive?
               if (!clientAlive)
               {
                  //if not, then increment retries and give another shot
                  if(retries < RETRY_LIMIT)
                  {
                     LoggingUtil.log("Heartbeat retry attempt [" + (retries + 1) + " of " + RETRY_LIMIT + "]");
                     retries ++;
                  }
                  else
                  {
                     //otherwise, kick out because client isn't reporting
                     throw new BuildException("This task timed out waiting for the Flash movie to respond.");
                  }
               }

               // reset client's alive status and ask again
               clientAlive = false;
            }
         }
      }
   }

}
