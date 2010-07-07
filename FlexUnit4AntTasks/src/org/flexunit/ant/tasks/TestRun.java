package org.flexunit.ant.tasks;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.launcher.FlexUnitLauncher;
import org.flexunit.ant.tasks.configuration.TestRunConfiguration;

public class TestRun
{
   private TestRunConfiguration configuration;
   private Project project;
   
   public TestRun(Project project, TestRunConfiguration configuration)
   {
      this.project = project;
      this.configuration = configuration;
   }
   
   public void run() throws BuildException
   {
      configuration.log();
      
      try
      {
         // setup callable thread
         Future<Object> future = setupDaemon();

         // launch FlashPlayer and test SWF
         Process player = launchTestSuite();

         // block until thread is completely done with all tests
         future.get();

         // kill the player if using a custom command
         if (configuration.isCustomCommand())
         {
            player.destroy();
         }
      } catch (Exception e)
      {
         throw new BuildException(e);
      }
   }
   
   /**
    * Create and launch the swf player in the appropriate domain
    */
   protected Process launchTestSuite()
   {
      Process process = null;
      final FlexUnitLauncher browser = new FlexUnitLauncher(project,
            configuration.isLocalTrusted(), configuration.isHeadless(),
            configuration.getDisplay(), configuration.getPlayer(),
            configuration.getCommand());

      try
      {
         process = browser.runTests(configuration.getSwf());
      } catch (Exception e)
      {
         throw new BuildException("Error launching the test runner.", e);
      }

      return process;
   }

   /**
    * Create a server socket for receiving the test reports from FlexUnit. We
    * read and write the test reports inside of a Thread.
    */
   protected Future<Object> setupDaemon() throws IOException
   {
      LoggingUtil.log("Setting up daemon process ...");

      // Create server for use by thread
      Daemon server = new Daemon(
            configuration.getPort(), 
            configuration.getServerBufferSize(), 
            configuration.getSocketTimeout(), 
            configuration.getReportDir()
         );

      // Get handle to service to run object in thread.
      ExecutorService executor = Executors.newSingleThreadExecutor();

      // Run object in thread and return Future.
      return executor.submit(server);
   }
}
