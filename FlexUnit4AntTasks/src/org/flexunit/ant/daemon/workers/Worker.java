package org.flexunit.ant.daemon.workers;

public interface Worker
{
   public boolean canProcess(byte[] message);
   public byte[] process(byte[] message);
}
