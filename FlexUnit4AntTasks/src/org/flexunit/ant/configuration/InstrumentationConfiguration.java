package org.flexunit.ant.configuration;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.types.SourcePaths;

public class InstrumentationConfiguration implements StepConfiguration
{
   private File workingDir;
   private SourcePaths sources;
   private File swf;
   
   public InstrumentationConfiguration()
   {
      this.sources = new SourcePaths();
   }
   
   public File getWorkingDir()
   {
      return workingDir;
   }

   public void setWorkingDir(File workingDir)
   {
      this.workingDir = workingDir;
   }

   public void addSource(FileSet fileset)
   {
      this.sources.add(fileset);
   }
   
   public SourcePaths getSources()
   {
      return sources;
   }

   public File getSwf()
   {
      return swf;
   }

   public void setSwf(File swf)
   {
      this.swf = swf;
   }

   public void log()
   {
      LoggingUtil.log("Using the following settings for compilation:");
      LoggingUtil.log("\tsourceDirectories: [" + sources.getPathElements(",") + "]");
      LoggingUtil.log("\tswf: [" + swf.getAbsolutePath() + "]");
      LoggingUtil.log("\tworkingDir: [" + workingDir.getAbsolutePath() + "]");
   }

   public void validate() throws BuildException
   {
      //sourcePaths exist
      if(!sources.exists())
      {
         throw new BuildException("One of the directories specified as a 'source' element does not exist.");
      }
      
      //sourcePaths not provided or contain no files
      if(sources.exists() && sources.isEmpty())
      {
         throw new BuildException("No source files could be found for the 'source' elements.");
      }
      
   }
}
