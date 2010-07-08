package org.flexunit.ant.daemon.workers;

import org.flexunit.ant.daemon.Daemon;
import org.flexunit.ant.daemon.helpers.WorkerUtil;

public class HandshakeWorker implements Worker
{
   private static final String HANDSHAKE_REQUEST = "?ack?"; 
   private static final String HANDSHAKE_RESPONSE = "!yack!";

   private Daemon server;
   private boolean shook = false;

   public HandshakeWorker(Daemon server)
   {
      this.server = server;
   }

   public boolean canProcess(byte[] message)
   {
      boolean accept = false;
      
      //if I haven't shaken hands yet, see if I'm being asked to
      if(!shook)
      {
         String request = WorkerUtil.readUtf8String(message);
         accept = HANDSHAKE_REQUEST.equals(request);
      }
      
      return accept;
   }

   public byte[] process(byte[] message) throws WorkerException
   {
      //send return signal for handshake
      server.send(WorkerUtil.toUtf8Bytes(HANDSHAKE_RESPONSE));
      
      shook = true;     //tell the worker it's been shook
      server.ready();   //tell the server its ready to accept messages
      
      return null;
   }
}
