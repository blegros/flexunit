package org.flexunit.ant.launcher.commands.player;

import java.io.File;
import java.io.IOException;

import org.flexunit.ant.launcher.commands.Command;
import org.flexunit.ant.launcher.platforms.PlatformDefaults;

public abstract class DefaultPlayerCommand extends Command implements PlayerCommand
{
   private File swf;
   private PlatformDefaults defaults;
   
   public DefaultPlayerCommand()
   {
      super();
   }

   public void setSwf(File swf)
   {
      this.swf = swf;
   }

   public File getSwf()
   {
      return swf;
   }

   public void setDefaults(PlatformDefaults defaults)
   {
      this.defaults = defaults;
   }

   public PlatformDefaults getDefaults()
   {
      return defaults;
   }
   
   public abstract File getFileToExecute();
   
   public abstract void prepare();
   
   @Override
   public Process launch() throws IOException
   {
      return super.launch();
   }
}
