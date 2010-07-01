package org.flexunit.ant.daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.daemon.workers.FlashPlayerTrustWorker;
import org.flexunit.ant.daemon.workers.HandshakeWorker;
import org.flexunit.ant.daemon.workers.HeartbeatWorker;
import org.flexunit.ant.daemon.workers.TestResultsWorker;
import org.flexunit.ant.daemon.workers.Worker;


public class MessageRouter
{
   private static final int WORKER_THREAD_POOL_SIZE = 2;
   
   private List<Worker> workers;
   private ExecutorService executor;
   byte[] leftOver = null;   //used as an internal buffer for routed message leftovers
   
   public MessageRouter(Daemon server)
   {
      workers = new ArrayList<Worker>();
      workers.add(new FlashPlayerTrustWorker(server));
      workers.add(new HandshakeWorker(server));
      workers.add(new HeartbeatWorker(server));
      workers.add(new TestResultsWorker(server));
      
      executor = Executors.newFixedThreadPool(WORKER_THREAD_POOL_SIZE);
      
      activateThreadedWorkers();
   }
   
   //TODO: Figure out way to kill threads when exception is thrown or server has been shutdown
   private void activateThreadedWorkers()
   {
      for(Worker worker : workers)
      {
         if(worker instanceof Runnable)
         {
            executor.execute((Runnable) worker);
         }
      }
   }

   public void route(byte[] message)
   {
      boolean moreToRead = true;
      
      leftOver = WorkerUtil.append(leftOver, message);
      
      while(moreToRead)
      {
         boolean anyoneInterested = false;
         
         for(Worker worker : workers)
         {
            //assumes only one worker is interested in each message type
            if(worker.canProcess(leftOver))
            {
               LoggingUtil.log("Routing message to " + worker.getClass().getCanonicalName() + " ...");
               
               byte[] remaining = worker.process(leftOver);
               
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
