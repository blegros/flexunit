package org.flexunit.ant.daemon.workers;

//TODO: add shared variable for spin lock and to hold report
//TODO: create coverage data DTO to expose line #'s for a classname
//TODO: create a coverage report that can write XML or HTML and be written to a specific location
public class CoverageWorker implements Worker, Runnable
{

   public boolean canProcess(byte[] message)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public byte[] process(byte[] message) throws WorkerException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void run()
   {
      // TODO Auto-generated method stub
      
   }

}
