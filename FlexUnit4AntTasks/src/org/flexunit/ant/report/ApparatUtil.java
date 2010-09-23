package org.flexunit.ant.report;

/**
 * Based on the implementation in org.sonatype.flexmojos.plugin.utilities::ApparatUtil in the FlexMojos 4.x project.
 */
public class ApparatUtil
{
   public static String toClassname( String apparatClassname )
   {
       // F:\4.x\flexmojos-aggregator\flexmojos-testing\flexmojos-test-harness\target\projects\concept\flexunit-example_testFlexUnitExample\src;com\adobe\example;Calculator.as
       String cn = apparatClassname;
       
       // com\adobe\example;Calculator.as
       cn = cn.substring( cn.indexOf( ';' ) + 1 );
       cn = cn.substring( 0, cn.indexOf( '.' ) );
       cn = cn.replace( '/', '.' ).replace( '\\', '.' ).replace( ';', '.' );
       
       return cn;
   }
}
