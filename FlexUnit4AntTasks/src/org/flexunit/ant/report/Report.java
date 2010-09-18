package org.flexunit.ant.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
   
   public String getClassName()
   {
      return suite.getClassName();
   }

   /**
    * Write the report out to file
    * 
    * @param reportDir
    *           Directory to hold report file.
    */
   public void save(File reportDir) throws ReportException
   {
      try
      {
         if(channel == null)
         {
            // Open the file matching the parameter suite
            final String safeClassName = ReportFormatUtil.sanitizeClassName(suite.getClassName());
            final File file = new File(reportDir, FILENAME_PREFIX + safeClassName  + FILENAME_EXTENSION);
            final FileOutputStream fos = new FileOutputStream(file);
            channel = fos.getChannel();
         }
         
         final ByteBuffer content = ByteBuffer.wrap(suite.getXmlSummary().getBytes());
         channel.position(0); //rewind file pointer
         channel.write(content);
         
         //don't close until the report is GC'd for faster report writing (see #finalize)
      }
      catch (Exception e)
      {
         throw new ReportException("Error saving report.", e);
      }
   }
   
   @Override
   protected void finalize()
   {
      try
      {
         channel.close();
      }
      catch(IOException ioe)
      {
         ioe.printStackTrace();
      }
   }
}
