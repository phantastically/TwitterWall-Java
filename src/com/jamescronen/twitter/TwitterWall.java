package com.jamescronen.twitter;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TwitterWall {
	public SerialPort serialPort = null;
	
	public static void main(String args[]) throws Exception {
		TwitterWall twitterWall = new TwitterWall();

		try {
			twitterWall.initializeSerialPort();
			twitterWall.run();
		} finally {
			twitterWall.closeSerialPort();
		}
	}

	public void run() throws Exception {
		while (true) {
			Twitter twitter = TwitterFactory.getSingleton();
			
			List<Status> statuses = twitter.getHomeTimeline();
		    System.out.println("Showing home timeline.");
		    
		    Status status = statuses.get(0);
		    PrintWriter writer = new PrintWriter("test.file", "UTF-8");
		    writer.println(status.getUser().getScreenName());
		    writer.println(status.getText());
		    writer.close();

		    System.out.println("Writing tweet:\r@" + status.getUser().getScreenName() + ": " + status.getText());
		    
		    OutputStream outputStream = this.serialPort.getOutputStream();
		    outputStream.write(status.getUser().getScreenName().getBytes());
		    outputStream.write(0x0d);  // CR
		    outputStream.write(0x0a);  // LF
		    outputStream.write(status.getText().getBytes());
		    
		    Thread.sleep(120000);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void initializeSerialPort() throws PortInUseException {
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		
		while (portList.hasMoreElements()) {
			CommPortIdentifier port_id = (CommPortIdentifier) portList.nextElement();

			System.out.println("Found item in port list: " + port_id.getName());

			if (port_id.getName().equals("/dev/tty.usbserial") || port_id.getName().startsWith("/dev/tty.usbserial")) {
				System.out.println("Connecting to serial port " + port_id.getName());
				this.serialPort = (SerialPort) port_id.open("TwitterWall", 1800);
				return;
			}
		}

		System.out.println("Cannot find a viable serial port. Exiting.");
		System.exit(0);
	}

	public void closeSerialPort() {
		if (this.serialPort != null) {
			this.serialPort.close();
		}
	}
}
