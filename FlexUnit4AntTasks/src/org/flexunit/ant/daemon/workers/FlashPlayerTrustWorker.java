package org.flexunit.ant.daemon.workers;

import java.text.MessageFormat;

import org.flexunit.ant.daemon.Daemon;

public class FlashPlayerTrustWorker implements Worker
{
   private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";

   private static final String DOMAIN_POLICY = "<?xml version=\"1.0\"?>"
         + "<cross-domain-policy xmlns=\"http://localhost\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.adobe.com/xml/schemas PolicyFileSocket.xsd\">"
         + "<allow-access-from domain=\"*\" to-ports=\"{0}\" />" + "</cross-domain-policy>\u0000";

   private Daemon server;
   private boolean negotiated = false;

   public FlashPlayerTrustWorker(Daemon server)
   {
      this.server = server;
   }

   public boolean canProcess(byte[] message)
   {
      boolean accept = false;

      if (!negotiated)
      {
         //did we potentially get a policy request
         if(message.length > POLICY_FILE_REQUEST.length())
         {
            String policyMessage = new String(message, 0, POLICY_FILE_REQUEST.length());
            accept = POLICY_FILE_REQUEST.equals(policyMessage);
         }
         
         //if we're not marked as ready and got a message that wasn't the policy 
         //request, then there was local trust and we're done
         if(!accept)
         {
            negotiated = true;
         }
      }

      return accept;
   }

   public byte[] process(byte[] message) throws WorkerException
   {
      try
      {
         String response = MessageFormat.format(DOMAIN_POLICY, new Object[]{ 
               Integer.toString(server.getPort()) 
            });

         server.send(response.getBytes());
         
         negotiated = true;
      }
      catch(Exception e)
      {
         throw new WorkerException("Could not parse FP trust policy template", e);
      }
      
      return null;
   }
}
