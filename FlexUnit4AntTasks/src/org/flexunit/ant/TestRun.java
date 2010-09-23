package org.flexunit.ant;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.flexunit.ant.configuration.TestRunConfiguration;
import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.launcher.commands.player.PlayerCommand;
import org.flexunit.ant.launcher.commands.player.PlayerCommandFactory;
import org.flexunit.ant.launcher.contexts.ExecutionContext;
import org.flexunit.ant.launcher.contexts.ExecutionContextFactory;

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

         // run the execution context and player
         PlayerCommand player = obtainPlayer();
         ExecutionContext context = obtainContext(player);
         
         //start the execution context
         context.start();
         
         //launch the player
         Process process = player.launch();

         // block until daemon is completely done with all test data
         future.get();

         //stop the execution context now that socket thread is done
         context.stop(process);
      } catch (Exception e)
      {
         throw new BuildException(e);
      }
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
            configuration.getWorkingDir(),
            configuration.getReportDir()
         );

      // Get handle to service to run object in thread.
      ExecutorService executor = Executors.newSingleThreadExecutor();

      // Run object in thread and return Future.
      return executor.submit(server);
   }
   
   /**
    * Fetch the player command to execute the SWF.
    * 
    * @return PlayerCommand based on user config
    */
   protected PlayerCommand obtainPlayer()
   {
      // get command from factory
      PlayerCommand command = PlayerCommandFactory.createPlayer(
            configuration.getOs(), 
            configuration.getPlayer(), 
            configuration.getCommand(), 
            configuration.isLocalTrusted());
      
      command.setProject(project);
      command.setSwf(configuration.getSwf());
      
      return command;
   }
   
   /**
    * 
    * @param player PlayerCommand which should be executed
    * @return Context to wrap the execution of the PlayerCommand
    */
   protected ExecutionContext obtainContext(PlayerCommand player)
   {
      ExecutionContext context = ExecutionContextFactory.createContext(
            configuration.getOs(), 
            configuration.isHeadless(), 
            configuration.getDisplay());
      
      context.setProject(project);
      context.setCommand(player);
      
      return context;
   }
}
