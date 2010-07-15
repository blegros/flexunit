package org.flexunit.listeners
{
   import flash.utils.Dictionary;

   public class CoverageData
   {
      public static var empty : Boolean = true;
      public static const instance : CoverageData = new CoverageData();
      
      public static function add(filename : String, line : int) : void
      {
         empty = false;
         instance.touch(filename, line);
      }
      
      private var touched : Boolean;
      private var _touches : Dictionary;
      
      public function CoverageData()
      {
         touched = false;
         _touches = new Dictionary();
      }
      
      public function touch(filename : String, line : int) : void
      {
         var values : Array = touches[filename]; 
         if(!values)
         {
            values = new Array();
            touches[filename] = values;
         }
         
         values.push(line);
      }
      
      public function get touches() : Dictionary
      {
         return _touches;
      }
   }
}