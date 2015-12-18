package easydarwin.android.videostreaming;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.vlc.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is a function class, it includes the major functions used in {@link VideoStreamingFragment} and 
 * {@link org.videolan.vlc.gui.video.VideoPlayerActivity}.
 * 
 * */
public class MultiRoom {
	
	/** */
	private final int PAINTVIEW = 2;
	private final int MESSAGEVIEW = 3;
	/** Chat room service in openfire, the format is conference.serverName: 
	 * In our server, it is named conference.myria*/
	private static final String serviceName = "conference.myria";
	private static final int DB_STREAMING_TOUCHINFO = 1;
	/** URI of touch information storage*/
	public static final String touchInfoURl = "http://129.128.184.46/db_insertInfo.php";
	/** URI of streams storage*/
	public static final String streamURl = "http://129.128.184.46/db_streamStore.php";

	
	private Activity context;

	private String rooom;
	
	/** Constructor 
	 * @param context*/
	public MultiRoom(Activity context){
		this.context = context;
	}
	/** Set method: set the chat-room name
	 * @param rooom chat-room name*/
	public void setChatRoom(String rooom){
		this.rooom = rooom;
	}
	/**Get method: get chat-room name
	 * @return chat-room name*/
	public String getChatRoom(){
		return rooom;
	}
	
	
	/**Create multiple users chat room (no password). You need to call {@link MultiUserChat#create(String)}.
	 * 
	 * @param connection XMMP connection
	 * @param roomName chat-room name
	 * @return return true if creating room successfully
	 * @throws XMPPException XMPP connection exception
	 * */
	public boolean createMultiUserRoom(XMPPConnection connection,
			String roomName) throws XMPPException {
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("createMultiUserRoom-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		// Get the MultiUserChatManager
		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, roomName + "@"+serviceName); 

		// Create the room
		muc.create(roomName);
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();

		// configure the room 
		List<String> roomOwner = new ArrayList<String>();
		roomOwner.add(connection.getUser());
			
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);   
        submitForm.setAnswer("muc#roomconfig_membersonly", false);  
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);          
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);  
        submitForm.setAnswer("x-muc#roomconfig_registration", true);  
        
		muc.sendConfigurationForm(submitForm);
		Log.i("CREATE_ROOM", roomName);
		
		return true;
	}
	
	/**Create multiple users chat room (with password). You need to call {@link MultiUserChat#create(String)}.
	 * 
	 * @param connection XMMP connection
	 * @param roomName chat-room name
	 * @param password the password to join the room
	 * @return return true if creating room successfully
	 * @throws XMPPException XMPP connection exception
	 * */
	public boolean createMultiUserRoom(XMPPConnection connection,
			String roomName, String password) throws XMPPException {
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("createMultiUserRoom-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		// Get the MultiUserChatManager
		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, roomName + "@"+serviceName); 

		// Create the room
		muc.create(roomName);
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();

		// configure the room 
		List<String> roomOwner = new ArrayList<String>();
		roomOwner.add(connection.getUser());
			
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);   
        submitForm.setAnswer("muc#roomconfig_membersonly", false);  
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);          
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);  
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);  
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", true);  
        submitForm.setAnswer("x-muc#roomconfig_registration", true);  
        
        // Set that the room requires a password
        submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
        // Set the password for the room
        submitForm.setAnswer("muc#roomconfig_roomsecret", password);
        
		muc.sendConfigurationForm(submitForm);
		Log.i("CREATE_ROOM", roomName);
		
		return true;
	}
	
	/** Join a chat room by default (no password). You need to call {@link MultiUserChat#join(String)}.
	 * @param connection XMPP connection
	 * @param roomName chat-room name
	 * @return return true if join the chat room successfully
	 * @throws XMPPException XMPP connection exception*/
	public boolean joinChatRoom(XMPPConnection connection, String roomName) throws XMPPException {
		if(connection!=null){
			// Get the MultiUserChatManager
			// Create a MultiUserChat using an XMPPConnection for a room
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@"+serviceName);
			muc.join(connection.getUser());
			Log.i("JOIN-USER-NAME",connection.getUser());

			return true;
		}
		else
			return false;
	}
	
	/** Join a chat room by default (with password). You need to call {@link MultiUserChat#join(String, String)}.
	 * @param connection XMPP connection
	 * @param roomName chat-room name
	 * @param password the password to join the chat-room
	 * @return return true if join the chat room successfully
	 * @throws XMPPException XMPP connection exception*/
	public boolean joinChatRoom(XMPPConnection connection, String roomName, String password) throws XMPPException {
		if(connection!=null){
			// Get the MultiUserChatManager
			// Create a MultiUserChat using an XMPPConnection for a room
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@"+serviceName);
			muc.join(connection.getUser(), password);
			Log.i("JOIN-USER-NAME",connection.getUser());

			return true;
		}
		else
			return false;
	}
	
	/** Get Existing rooms, and rejoin it.
	 * You need to call {@link MultiUserChat#getHostedRooms(org.jivesoftware.smack.Connection, String)}.
	 * FIXME: you can only join the room that you are allowed.
	 * 		solution1: when rejoin, send message to owner, and owner make a decision
	 *      solution2: create chat room with password and give it to friends
	 * 
	 * @param connection XMPP connection
	 * @param serviceName chat room service on openfire server
	 * @return roomList A list of all the chat-room or null if there's no rooms
	 * @throws XMPPException */
	public ArrayList<String> getChatRoomList(XMPPConnection connection, String serviceName) throws XMPPException {
		
		if(connection!=null){

			// Group Chat Rooms in the service "conference.myria"
			Collection<HostedRoom>  rooms = MultiUserChat.getHostedRooms(connection, serviceName);
			Log.i("ROOMLIST",Integer.toString(rooms.size()));
			ArrayList<String> roomList = new ArrayList<String>();
			for(HostedRoom room : rooms) {
				// room.getName()+"@conference.myria" == room.getJid();
				MultiUserChat muc = new MultiUserChat(connection, room.getJid());
				
				Iterator <String> sss = muc.getOccupants();			
				ArrayList<String> listUser = new ArrayList<String>();
				while (sss.hasNext()) {
			        String name = StringUtils.parseResource(sss.next());
			        listUser.add(name);
			        Log.e("ROOMLIST-------", "" + name);
			    }

				roomList.add(room.getName());
            }     
			Log.i("ROOMLIST",roomList.toString()); 
			return roomList;
		}else
			return null;
	}
	
	
	/** Invite users to a chat room (no password). You need to call {@link MultiUserChat#invite(String, String)}
	 * 
	 * @param connection XMMP connection
	 * @param roomName chat-room name
	 * @param friendsList the list of selected friends
	 * @return return true if sending invitation successfully
	 * @throws XMPPException */
	public boolean inviteToChatRoom(XMPPConnection connection, String roomName, ArrayList<String> friendsList) throws XMPPException {

		if(connection != null){
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@"+serviceName);
			muc.join(connection.getUser()+"-owner");

			// invite another users
			for(String friend: friendsList){
				Log.i("INVITATION-FRIENDS",friend);
				muc.invite(friend, "Join us "+friend);
			}
		
			return true;
		}else
			return false;
	}
	
	/** Invite users to a chat room with a password. You need to call {@link MultiUserChat#invite(String, String)}
	 * 
	 * @param connection XMMP connection
	 * @param roomName chat-room name
	 * @param friendsList the list of selected friends
	 * @param password password to join the chat room
	 * @return return true if sending invitation successfully
	 * @throws XMPPException */
	public boolean inviteToChatRoom(XMPPConnection connection, String roomName, ArrayList<String> friendsList, String password) throws XMPPException {

		if(connection != null){
			MultiUserChat muc = new MultiUserChat(connection, roomName
					+ "@"+serviceName);
			muc.join(connection.getUser()+"-owner", password);

			// invite another users
			for(String friend: friendsList){
				Log.i("INVITATION-FRIENDS",friend);
				muc.invite(friend, "Join us "+friend);
			}
		
			return true;
		}else
			return false;
	}

	
	/** Room-message-Listener. 
	 * 
	 * @param connection XMPP connection
	 * @param roomName chat-room name*/
	public void RoomMsgListenerConnection(XMPPConnection connection, String roomName) {

		if(!connection.isConnected()) {
			Log.i("SECOND-MULTIROOM-RoomMsgListenerConnection","connection == null! disconnected");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		// Add a packet listener to get messages sent to us
		MultiUserChat muc = new MultiUserChat(connection, roomName +"@"+serviceName);
		muc.addMessageListener(new PacketListener() {  
            @Override  
            public void processPacket(Packet packet) {  
            	Message message = (Message) packet;
                Log.i("ROOM-CHAT RECEIVE-MESSAGE: ", message.getFrom() + ":" + message.getBody());
                //room3@conference.myria/admin@myria/Smack-owner:dggjjk
                final String[] fromName = message.getFrom().split("/");
                final String msg = message.getBody().toString();
                mHandler.post(new Runnable() {
					@SuppressLint("NewApi")
					public void run() {
						
						/** update UI [new thread]
						 *  use handler to process it: display message
						 */
						String[] coordination = msg.split(",");
						if (msg.contains("PaintView"))	{
							android.os.Message handlerMsg = new android.os.Message();

							handlerMsg.what = PAINTVIEW;
							handlerMsg.obj = fromName[1]+ ": (" + coordination[1]+","+coordination[2]+")";
							mHandler.sendMessage(handlerMsg);
						}else{
							android.os.Message handlerMsg = new android.os.Message();
							handlerMsg.what = MESSAGEVIEW;
							handlerMsg.obj = fromName[1]+ ": " + msg;
							mHandler.sendMessage(handlerMsg);
						}
						
//						// notification or chat...	
//						if(msg.equals("PaintView")){
//							String[] coordination = msg.split(",");
//							Toast.makeText(context,fromName[1]+ ": (" + coordination[1]+","+coordination[2]+")", Toast.LENGTH_SHORT).show();
//						}else
//							Toast.makeText(context,fromName[1]+ ": " + msg, Toast.LENGTH_SHORT).show();
					}
				}); 
            }  
            
            @SuppressLint("HandlerLeak")
			private Handler mHandler = new Handler(Looper.getMainLooper()) {
        		
        		@Override
        		public void handleMessage(android.os.Message handlerMsg) {
        			super.handleMessage(handlerMsg);
        			switch (handlerMsg.what) {
        			case PAINTVIEW:
        				Log.i("PAINTVIEW", handlerMsg.obj.toString() );
//        				Toast.makeText(context, handlerMsg.obj.toString(), Toast.LENGTH_SHORT).show();
        				/** redraw circle on screen in here */
        				/** NOTE: because Sender and Receiver use different technologies to draw circle,
        				       (View and surfaceView respectively), the reason is that on Sender side,
        				       if use surfaceView on the top of screen, no control button can be set over it.
        				       so we can not do it in the same way. */
        				break;
        			case MESSAGEVIEW:
        				Toast.makeText(context, handlerMsg.obj.toString(), Toast.LENGTH_SHORT).show();
        				break;
        			default:
        				break;
        			}
        		}
        	};
        });  
	}
	

	/** Save touch information into MySQL. 
	 * You need to start a new thread to save {@link MyAsyncTask}.
	 * 
	 * @param connection XMPP connection
	 * @param room
	 * @param timestamp
	 * @param coordinate
	 * */
	public void touchAnnotation(final XMPPConnection connection, final String room, final String timestamp, final String coordinate){
		AlertDialog.Builder tagDialog = new AlertDialog.Builder(context);
		final EditText input = new EditText(context);

		input.setHint("tag some annotation...");
//		tagDialog.setTitle(R.string.TAG_title).setView(input);
		tagDialog.setView(input);
		
		tagDialog.setPositiveButton(R.string.TAG_send,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int button) {

					final String tag = input.getText().toString();
					SendMessage(connection, room, tag);
					
					/** store these data
			        [connection.getUser(), timestamp, (xTouch, yTouch), tag]
			        */
					//[connection.getUser(), roomname, timestamp, (xTouch, yTouch), tag]
					// store the touch event data
					JSONObject dataObject = new JSONObject();
					try {
						dataObject.put("username", connection.getUser().split("/")[0]);
						dataObject.put("roomname", room);
						dataObject.put("timestamp", timestamp);
						dataObject.put("coordinate", coordinate);
						dataObject.put("annotation", tag);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
//					Log.i("DATA",dataObject.toString());
//					android.os.Message msg = new android.os.Message();
//					msg.what = DB_STREAMING_TOUCHINFO;
//					msg.obj = dataObject;	
//					mHandler.sendMessage(msg);
					
					// directly save data into database (not use a new thread)
					new MyAsyncTask().execute(dataObject.toString());	

				}
			}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int button) {
						return;
					}
			}).show();

	}
	
	/** [depreciated] Save touch information into MySQL */
	public void saveTouchInfo(String dataObject){
		
		Log.i("MultiRoom-->VideoPlayerActivity","Save success!");
		new MyAsyncTask().execute(dataObject.toString());	
	}
	
	/**Use openRTSP tool to store media stream into a file,
	 * details see: http://www.live555.com/openRTSP. 
	 * You need to start a new thread {@link StreamAsyncTask}
	 * 
	 * PATH: file path is current file path of openRTSP
	 * FileName: -F filename
	 * 
	 * @param room chat-room name
	 */
	public void storeMediaStream(String room) {
		
		JSONObject streamInfo = new JSONObject();
		try {
			// use the room name as file name: room2015_xxxx_xxxx

			
			/** stream store configuration
			 *  use openRTSP tool to store streams 
			 *  -D 3 -B 10000000 -b 10000000 -4 -w 640 -h 480 -f 24 -Q -d 60 -P 300
			 */
			String parameter = "./openRTSP -D 3 -B 10000000 -b 10000000 -4 -w 640 -h 480 -f 24 -Q -d 60 -P 300 -F "+room;
			String url = " rtsp://129.128.184.46:8554/"+room+".sdp";
			streamInfo.put("parameter", parameter);
			streamInfo.put("streamUrl", "rtsp://129.128.184.46:8554/"+room+".sdp");
			
			streamInfo.put("play", parameter + url);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		new StreamAsyncTask().execute(streamInfo.toString());	
		
	}
	
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DB_STREAMING_TOUCHINFO:
				
				// use async thread to store touch info into database
				new MyAsyncTask().execute(msg.obj.toString());	
			
				break;
			}
		}
	};
	
	
	/** Save touch information class thread*/
	public class MyAsyncTask extends AsyncTask<String, Integer, Double>{
		 
		@Override
		protected Double doInBackground(String... params) {
			postData(params[0]);
			return null;
		}
 
		public void postData(String data) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(touchInfoURl);
			
			try {
				JSONObject dataObject = new JSONObject(data);
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
	            params.add(new BasicNameValuePair("username", dataObject.get("username").toString()));
	            params.add(new BasicNameValuePair("roomname", dataObject.get("roomname").toString()));
	            params.add(new BasicNameValuePair("timestamp", dataObject.get("timestamp").toString()));
	            params.add(new BasicNameValuePair("coordinate", dataObject.get("coordinate").toString()));
	            params.add(new BasicNameValuePair("annotation", dataObject.get("annotation").toString()));

	            httppost.setEntity(new UrlEncodedFormEntity(params));
	            
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String result = EntityUtils.toString(response.getEntity());

				// {"success":1,"message":"Touch event info successfully created."}
				
//				JSONObject resultJson = new JSONObject(result);
//				if(resultJson.get("success").equals("1")){
//					//
//				}else{
//					//
//				}
					
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
		}
 
	}
	
	/** Save streams class thread*/
	public class StreamAsyncTask extends AsyncTask<String, Integer, Double>{
		 
		@Override
		protected Double doInBackground(String... params) {
			postData(params[0]);
			return null;
		}
 
		public void postData(String data) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(streamURl);
			
			try {
				JSONObject streamInfo = new JSONObject(data);
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("parameter", streamInfo.get("parameter").toString()));
	            params.add(new BasicNameValuePair("streamUrl", streamInfo.get("streamUrl").toString()));
	            params.add(new BasicNameValuePair("play", streamInfo.get("play").toString()));

	            httppost.setEntity(new UrlEncodedFormEntity(params));
	            
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				String result = EntityUtils.toString(response.getEntity());
				Log.i("Strore response", result);
				
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
			
		}
 
	}
	
	/**Send message function. You need to call {@link MultiUserChat#sendMessage(Message)}.
	 * 
	 * @param connection XMPP connection
	 * @param room chat-room name
	 * @param textMessage input message
	 * */
	public void SendMessage(XMPPConnection connection, String room, String textMessage){
		
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("SendMessage-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		if(room!=null){
			MultiUserChat muc = new MultiUserChat(connection, room);  
//			String text = textMessage.getText().toString();
			if(!textMessage.equals("")&&textMessage!=null){
				
				Message message = new Message(room + "@"+serviceName,Message.Type.groupchat);  
	            message.setBody(textMessage);  
				try {
					if (muc != null) {
						muc.sendMessage(message);
						Log.i("SEND-MSG-TO-ROOM", "Sending text " + textMessage + " to " + room+"=="+muc.getRoom());
					}
				} catch (XMPPException e) {
					e.printStackTrace();
				} 
	
//				textMessage.setText("");
			}else{
				Toast.makeText(context, "The input cannot be null!",
						Toast.LENGTH_SHORT).show();
			} 
		}else{
//			room = getChatRoom();
			Log.i("MULTIROOM-SENDMESSAGE:", "room Name"+room);
			Toast.makeText(context, "Please join a Room first",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/*** Send notification such "depart or destroy room."
	 * 
	 * @param connection XMPP connection
	 * @param room chat-room name
	 * @param content content of notification
	 * */
	public void SendNotification(XMPPConnection connection, String room, String content){

		MultiUserChat muc = new MultiUserChat(connection, room);  
		Message message = new Message(muc.getRoom()+"@"+serviceName, Message.Type.groupchat);  
        message.setBody(content);  
		 
		try {
			if (muc != null)
				muc.sendMessage(message);
			Log.i("ROOM-NOTIFICATION", "Sending text " + content + " to " + muc.getRoom());
		} catch (XMPPException e) {
			e.printStackTrace();
		} 
	
	}
	
	/**stop XMPPconnection
	 * Do not call it:  
	 * @param connection XMPP connection*/ 
	public void stopConnection(XMPPConnection connection){
		try {
			if (connection!=null){
				connection.disconnect();
				Log.i("STOP","stop connection");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		connection = null;
	}
	
	/** user off line
	 * @param connection XMPP connection
	 * */
	public void userOffline(XMPPConnection connection){
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("userOffline-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		 Presence presence = new Presence(Presence.Type.unavailable);
	     connection.sendPacket(presence);
	     Log.i("off-line","off-line");
	}
	
	/** Destroy /leave the chat room. You need to call {@link MultiUserChat#destroy(String, String)}.
	 * 
	 * @param connection XMPP connection
	 * @param room chat-room name
	 * @return return true, if departing/destroying room successfully
	 * */
	public boolean departChatRoom(XMPPConnection connection,String room){  
		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			Log.i("departChatRoom-SECOND-CREATEROOM_BUG","connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	    MultiUserChat muc = new MultiUserChat(connection, room+"@"+serviceName); //Must write room- jid  
	    try {
			muc.destroy("destroy reason", room + "@"+serviceName);
			Log.i("LEAVE_ROOM",connection.getUser()+" Destroy the room");
//			room = null;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return true;    
	}

}
