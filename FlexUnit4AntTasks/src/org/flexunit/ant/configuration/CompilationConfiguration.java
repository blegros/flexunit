package org.flexunit.ant.configuration;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.types.LibraryPaths;
import org.flexunit.ant.types.SourcePaths;

//TODO: Create type to handle multiple flex-config files
public class CompilationConfiguration implements StepConfiguration
{
   private File flexConfig;
   private SourcePaths sources;
   private SourcePaths testSources;
   private LibraryPaths libraries;
   private File flexHome;
   private String player;
   private File workingDir;

   public CompilationConfiguration()
   {
      sources = new SourcePaths();
      testSources = new SourcePaths();
      libraries = new LibraryPaths();
   }
   
   public File getFlexHome()
   {
      return flexHome;
   }
   
   public void setFlexHome(File flexHome)
   {
      this.flexHome = flexHome;
   }
   
   public File getFlexConfig()
   {
      return flexConfig;
   }
   
   public void setFlexConfig(File path)
   {
      this.flexConfig = path;
   }

   public void addLibrary(FileSet fileset)
   {
      this.libraries.add(fileset);
   }
   
   public LibraryPaths getLibraries()
   {
      return libraries;
   }
   
   public String getPlayer()
   {
      return player;
   }
   
   public void setPlayer(String player)
   {
      this.player = player;
   }
   
   public void addSource(FileSet fileset)
   {
      this.sources.add(fileset);
   }
   
   public SourcePaths getSources()
   {
      return sources;
   }
   
   public void addTestSource(FileSet fileset)
   {
      this.testSources.add(fileset);
   }
   
   public SourcePaths getTestSources()
   {
      return testSources;
   }
   
   public void setWorkingDir(File workingDir)
   {
      this.workingDir = workingDir;
   }

   public File getWorkingDir()
   {
      return workingDir;
   }

   public void validate() throws BuildException
   {
      if(!testSources.exists())
      {
         throw new BuildException("One of the directories specified as a 'testSource' element does not exist.");
      }
      
      if(testSources.exists() && testSources.isEmpty())
      {
         throw new BuildException("No test files could be found for the 'testSource' elements.");
      }
      
      if(!libraries.exists())
      {
         throw new BuildException("One of the directories specified as a 'library' element does not exist.");
      }
      
      if(libraries.exists() && libraries.isEmpty())
      {
         throw new BuildException("No SWC files could be found for the provided 'library' elements.");
      }
      
      if(flexConfig != null)
      {
         if(!flexConfig.exists())
         {
            throw new BuildException("The provided path value [" + flexConfig.getAbsolutePath() + "] for the element 'flexConfig', does not exist.");
         }
         
         if(!flexConfig.isFile())
         {
            throw new BuildException("The provided path value [" + flexConfig.getAbsolutePath() + "] for the element 'flexConfig', is not a file.");
         }
      }
   }
   
   public void log()
   {
      LoggingUtil.log("Using the following settings for compilation:");
      LoggingUtil.log("\tFLEX_HOME: [" + flexHome.getAbsolutePath() + "]");
      LoggingUtil.log("\tplayer: [" + player + "]");
      if(flexConfig != null)
      {
         LoggingUtil.log("\tflexConfig: [" + flexConfig.getAbsolutePath() + "]");
      }
      LoggingUtil.log("\tsourceDirectories: [" + sources.getPathElements(",") + "]");
      LoggingUtil.log("\ttestSourceDirectories: [" + testSources.getPathElements(",") + "]");
      LoggingUtil.log("\tlibraries: [" + libraries.getPathElements(",") + "]");
   }
}
