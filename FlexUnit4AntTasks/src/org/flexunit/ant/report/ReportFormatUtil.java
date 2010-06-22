package org.flexunit.ant.report;

public class ReportFormatUtil
{
   //Output templates
   public static String TEST_XML_SUCCESS_TEMPLATE;
   public static String TEST_XML_FAILURE_TEMPLATE;
   public static String TEST_XML_ERROR_TEMPLATE;
   public static String TEST_XML_IGNORED_TEMPLATE;
   public static final String SUITE_PLAIN_TEMPLATE = "Suite: {0}\nTests run: {1}, Failures: {2}, Errors: {3}, Skipped: {4}, Time elapsed: {5} sec";
   public static final String TEST_RUN_PLAIN_TEMPLATE = "Tests run: {0}, Failures: {1}, Errors: {2}, Skipped: {3}, Time elapsed: {4} sec";
   public static String SUITE_XML_TEMPLATE;
   public static final String FAILED_PLAIN_TEMPLATE = "FlexUnit test {0} in suite {1} failed.";
   public static final String ERRORED_PLAIN_TEMPLATE = "FlexUnit test {0} in suite {1} had errors.";
   public static final String IGNORED_PLAIN_TEMPLATE = "FlexUnit test {0} in suite {1} was ignored.";
   
   //XML constants for TestResult
   private static final String TEST = "testcase";
   private static final String CLASSNAME_ATTRIBUTE = "classname";
   private static final String NAME_ATTRIBUTE = "name";
   private static final String TIME_ATTRIBUTE = "time";
   private static final String FAILURE_ELEMENT = "failure";
   private static final String FAILURE_MESSAGE_ATTRIBUTE = "message";
   private static final String FAILURE_TYPE_ATTRIBUTE = "type";
   private static final String ERROR_ELEMENT = "error";
   private static final String IGNORE_ELEMENT = "skipped";
   
   //XML constants for Suites
   private static final String XML_PROCESSING_INSTRUCTION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
   private static final String TEST_SUITE = "testsuite";
   private static final String NAME_ATTRIBUTE_LABEL = "name";
   private static final String FAILURE_ATTRIBUTE_LABEL = "failures";
   private static final String ERROR_ATTRIBUTE_LABEL = "errors";
   private static final String IGNORE_ATTRIBUTE_LABEL = "skipped";
   private static final String TIME_ATTRIBUTE_LABEL = "time";
   private static final String TESTS_ATTRIBUTE_LABEL = "tests";
   private static final String HOSTNAME_ATTRIBUTE_LABEL = "hostname";
   private static final String TIMESTAMP_ATTRIBUTE_LABEL = "timestamp";
   
   static
   {
      TEST_XML_SUCCESS_TEMPLATE = "<" + TEST + " "
         + CLASSNAME_ATTRIBUTE + "=\"{0}\" "
         + NAME_ATTRIBUTE + "=\"{1}\" "
         + TIME_ATTRIBUTE + "=\"{2}\" "
         + "/>";
      
      TEST_XML_FAILURE_TEMPLATE = "<" + TEST + " "
         + CLASSNAME_ATTRIBUTE + "=\"{0}\" "
         + NAME_ATTRIBUTE + "=\"{1}\" "
         + TIME_ATTRIBUTE + "=\"{2}\">\n"
         + "<" + FAILURE_ELEMENT + " "
         + FAILURE_MESSAGE_ATTRIBUTE + "=\"{3}\" "
         + FAILURE_TYPE_ATTRIBUTE + "=\"{4}\">\n"
         + "<![CDATA[\n{5}\n]]>\n"
         + "</" + FAILURE_ELEMENT + ">\n"
         + "</" + TEST + ">";
      
      TEST_XML_ERROR_TEMPLATE = "<" + TEST + " "
         + CLASSNAME_ATTRIBUTE + "=\"{0}\" "
         + NAME_ATTRIBUTE + "=\"{1}\" "
         + TIME_ATTRIBUTE + "=\"{2}\">\n"
         + "<" + ERROR_ELEMENT + " "
         + FAILURE_MESSAGE_ATTRIBUTE + "=\"{3}\" "
         + FAILURE_TYPE_ATTRIBUTE + "=\"{4}\">\n"
         + "<![CDATA[\n{5}\n]]>\n"
         + "</" + ERROR_ELEMENT + ">\n"
         + "</" + TEST + ">";
      
      TEST_XML_IGNORED_TEMPLATE = "<" + TEST + " "
         + CLASSNAME_ATTRIBUTE + "=\"{0}\" "
         + NAME_ATTRIBUTE + "=\"{1}\" "
         + TIME_ATTRIBUTE + "=\"{2}\">\n"
         + "<" + IGNORE_ELEMENT + " />\n"
         + "</" + TEST + ">";
      
      SUITE_XML_TEMPLATE = XML_PROCESSING_INSTRUCTION + "\n" 
         + "<" + TEST_SUITE + " "
         + NAME_ATTRIBUTE_LABEL + "=\"{0}\" "
         + TESTS_ATTRIBUTE_LABEL + "=\"{1}\" "
         + FAILURE_ATTRIBUTE_LABEL + "=\"{2}\" "
         + ERROR_ATTRIBUTE_LABEL + "=\"{3}\" "
         + IGNORE_ATTRIBUTE_LABEL + "=\"{4}\" "
         + TIME_ATTRIBUTE_LABEL + "=\"{5}\" "
         + HOSTNAME_ATTRIBUTE_LABEL + "=\"{6}\" "
         + TIMESTAMP_ATTRIBUTE_LABEL + "=\"{7}\">\n"
         + "{8}"
         + "</" + TEST_SUITE + ">";
   }
   
   public static String formatTime(long time)
   {
      return String.valueOf(time / 1000.0000);
   }
}
