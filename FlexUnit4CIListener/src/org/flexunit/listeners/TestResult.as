package org.flexunit.listeners
{
   [RemoteClass(alias="org.flexunit.ant.report.TestResult")]
   public class TestResult
   {
      public static const SUCCESS : String = "success";
      public static const FAILURE : String = "failure";
      public static const ERROR : String = "error";
      public static const IGNORE : String = "ignore";
      
      public var className : String;
      public var name : String;
      public var status : String;
      public var time : Number;
      public var failureStackTrace : String;
      public var failureMessage : String;
      public var failureType : String;
      
      public function TestResult()
      {
      }
   }
}