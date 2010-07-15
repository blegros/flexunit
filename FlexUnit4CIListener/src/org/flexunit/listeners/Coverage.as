package org.flexunit.listeners
{
   public class Coverage
   {
      public static function onSample(file : String, line : int) : void
      {
         CoverageData.add(file, line);
      }
   }
}