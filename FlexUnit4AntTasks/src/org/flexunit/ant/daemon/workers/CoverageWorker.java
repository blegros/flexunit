package org.flexunit.ant.daemon.workers;

import java.io.File;
import java.util.Map;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.helpers.AMF3Marshaler;
import org.flexunit.ant.daemon.helpers.WorkerUtil;
import org.flexunit.ant.report.CoverageReport;

public class CoverageWorker implements Worker, Runnable
{
   private final String COBERTURA_METADATA_REPORT = "cobertura.ser";
   
   private AMF3Marshaler marshaler;
   private File reportDir;
   
   private ProjectData coberturaData;
   private Map<String, Integer> actuals;
   private Boolean available = false; // needs to be thread safe
   
   public CoverageWorker(File workingDir, File reportDir)
   {
      this.marshaler = new AMF3Marshaler();
      this.reportDir = reportDir;
      
      File coberturaMetadata = new File(workingDir, COBERTURA_METADATA_REPORT);
      if(coberturaMetadata.exists())
      {
         this.coberturaData = CoverageDataFileHandler.loadCoverageData(coberturaMetadata);
      }
   }

   @SuppressWarnings("unchecked")
   public boolean canProcess(byte[] message)
   {
      try
      {
         // TODO: Redo message format to send AMF byte length before message, so
         // we can do a quick check instead of unmarshal
         Object actual = marshaler.unmarshal(message);
         return actual instanceof Map;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   @SuppressWarnings("unchecked")
   public byte[] process(byte[] message) throws WorkerException
   {
      try
      {
         Map<String, Integer> data = (Map<String, Integer>) marshaler.unmarshal(message);

         synchronized (available)
         {
            this.actuals = data;
            
            available = true;
            available.notify();
         }
      }
      catch (Exception e)
      {
         throw new WorkerException("Could not unmarshal AMF object when was worker approved...", e);
      }
      
      // send back bytes we didn't use
      int lastPosition = marshaler.getLastPosition();
      if (lastPosition != -1)
      {
         try
         {
            return WorkerUtil.slice(message, lastPosition);
         }
         catch (Exception e)
         {
            throw new WorkerException("Slicing coveragedata from input stream failed...", e);
         }
      }
      
      return null;
   }

   public void run()
   {
      //was the coverage data available?
      if(coberturaData == null)
      {
         LoggingUtil.log("No coverage data found...");
         return;
      }
      
      // don't start looking for coverage data until data is available
      synchronized(available)
      {
         while (!available)
         {
            try
            {
               available.wait(10);
            }
            catch (InterruptedException ie)
            {
               LoggingUtil.log("CoverageWorker thread done.");
               return;
            }
         }
      }
      
      //now that data is available, update coberturaData with touches
      recordTouches();
      
      //generate report with finalized raw coverage data 
      CoverageReport report = new CoverageReport(reportDir, coberturaData, null);
      report.save();
   }
   
   private void recordTouches()
   {
      //TODO: Process all touch data onto project data
   }
}
