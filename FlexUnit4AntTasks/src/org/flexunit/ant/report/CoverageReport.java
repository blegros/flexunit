package org.flexunit.ant.report;

import java.io.File;
import java.util.List;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.reporting.ComplexityCalculator;
import net.sourceforge.cobertura.reporting.xml.XMLReport;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Source;

import org.apache.tools.ant.BuildException;

public class CoverageReport
{
   private File reportDir;
   private ProjectData coverageData;
   private List<String> sources;
   
   public CoverageReport(File reportDir, ProjectData coverageData, List<String> sources)
   {
      this.reportDir = reportDir;
      this.coverageData = coverageData;
      this.sources = sources;
   }
   
   public void save()
   {
      //configure report resources
      FileFinder finder = new FileFinder()
      {
          public Source getSource( String fileName )
          {
              Source source = super.getSource( fileName.replace( ".java", ".as" ) );
              if ( source == null )
              {
                  source = super.getSource( fileName.replace( ".java", ".mxml" ) );
              }
              return source;
          }
      };

      for ( String dir : sources )
      {
          finder.addSourceDirectory(dir);
      }

      ComplexityCalculator complexity = new ComplexityCalculator(finder);
      
      //generate report
      try
      {
         new XMLReport(coverageData, reportDir, finder, complexity);
      }
      catch ( Exception e )
      {
          throw new BuildException( "Unable to write coverage report...", e );
      }
   }
}
