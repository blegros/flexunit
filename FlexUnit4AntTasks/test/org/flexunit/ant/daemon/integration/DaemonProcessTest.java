package org.flexunit.ant.daemon.integration;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;

public class DaemonProcessTest
{

   public static void main(String[] args)
   {
      try
      {
         LoggingUtil.VERBOSE = true;
         
         InetAddress address = InetAddress.getByName("127.0.0.1");
         ExecutorService executor = Executors.newSingleThreadExecutor();
         executor.execute(new Daemon(address, 1025));
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
}
