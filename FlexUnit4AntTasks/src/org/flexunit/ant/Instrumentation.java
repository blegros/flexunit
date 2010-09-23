package org.flexunit.ant;

import java.io.File;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.flexunit.ant.configuration.InstrumentationConfiguration;
import org.flexunit.ant.report.ApparatUtil;

import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;
import apparat.tools.coverage.CoverageConfiguration;
import apparat.tools.coverage.CoverageObserver;
import apparat.tools.coverage.Coverage.CoverageTool;

public class Instrumentation
{
   private final String INSTRUMENTED_SWF_FILENAME = "InstrumentedTestRunner.swf";
   private final String COBERTURA_METADATA_REPORT = "cobertura.ser";
   
   private InstrumentationConfiguration configuration;
   private ProjectData coberturaData;
   
   public Instrumentation(InstrumentationConfiguration configuration)
   {
      this.configuration = configuration;
      this.coberturaData = new ProjectData();
   }
   
   public ProjectData getInstrumentationData()
   {
      return coberturaData;
   }
   
   public File instrument()
   {
      File reInstrumentedSwf = instrumentSwf();
      generateMetadataReport();
      return reInstrumentedSwf;
   }
   
   private File instrumentSwf()
   {
      LoggingUtil.log("Instrumenting swf: [" + configuration.getSwf().getAbsolutePath() + "]");
      
      //configure apparat coverage tool
      File metadata = new File(configuration.getWorkingDir(), INSTRUMENTED_SWF_FILENAME);
      CoverageConfiguration coverageConfiguration = new CoverageConfigurationImpl(configuration.getSwf(), metadata, configuration.getSources().getPathElements());
      
      CoverageTool utility = new CoverageTool();
      utility.configure(coverageConfiguration);
      
      CoverageObserver observer = new CoverageObserver() {
            public void instrument( String file, int line )
            {
               String classname = ApparatUtil.toClassname(file);
               LoggingUtil.log( "Instrumenting " + classname + ":" + line );
                 
               ClassData classData = coberturaData.getOrCreateClassData(classname);
               classData.addLine( line, null, null );
            }
         };
      utility.addObserver(observer);

      //DO it!
      utility.run();
      
      //return the new SWF
      return metadata;
   }
   
   private File generateMetadataReport()
   {
      //generate metadata file
      File metadataReport = new File(configuration.getWorkingDir(), COBERTURA_METADATA_REPORT);
      CoverageDataFileHandler.saveCoverageData(coberturaData,  metadataReport);
      
      return metadataReport;
   }

   /**
    * Based on the FlexMojos 4.x org.sonatype.flexmojos.coverage::AbstractCoverageReport class implementation
    */
   private final class CoverageConfigurationImpl implements CoverageConfiguration
   {
      private final ListBuffer<String> _sourcePath = new ListBuffer<String>();
      private final File _input;
      private final File _output;

      public CoverageConfigurationImpl(final File input, final File output, final java.util.List<String> sourcePath)
      {
         _input = input;
         _output = output;

         for(final String path : sourcePath)
         {
            // Java equivalent of the following Scala code: _sourcePath += PathUtil getCanonicalPath sourcePathElement
            _sourcePath.$plus$eq(path);
         }
      }

      public File input()
      {
         return _input;
      }

      public File output()
      {
         return _output;
      }

      public List<String> sourcePath()
      {
         // Convert the mutable ListBuffer[String] to an immutable List[String] for Apparat.
         return _sourcePath.toList();
      }
   }
}
