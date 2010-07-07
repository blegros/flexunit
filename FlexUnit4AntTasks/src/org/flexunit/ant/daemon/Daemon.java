package org.flexunit.ant.daemon;

import java.io.File;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import org.apache.tools.ant.BuildException;
import org.flexunit.ant.LoggingUtil;
import org.flexunit.ant.daemon.workers.WorkerException;

//TODO: Add Javadocs
//TODO: Improve logging workflow
public class Daemon implements Callable<Object>
{
   private final String LOCALHOST = "127.0.0.1";

   private InetAddress hostAddress;
   private int port;
   private long timeout;
   
   private ServerSocketChannel serverChannel;
   private Selector selector;
   private SocketChannel socketChannel; // only possible if one connection is ever being made to server
   private ByteBuffer readBuffer;

   private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();
   private List<ByteBuffer> pendingData = new ArrayList<ByteBuffer>();

   private Timer readyTimer;
   private MessageRouter router;

   private Boolean ready = false;
   private Boolean shutdown = false;

   public Daemon(int port, int bufferSize, long timeout, File reportDir) throws IOException
   {
      this.hostAddress = InetAddress.getByName(LOCALHOST);
      this.port = port;
      this.timeout = timeout;
      
      this.readyTimer = new Timer();
      this.readBuffer = ByteBuffer.allocate(bufferSize);
      this.selector = this.initSelector();
      this.router = new MessageRouter(this, timeout, reportDir);
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
      
      //timeout if not ready by provided timeout value
      TimerTask task = new TimerTask(){
         @Override
         public void run()
         {
            if(!isReady())
            {
               LoggingUtil.log("The Ant task timed out waiting for a connection from the player...");
               halt();
            }
         }
      };
      
      readyTimer.schedule(task, timeout);

      return socketSelector;
   }
   
   private void destroyTimer()
   {
      readyTimer.cancel();
      readyTimer.purge();
      readyTimer = null;
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

      synchronized (ready)
      {
         result = ready;
      }

      return result;
   }

   public void ready()
   {
      synchronized (ready)
      {
         ready = true;
      }
      
      destroyTimer();
   }

   public void halt()
   {
      synchronized (this.changeRequests)
      {
         changeRequests.add(new ChangeRequest(ChangeRequest.SHUTDOWN, 0));
      }

      synchronized(this.shutdown)
      {
         shutdown = true;
      }
      
      
      if(readyTimer != null)
      {
         destroyTimer();
      }
      
      selector.wakeup();
   }
   
   public boolean shuttingDown()
   {
      boolean result = false;
      
      synchronized(shutdown)
      {
         result = shutdown;
      }
      
      return result;
   }

   public Object call() throws Exception
   {
      while (serverChannel.isOpen())
      {
         try
         {
            // Process any pending changes
            synchronized (this.changeRequests)
            {
               for (ChangeRequest change : changeRequests)
               {
                  switch (change.type)
                  {
                  case ChangeRequest.CHANGEOPS:
                     SelectionKey key = this.socketChannel.keyFor(this.selector);
                     key.interestOps(change.ops);
                     break;
                  case ChangeRequest.SHUTDOWN:
                     close();
                     return null;
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
               selectedKeys.remove(); // remove keys as read as to not read
                                      // again

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
         catch (Exception e)
         {
            e.printStackTrace(); // print error message that caused shutdown

            try
            {
               close();
            }
            catch (IOException ioe)
            {
               ioe.printStackTrace();
            }

            throw new BuildException(e); // throw that error as a build
                                         // exception
         }
      }

      return null;
   }

   private void accept(SelectionKey key) throws IOException, MultipleConnectionException
   {
      if (isReady())
      {
         throw new MultipleConnectionException("Cannot accept more than one connection at a time.");
      }

      // close the current socket channel and remove it
      if (this.socketChannel != null)
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
   }

   private void read(SelectionKey key) throws IOException, ClientShutdownException, WorkerException
   {
      SocketChannel channel = (SocketChannel) key.channel();
      boolean keepReading = true;

      while (keepReading)
      {
         // Clear out our read buffer so it's ready for new data
         this.readBuffer.clear();

         int numRead = channel.read(this.readBuffer);
         ;
         readBuffer.rewind(); // read move the buffer position forward

         if (numRead == -1)
         {
            // Remote entity shut the socket down. Do the
            // same from our end and cancel the channel.
            key.cancel();
            key.channel().close();

            if (isReady())
            {
               throw new ClientShutdownException();
            }
         }

         // Hand the data off to the message router if bytes were read
         if (numRead > 0)
         {
            byte[] message = new byte[numRead];
            readBuffer.get(message, 0, numRead);
            router.route(message);
         }

         // check to see if we should stop reading
         if (numRead != readBuffer.capacity())
         {
            keepReading = false;
         }
      }
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
         // Write until there's not more data ...
         while (!pendingData.isEmpty())
         {
            ByteBuffer buf = pendingData.get(0);

            socketChannel.write(buf);

            if (buf.remaining() > 0)
            {
               // ... or the socket's buffer fills up
               break;
            }

            pendingData.remove(0);
         }

         // We wrote away all data, so we're no longer interested
         // in writing on this socket.
         if (pendingData.isEmpty())
         {
            key.interestOps(SelectionKey.OP_READ);
         }
      }
   }

   private void close() throws IOException
   {
      router.shutdown();
      selector.close();
      
      if(socketChannel != null)
      {
         socketChannel.close();
      }
      
      serverChannel.close();
   }
}
