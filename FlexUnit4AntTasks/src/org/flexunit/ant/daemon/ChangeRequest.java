package org.flexunit.ant.daemon;

public class ChangeRequest {
   public static final int REGISTER = 1;
   public static final int CHANGEOPS = 2;
   
   public int type;
   public int ops;
   
   public ChangeRequest(int type, int ops) {
     this.type = type;
     this.ops = ops;
   }
 }
