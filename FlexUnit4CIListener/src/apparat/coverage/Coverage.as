package apparat.coverage
{
   import org.flexunit.listeners.CoverageData;

   public class Coverage
   {
      public static function onSample(file : String, line : int) : void
      {
         CoverageData.add(file, line);
      }
   }
}