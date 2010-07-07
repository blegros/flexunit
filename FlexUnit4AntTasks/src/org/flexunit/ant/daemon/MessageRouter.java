package org.flexunit.ant.daemon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.daemon.workers.FlashPlayerTrustWorker;
import org.flexunit.ant.daemon.workers.HandshakeWorker;
import org.flexunit.ant.daemon.workers.HeartbeatWorker;
import org.flexunit.ant.daemon.workers.TestCompletionWorker;
import org.flexunit.ant.daemon.workers.TestResultsWorker;
import org.flexunit.ant.daemon.workers.Worker;
import org.flexunit.ant.daemon.workers.WorkerException;


public class MessageRouter
{
   private static final int WORKER_THREAD_POOL_SIZE = 3;
   
   private List<Worker> pipeline;
   private ExecutorService executor;
   
   //shared barrier lock to allow receipt of results and writing of reports to occur in different threads
   private CyclicBarrier barrier;
   
   //used as an internal buffer for routed message leftovers
   byte[] leftOver = null;
   
   public MessageRouter(Daemon server, long timeout, File reportDir)
   {
      barrier = new CyclicBarrier(2);
      
      pipeline = new ArrayList<Worker>();
      pipeline.add(new FlashPlayerTrustWorker(server));
      pipeline.add(new HandshakeWorker(server));
      pipeline.add(new HeartbeatWorker(server, timeout));
      pipeline.add(new TestResultsWorker(server, reportDir, barrier));
      pipeline.add(new TestCompletionWorker(server, barrier));
      
      activateThreadedWorkers();
   }
   
   private void activateThreadedWorkers()
   {
      executor = Executors.newFixedThreadPool(WORKER_THREAD_POOL_SIZE);
      
      for(Worker worker : pipeline)
      {
         if(worker instanceof Runnable)
         {
            executor.execute((Runnable) worker);
         }
      }
   }
   
   public void shutdown()
   {
      executor.shutdownNow();    //need to do since we're using infinite loops for each thread
      while(!executor.isTerminated()){}
   }

   public void route(byte[] message) throws WorkerException
   {
      boolean moreToRead = true;
      
      leftOver = WorkerUtil.append(leftOver, message);
      
      while(moreToRead)
      {
         boolean anyoneInterested = false;
         
         for(Worker worker : pipeline)
         {
            //assumes only one worker is interested in each message type
            if(worker.canProcess(leftOver))
            {
               LoggingUtil.log("Routing message to " + worker.getClass().getCanonicalName() + " ...");
               
               byte[] remaining = null;
               
               try
               {
                  remaining = worker.process(leftOver);
               }
               catch(WorkerException we)
               {
                  shutdown();
                  throw new WorkerException(we);
               }
               
               if(remaining == null || remaining.length == 0)
               {
                  leftOver = null;
                  moreToRead = false;     //a worker did something but no bytes were left over, done routing
               }
               else
               {
                  leftOver = remaining;   //still some bytes left over from work, reset what's left
               }
               
               anyoneInterested = true;   //a worker did something and there were bytes left over, keep going
               
               break;   //pop-out of the worker loop since only one worker cares about a message at a time
            }
         }
         
         //if no one's interested the leftOver's must be at a fragment and need more data, so pop-out
         if(!anyoneInterested)
         {
            moreToRead = false;
         }
      }
   }
}
