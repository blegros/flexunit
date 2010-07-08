/**
 * Copyright (c) 2009 Digital Primates IT Consulting Group
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author     Brian LeGros
 * @version
 **/

package org.flexunit.listeners
{
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.events.TimerEvent;
	import flash.net.Socket;
	import flash.utils.Timer;
	
	import org.flexunit.listeners.closer.ApplicationCloser;
	import org.flexunit.listeners.closer.StandAloneFlashPlayerCloser;
	import org.flexunit.reporting.FailureFormatter;
	import org.flexunit.runner.Descriptor;
	import org.flexunit.runner.IDescription;
	import org.flexunit.runner.Result;
	import org.flexunit.runner.notification.Failure;
	import org.flexunit.runner.notification.IAsyncStartupRunListener;
	import org.flexunit.runner.notification.ITemporalRunListener;
	import org.flexunit.runner.notification.async.AsyncListenerWatcher;
	
	public class CIListener extends EventDispatcher implements IAsyncStartupRunListener, ITemporalRunListener
	{
		protected static const DEFAULT_PORT : uint = 1024;
		protected static const DEFAULT_SERVER : String = "127.0.0.1";
      private const HANDSHAKE_REQUEST : String = "?ack?";
      private const HANDSHAKE_RESPONSE : String = "!yack!";
      private const HEARTBEAT_REQUEST : String = "?thump?";
      private const HEARTBEAT_RESPONSE : String = "!thump!";
      private const END_OF_RUN_NOTICE : String = "!EOR!";
		
		private var _ready:Boolean = false;
		
		private var socket:Socket;
		
		[Inspectable]
		public var port : uint;
		
		[Inspectable]
		public var server : String; //this is local host. same machine
		
		public var closer : ApplicationCloser;
		
		private var lastFailedTest:IDescription;
      private var lastTestTime : Number;
		private var timeOut:Timer;
		
		public function CIListener(port : uint = DEFAULT_PORT, server : String = DEFAULT_SERVER) 
		{
			this.port = port;
			this.server = server;
			this.closer = new StandAloneFlashPlayerCloser(); //default application closer
			
			socket = new Socket();
         socket.addEventListener( ProgressEvent.SOCKET_DATA, onData);
         socket.addEventListener( Event.CONNECT, onConnect);
         socket.addEventListener( IOErrorEvent.IO_ERROR, onError);
         socket.addEventListener( SecurityErrorEvent.SECURITY_ERROR, onError);
         
			timeOut = new Timer( 2000, 1 );
			timeOut.addEventListener(TimerEvent.TIMER_COMPLETE, declareBroken, false, 0, true );
			timeOut.start();

			try
			{
				socket.connect( server, port );
				timeOut.stop();
			} 
         catch (e:Error) 
         {
				//This needs to be more than a trace
				trace (e.message);
			}
		}
		
		private function declareBroken( event:TimerEvent ):void 
      {
			onError( new ErrorEvent("Flash movie timed out while attempting to connect to [" + server + ":" + port + "].") );
		}
		
		[Bindable(event="listenerReady")]
		public function get ready():Boolean 
		{
			return _ready;
		}

		private function readyToGo() : void 
      {
			_ready = true;
			dispatchEvent( new Event( AsyncListenerWatcher.LISTENER_READY ) );
		}
      
      private function onConnect(event:Event):void
      {
         socket.writeUTF(HANDSHAKE_REQUEST);
         socket.flush();
      }
      
      private function onError(event:Event):void
      {
         if ( !ready ) {
            //If we are not yet ready and received this, just inform the core so it can move on
            dispatchEvent( new Event( AsyncListenerWatcher.LISTENER_FAILED ) );
         } else {
            //If on the other hand we were ready once, then the core is counting on us... so, if something goes
            //wrong now, we are likely hung up. For now we are simply going to bail out of this process
            exit();
         }
      }

      private function onData( event : ProgressEvent ) : void
      {
         var result : String = socket.readUTF();
         if(!ready && result == HANDSHAKE_RESPONSE)
         {
            readyToGo();
         }
         
         if(ready && result == HEARTBEAT_REQUEST)
         {
            //tell the server we're alive
            writeHeartBeatResponse();
         }
      }
      
      private function writeHeartBeatResponse() : void
      {
         socket.writeUTF(HEARTBEAT_RESPONSE);
         socket.flush();
      }
      
      private function writeTestResult(descriptor : Descriptor, status : String, failureMessage : String = null, failureType : String = null, failureStackTrace : String = null) : void
      {
         var result : TestResult = new TestResult();
         result.className = descriptor.suite;
         result.name = descriptor.method;
         result.time = lastTestTime;
         result.status = status;
         result.failureMessage = failureMessage;
         result.failureType = failureType;
         result.failureStackTrace = failureStackTrace;
         
         socket.writeObject(result);
         socket.flush();
      }
		
		public function testTimed( description:IDescription, runTime:Number ):void {
			lastTestTime = runTime;
			//trace( description.displayName + " took " + runTime + " ms " );
		}
		
		public function testRunStarted( description:IDescription ):void
		{
			//Since description tells us nothing about failure, error, and skip counts, this is 
		   //computed by the Ant task as the process executes and no work is needed to signify
		   //the start of a test run.
		}
		
		public function testRunFinished( result:Result ):void 
		{
         socket.writeUTF(END_OF_RUN_NOTICE);
         socket.flush();
         exit();
		}
		
		public function testStarted( description:IDescription ):void 
		{
			// called before each test
		}
		
		public function testFinished( description:IDescription ):void 
		{
			// called after each test
			if(!lastFailedTest || description.displayName != lastFailedTest.displayName){
            var descriptor : Descriptor = getDescriptorFromDescription(description);
            writeTestResult(descriptor, TestResult.SUCCESS);
			}
		}
		
		public function testAssumptionFailure( failure:Failure ):void 
		{
			// called on assumptionFail
		}
		
		public function testIgnored( description:IDescription ):void 
		{
			// called on ignored test if we want to send ignore to ant.
			var descriptor:Descriptor = getDescriptorFromDescription(description);
         writeTestResult(descriptor, TestResult.IGNORE);
		}
		
		
		public function testFailure( failure:Failure ):void 
		{
			// called on a test failure
			lastFailedTest = failure.description;
			var descriptor:Descriptor = getDescriptorFromDescription(failure.description);
         
         var stackTrace : String = FailureFormatter.xmlEscapeMessage( stackTrace );
         var message : String = FailureFormatter.xmlEscapeMessage( message );
			
         var status : String = TestResult.FAILURE;
			if(FailureFormatter.isError(failure.exception)) 
			{
            status = TestResult.ERROR;
			}
			
         writeTestResult(descriptor, status, message, failure.description.displayName, stackTrace);
		}
		
		/*
		* Internal methods
		*/
		private function getDescriptorFromDescription( description:IDescription ):Descriptor
		{
			// reads relavent data from descriptor
			/**
			 * JAdkins - 7/27/07 - FXU-53 - Listener was returning a null value for the test class
			 * causing no data to be returned.  If length of array is greater than 1, then class is
			 * not in the default package.  If array length is 1, then test class is default package
			 * and formats accordingly.
			 **/
			var descriptor:Descriptor = new Descriptor();
			var descriptionArray:Array = description.displayName.split("::");
			var classMethod:String;
			if ( descriptionArray.length > 1 ) 
			{
				descriptor.path = descriptionArray[0];
				classMethod =  descriptionArray[1];
			} 
			else 
			{
				classMethod =  descriptionArray[0];
			}
			var classMethodArray:Array = classMethod.split(".");
			descriptor.suite = ( descriptor.path == "" ) ?  classMethodArray[0] :
				descriptor.path + "::" + classMethodArray[0];
			descriptor.method = classMethodArray[1];
			return descriptor;
		}
		
		/**
		 * Exit the test runner by calling the ApplicationCloser.
		 */
		protected function exit() : void
		{
			this.closer.close();
		}
	}
}
