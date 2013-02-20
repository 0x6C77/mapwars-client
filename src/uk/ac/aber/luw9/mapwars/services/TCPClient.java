package uk.ac.aber.luw9.mapwars.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.aber.luw9.mapwars.controllers.MainController;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TCPClient implements Runnable {
	
	private static final String SERVER_IP = "178.17.41.167";
	private static final int SERVER_PORT = 4565;
	private ArrayBlockingQueue<String> messageQueue = new ArrayBlockingQueue<String>(100);
	private static MainController mainController;
	private boolean threadRunning;
	private ScheduledExecutorService exec;

	private Socket socket;
	private InetSocketAddress socketAddress = new InetSocketAddress(SERVER_IP, SERVER_PORT);
	
	public TCPClient() {
		mainController = MainController.getController();
	}
	
	public TCPClient(String message) {
		messageQueue.add(message);
	}
	
	public void sendMessage(String message) {
		if (!threadRunning) {
			startThread();
		}

		// THREAD THIS NOEW!!!!!
		checkSocket();
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			out.println(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.d("TCP", "C: Sent " + message);
	}
	
	public void startThread() {
		if (!threadRunning) {
			threadRunning = true;
			checkSocket();
			
			exec = Executors.newSingleThreadScheduledExecutor();
			exec.scheduleAtFixedRate(this, 0, 100, TimeUnit.MILLISECONDS);
		}
	}
	
	public void stopThread() {
		Log.i("TCPClient", "Stopping thread");
		exec.shutdown();
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadRunning = false;
	}
	
	final static Handler handler = new Handler() {
		  @Override
		  public void handleMessage(Message msg) {
			try {
				if (msg.obj != null) {
					Log.d("TCP", "C: Recieved " + msg.obj);
	
					JSONObject response = new JSONObject((String)msg.obj);
					if (mainController != null)
						mainController.handleTCPReply(response);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			super.handleMessage(msg);
		  }
	};
		
	public void run() {
		checkSocket();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
			Message msg = Message.obtain();
			msg.obj = in.readLine();
			handler.sendMessage(msg);
		} catch(Exception e) {
			Log.e("TCP", "S: Error");
		}
    }     
	
	public void checkSocket() {
		try {
			if (socket == null || socket.isClosed()) {
				socket = new Socket();
			}
			if (!socket.isConnected()) {
				socket.connect(socketAddress);
			}
        } catch (Exception e) {
       	 Log.e("TCP", "C: Error", e);
        }
	}
}