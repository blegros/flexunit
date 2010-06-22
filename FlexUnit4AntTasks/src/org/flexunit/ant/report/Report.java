package org.flexunit.ant.report;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.apache.tools.ant.BuildException;

public class Report
{
   private static final String FILENAME_PREFIX = "TEST-";
   private static final String FILENAME_EXTENSION = ".xml";

   private Suite suite;
   private FileChannel channel;
   
   public Report(Suite suite)
   {
      this.suite = suite;
   }

   /**
    * Write the report out to file
    * 
    * @param reportDir
    *           Directory to hold report file.
    */
   public void save(File reportDir) throws BuildException
   {
      try
      {
         if(channel == null)
         {
            // Open the file matching the parameter suite
            final File file = new File(reportDir, FILENAME_PREFIX + suite + FILENAME_EXTENSION);
            final FileOutputStream fos = new FileOutputStream(file);
            channel = fos.getChannel();
         }
      }
      catch (Exception e)
      {
         throw new BuildException("Error saving report.", e);
      }
   }
}
