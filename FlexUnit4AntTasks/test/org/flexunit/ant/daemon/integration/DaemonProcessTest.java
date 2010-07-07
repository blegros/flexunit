package org.flexunit.ant.daemon.integration;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;

public class DaemonProcessTest
{

   public static void main(String[] args)
   {
      try
      {
         LoggingUtil.VERBOSE = true;

         ExecutorService executor = Executors.newSingleThreadExecutor();
         Future<Object> future = executor.submit(new Daemon(
                  1025, 
                  64000, 
                  10000L, 
                  new File("/Users/dblegros/Documents/workspace/flexunit/FlexUnit4AntTasks/test/temp"
               )));
         
         Object result = future.get();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      LoggingUtil.log("ALL DONE!");
      System.exit(0);
   }

}
