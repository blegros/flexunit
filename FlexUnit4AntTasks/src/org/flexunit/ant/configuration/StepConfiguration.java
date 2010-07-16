package org.flexunit.ant.configuration;

import org.apache.tools.ant.BuildException;

public interface StepConfiguration
{
   public void validate() throws BuildException;
   public void log();
}
