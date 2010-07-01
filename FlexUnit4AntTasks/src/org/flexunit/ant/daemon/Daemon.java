package org.flexunit.ant.daemon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.flexunit.ant.LoggingUtil;

public class Daemon implements Runnable
{
   public static final String DEFAULT_HOST = "127.0.0.1";
   public static int DEFAULT_PORT = 1025;
   private final int BUFFER_SIZE = 262144;

   private InetAddress hostAddress;
   private int port;
   private ServerSocketChannel serverChannel;
   private Selector selector;
   private SocketChannel socketChannel; // only possible if one connection is
                                        // ever being made to server
   private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

   private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();
   private List<ByteBuffer> pendingData = new ArrayList<ByteBuffer>();

   private MessageRouter router;

   private Boolean ready = false;
   
   private int reads = 0;

   public Daemon(InetAddress hostAddress, int port) throws IOException
   {
      this.hostAddress = hostAddress;
      this.port = port;
      this.selector = this.initSelector();
      this.router = new MessageRouter(this);
   }

   private Selector initSelector() throws IOException
   {
      // Create a new selector
      Selector socketSelector = SelectorProvider.provider().openSelector();

      // Create a new non-blocking server socket channel
      this.serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);

      // Bind the server socket to the specified address and port
      InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.getPort());
      serverChannel.socket().bind(isa);

      // Register the server socket channel, indicating an interest in
      // accepting new connections
      serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

      return socketSelector;
   }

   public InetAddress getHostAddress()
   {
      return hostAddress;
   }

   public int getPort()
   {
      return port;
   }

   public boolean isReady()
   {
      boolean result = false;
      
      synchronized(ready)
      {
         result = ready;
      }
      
      return result;
   }

   public void ready()
   {
      synchronized(ready)
      {
         ready = true;
      }
   }

   public void run()
   {
      while (true)
      {
         try
         {
            // Process any pending changes
            synchronized (this.changeRequests)
            {
               Iterator<ChangeRequest> changes = this.changeRequests.iterator();
               while (changes.hasNext())
               {
                  ChangeRequest change = (ChangeRequest) changes.next();
                  switch (change.type)
                  {
                  case ChangeRequest.CHANGEOPS:
                     SelectionKey key = this.socketChannel.keyFor(this.selector);
                     key.interestOps(change.ops);
                  }
               }
               this.changeRequests.clear();
            }

            // Wait for an event one of the registered channels
            this.selector.select();

            // Iterate over the set of keys for which events are available
            Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
            while (selectedKeys.hasNext())
            {
               SelectionKey key = (SelectionKey) selectedKeys.next();
               selectedKeys.remove(); // absolutely must remove keys as they are
               // read, otherwise infinite loop

               if (!key.isValid())
               {
                  continue;
               }

               if (key.isAcceptable())
               {
                  this.accept(key);
               }
               else if (key.isReadable())
               {
                  this.read(key);
               }
               else if (key.isWritable())
               {
                  this.write(key);
               }
            }
         }
         catch (MultipleConnectionException mce)
         {
            mce.printStackTrace();
            throw new BuildException(mce);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new BuildException(e);
         }
      }
   }

   private void accept(SelectionKey key) throws IOException, MultipleConnectionException
   {
      //close the current socket channel and remove it
      if(this.socketChannel != null)
      {
         this.socketChannel.close();
      }
      
      // For an accept to be pending the channel must be a server socket
      // channel.
      ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

      // Accept the connection and make it non-blocking
      SocketChannel socketChannel = serverSocketChannel.accept();
      socketChannel.configureBlocking(false);

      LoggingUtil.log("Connection accepted.");

      // Register the new SocketChannel with our Selector, indicating
      // we'd like to be notified when there's data waiting to be read
      socketChannel.register(this.selector, SelectionKey.OP_READ);

      this.socketChannel = socketChannel;

      LoggingUtil.log("Waiting for data ...");
   }

   private void read(SelectionKey key) throws IOException
   {
      LoggingUtil.log("Read request: " + ++reads);
      SocketChannel channel = (SocketChannel) key.channel();

      // Clear out our read buffer so it's ready for new data
      this.readBuffer.clear();

      // Attempt to read off the channel
      int numRead = fillBuffer(channel);
      if (numRead == -1)
      {
         // Remote entity shut the socket down cleanly. Do the
         // same from our end and cancel the channel.
         key.channel().close();
         key.cancel();
         return;
      }

      // Hand the data off to the message router
      //TODO: May have to upgrade to read from socketBuffer until numRead < capacity or numRead == 0 so can use a smaller buffer
      if(numRead != 0)
      {
         byte[] message = new byte[numRead];
         readBuffer.get(message, 0, numRead);
         router.route(message);
      }
   }
   
   private int fillBuffer(SocketChannel channel)
   {
      int numRead = 0;
      try
      {
         numRead = channel.read(this.readBuffer);
         this.readBuffer.rewind();
      }
      catch (IOException e)
      {
         numRead = -1;
      }
      
      return numRead;
   }

   public void send(byte[] data)
   {
      synchronized (this.changeRequests)
      {
         LoggingUtil.log("Recieved request to write ...");

         // Indicate we want the interest ops set changed
         this.changeRequests.add(new ChangeRequest(ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

         // And queue the data we want written
         synchronized (this.pendingData)
         {
            pendingData.add(ByteBuffer.wrap(data));
         }
      }

      // Finally, wake up our selecting thread so it can make the required
      // changes
      this.selector.wakeup();
   }

   private void write(SelectionKey key) throws IOException
   {
      SocketChannel socketChannel = (SocketChannel) key.channel();

      synchronized (this.pendingData)
      {
         LoggingUtil.log("Write queue size: " + pendingData.size());

         // Write until there's not more data ...
         while (!pendingData.isEmpty())
         {
            LoggingUtil.log("Writing head of queue to socket.");

            ByteBuffer buf = (ByteBuffer) pendingData.get(0);

            socketChannel.write(buf);

            if (buf.remaining() > 0)
            {
               // ... or the socket's buffer fills up
               break; // TODO: revisit
            }

            pendingData.remove(0);
         }

         if (pendingData.isEmpty())
         {
            LoggingUtil.log("Write queue empty, switching to read mode.");

            // We wrote away all data, so we're no longer interested
            // in writing on this socket. Switch back to waiting for
            // data.
            key.interestOps(SelectionKey.OP_READ);
         }
      }
   }
}
