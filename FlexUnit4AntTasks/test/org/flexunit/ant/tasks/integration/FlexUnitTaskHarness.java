package org.flexunit.ant.tasks.integration;

import java.io.File;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.flexunit.ant.tasks.FlexUnitTask;

public class FlexUnitTaskHarness extends TestCase
{
   private FlexUnitTask fixture;
   
   protected void setUp()
   {
      fixture = new FlexUnitTask();
      Project project = new Project();
      project.setProperty("ant.home", "/usr/share/java/ant-1.7.1");
      project.setProperty("FLEX_HOME", "/Users/dblegros/api/flexsdk/4.1.0");
      fixture.setProject(project);
      
      //call all setters for task attributes
      fixture.setHaltonfailure(true);
      fixture.setLocalTrusted(true);
      fixture.setPort(1024);
      fixture.setTimeout(10000);
      fixture.setBuffer(555555);
      //fixture.setSWF("test/TestRunner.swf");
      fixture.setToDir("test/target");
      fixture.setVerbose(true);
      fixture.setFailureproperty("failedtests");
      //fixture.setPlayer("flash");
      //fixture.setCommand("/Applications/Safari.app/Contents/MacOS/Safari");
      //fixture.setHeadless(false);
      fixture.setWorkingDir("test/sandbox");
      //Property configPath = new Property();
      //configPath.setValue("/Users/dblegros/api/flexsdk/4.0.0/frameworks/flex-config.xml");
      //fixture.addConfiguredFlexConfig(configPath);
      fixture.setCoverage(true);
      
      //Call elements next
      FileSet sourceFileSet = new FileSet();
      sourceFileSet.setDir(new File("test/sandbox/src/"));
      sourceFileSet.setIncludes("**/*.as");
      fixture.addSource(sourceFileSet);
      
      FileSet testSourceFileSet = new FileSet();
      testSourceFileSet.setDir(new File("test/sandbox/test"));
      testSourceFileSet.setIncludes("**/*class.as");
      fixture.addTestSource(testSourceFileSet);
      
      FileSet libraryFileSet = new FileSet();
      libraryFileSet.setDir(new File("test/sandbox/libs"));
      fixture.addLibrary(libraryFileSet);
   }

   public void testExecute()
   {
      fixture.execute();
   }

}
