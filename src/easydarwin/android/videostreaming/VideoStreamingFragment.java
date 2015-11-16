package easydarwin.android.videostreaming;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.Session.Callback;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtp.RtpThread;
import net.majorkernelpanic.streaming.rtsp.RtspClient;
import net.majorkernelpanic.streaming.video.VideoQuality;
import openfire.chat.activity.LoginActivity;
import openfire.chat.adapter.FriendsAdapter;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.videolan.vlc.R;
import org.videolan.vlc.VLCCallbackTask;
import org.videolan.vlc.audio.AudioServiceController;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import easydarwin.android.service.EasyCameraApp;
import easydarwin.android.service.SettingsActivity;

@SuppressLint("ClickableViewAccessibility")
public class VideoStreamingFragment extends Fragment implements Callback,
		RtspClient.Callback, android.view.SurfaceHolder.Callback,
		OnClickListener {

	private static final int REQUEST_SETTING = 1000;
	// display current time
	private static final int DISPLAY = 1;

	private BroadcastReceiver mReceiver;
	private String mAddress;
	private String mPort;
	private String mVideoName;
	protected Session mSession;
	protected RtspClient mClient;

	/** Default quality of video streams. */
	public VideoQuality videoQuality;
	/** By default AMRNB is the audio encoder. */
	public int audioEncoder = SessionBuilder.AUDIO_AMRNB;
	/** By default H.264 is the video encoder. */
	public int videoEncoder = SessionBuilder.VIDEO_H264;
	private static final int mOrientation = 0;
	private Button btnOption;
	private Button btnSelectContact;
	private Button btnStop;
	private Button btnSendMessage;
	private TextView ipView;
	private TextView mTime;
	private boolean alive = false;
	private SurfaceView mSurfaceView;
	private static SurfaceHolder surfaceHolder;
	private SharedPreferences preferences;

	private Pattern pattern = Pattern.compile("([0-9]+)x([0-9]+)");
	public static String username;
	public static String password;
	// private String entries;
	private List<Map<String, String>> friendList;

	public static XMPPConnection connection;
	private String streaminglink = "";
	private String streaminglinkTag = "";//"rtsp://129.128.184.46:8554/";
	private String curDateTime;

	/** draw a circle when touch the screen */
	// private PaintView paintView;
	private Paint mPaint;
	private PaintThread paintThread;
	
	private android.view.SurfaceView paintView;
	private SurfaceHolder paintViewHolder;
	private FragmentActivity faActivity;

	public static MultiRoom mRoom;
	private String room = null; // "room3"
	
	//TODO: 
	public static String Password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// get XMPPConnection if login success
		connection = LoginActivity.connection;
		// check for after videoPlaying back to streamingFragment
		if (!connection.isConnected()) {
			Log.i("SECOND-CREATEROOM_BUG", "connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		faActivity = (FragmentActivity) super.getActivity();

		View v = inflater.inflate(R.layout.streaming_main, container, false);
		// set provider
		configureProviderManager(ProviderManager.getInstance());
		faActivity.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);

		initView(v);

//		curDateTime = new SimpleDateFormat("yyyy_MMdd_HHmmss").format(Calendar.getInstance().getTime());
//		streaminglink = streaminglinkTag + getDefaultDeviceId()+ curDateTime + ".sdp";
//		streaminglink = "rtsp://129.128.184.46:8554/live.sdp";
		
//		streaminglinkTag = "rtsp://"+mAddress+":"+mPort+"/";
		 
		boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
				|| TextUtils.isEmpty(mPort) || TextUtils.isEmpty(mVideoName));
		if (EasyCameraApp.sState != EasyCameraApp.STATE_DISCONNECTED) {
			setStateDescription(EasyCameraApp.sState);
		}
		if (bParamInvalid) {
			startActivityForResult(new Intent(faActivity,
					SettingsActivity.class), REQUEST_SETTING);
		} else {
			// streaminglink = String.format("rtsp://%s:%d/%s.sdp", mAddress,
			// Integer.parseInt(mPort), mVideoName);
			// ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
			// Integer.parseInt(mPort), mVideoName));

		}
		
		/** Invitation Listener */
		InvitationListener(connection);
		// send available after return from VideoPlayerActivity.java
		Presence presence = new Presence(Presence.Type.available);
		connection.sendPacket(presence);

		btnSelectContact.setOnClickListener(this);
		btnOption.setOnClickListener(this);
		// btnStop.setOnClickListener(this);
		btnSendMessage.setOnClickListener(this);
		// EditText: set android keyboard enter button as send button
		textMessage.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				mRoom.SendMessage(connection, room, textMessage.getText()
						.toString());
				textMessage.setText("");
				return true;
			}
		});

		return v;
	}

	@SuppressWarnings("deprecation")
	public void initView(View v) {

		mRoom = new MultiRoom(faActivity);

		mAddress = preferences.getString("key_server_address", null);
		mPort = preferences.getString("key_server_port", null);
		mVideoName = preferences.getString("key_device_id", null/*
																 * getDefaultDeviceId
																 * ()
																 */);
		ipView = (TextView) v.findViewById(R.id.main_text_description);
		mTime = (TextView) v.findViewById(R.id.timeDisplay);

		mSurfaceView = (SurfaceView) v.findViewById(R.id.surface);
		mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
		surfaceHolder = mSurfaceView.getHolder();
		surfaceHolder.addCallback(this);
		// needed for sdk < 11
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// draw paint View
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Style.STROKE);
		
		// paintView = (PaintView)v.findViewById(R.id.drawView);
		paintView = (android.view.SurfaceView) v.findViewById(R.id.drawView);
		paintViewHolder = paintView.getHolder();
		paintView.setZOrderOnTop(true);

		paintViewHolder.setFormat(PixelFormat.TRANSPARENT);
		paintViewHolder.addCallback(paintViewCallback);
		
		
		btnSelectContact = (Button) v.findViewById(R.id.btnPlay);
		btnOption = (Button) v.findViewById(R.id.btnOptions);
		// btnStop = (Button) v.findViewById(R.id.btnStop);

		// get username & password for [VideoPlayerActivity] reconnect to the
		// XMPP server
		username = faActivity.getIntent().getStringExtra("username");
		password = faActivity.getIntent().getStringExtra("password");
		// send message
		textMessage = (EditText) v.findViewById(R.id.edit_say_something);
		btnSendMessage = (Button) v.findViewById(R.id.btn_send_message);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPlay:
			// get all the friends
			friendList = getAllFriendsUser(connection);

			if (!alive) {
				//make paintView visable			
				paintView.setVisibility(View.VISIBLE);
				// popup ContactList and select to send invitation
				popupContactList(/* entries */);

			} else {
				// make paintView disappear
				paintView.setVisibility(View.GONE);
				
				alive = false;
				stopStream();
				String msg = "Owner destroyed the room!";
				// send disconnect connection notifiation
				mRoom.SendNotification(connection, room, msg);
				// leave the chat room
				mRoom.departChatRoom(connection, room);

				btnSelectContact.setBackgroundResource(R.drawable.play);
				ipView.setText("");
				// ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
				// mAddress,Integer.parseInt(mPort), mVideoName));

			}
			break;
		case R.id.btnOptions:
			Intent intent = new Intent();
			intent.setClass(faActivity, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_SETTING);

			break;
		// case R.id.btnStop:
		// if (alive) {
		// alive = false;
		// stopStream();
		// String msg = "Owner disconnected the connection!";
		// mRoom.SendNotification(connection, room, msg);
		// btnSelectContact.setBackgroundResource(R.drawable.play);
		// ipView.setText(String.format("rtsp://%s:%d/%s.sdp", mAddress,
		// Integer.parseInt(mPort), mVideoName));
		// // off line
		// mRoom.userOffline(connection);
		// }
		// faActivity.finish();
		//
		// break;
		case R.id.btn_send_message:
			mRoom.SendMessage(connection, room, textMessage.getText()
					.toString());
			textMessage.setText("");
			break;
		}
	}

	/**
	 * start video streaming function
	 */
	private void PLAYVideoStreaming(final String linkname) {
		preferences = PreferenceManager.getDefaultSharedPreferences(faActivity);

		/** draw a circle when user touch the screen */
		paintView.setOnTouchListener(paintViewTouchListener);

		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected void onProgressUpdate(Void... values) {

				super.onProgressUpdate(values);
				alive = true;
				btnSelectContact.setBackgroundResource(R.drawable.pause);
				// start time thread
				new CurrentTimeThread().start();

			}

			@Override
			protected Integer doInBackground(Void... params) {

				publishProgress();

				if (mSession == null) {// try to load video info directly...
					boolean audioEnable = preferences.getBoolean(
							"p_stream_audio", true);
					boolean videoEnable = preferences.getBoolean(
							"p_stream_video", true);
					audioEncoder = Integer.parseInt(preferences.getString(
							"p_audio_encoder", String.valueOf(audioEncoder)));
					videoEncoder = Integer.parseInt(preferences.getString(
							"p_video_encoder", String.valueOf(videoEncoder)));

					Matcher matcher = pattern.matcher(preferences.getString(
							"video_resolution", "320x240"));
					matcher.find();

					videoQuality = new VideoQuality(Integer.parseInt(matcher
							.group(1)), Integer.parseInt(matcher.group(2)),
							Integer.parseInt(preferences.getString(
									"video_framerate", "15")),
							Integer.parseInt(preferences.getString(
									"video_bitrate", "300")) * 1000);
					mSession = SessionBuilder.getInstance()
							.setContext(faActivity.getApplicationContext())
							.setAudioEncoder(audioEnable ? audioEncoder : 0)
							.setVideoQuality(videoQuality)
							.setAudioQuality(new AudioQuality(8000, 32000))
							.setVideoEncoder(videoEnable ? videoEncoder : 0)
							.setOrigin("127.0.0.0").setDestination(mAddress)
							.setSurfaceView(mSurfaceView)
							.setPreviewOrientation(mOrientation)
							.setCallback(VideoStreamingFragment.this).build();
				}

				if (mClient == null) {
					// Configures the RTSP client
					mClient = new RtspClient();

					String tranport = preferences.getString(
							EasyCameraApp.KEY_TRANPORT, "0");
					if (tranport.equals("0")) {
						mClient.setTransportMode(RtspClient.TRANSPORT_TCP);
					} else {
						mClient.setTransportMode(RtspClient.TRANSPORT_UDP);
					}

					mClient.setSession(mSession);
					mClient.setCallback(VideoStreamingFragment.this);
				}

				mClient.setCredentials("", "");
				mClient.setServerAddress(mAddress, Integer.parseInt(mPort));
				// mClient.setStreamPath(String.format("/%s.sdp",preferences.getString("key_device_id",
				// Build.MODEL)));
				mClient.setStreamPath(String.format("/%s.sdp", linkname));
				/** IMPORTANT, start push stream. */
				mClient.startStream();
				return 0;
			}

		}.execute();
	}

	/** Invitation Listener */
	public void InvitationListener(XMPPConnection connection) {

		// check for after videoPlaying back to streamingFragment
		if (!connection.isConnected()) {
			Log.i("InvitationListener-SECOND-CREATEROOM_BUG",
					"connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}

		MultiUserChat.addInvitationListener(connection,
				new InvitationListener() {
					@Override
					public void invitationReceived(Connection conn,
							String room, String inviter, String reason,
							String password, Message message) {
						
						// userB.Room = userA.Room if userA.Room!=null
						mRoom.setChatRoom(room.split("@")[0]); 
						
						// accepted by default
						MultiUserChat muc = new MultiUserChat(conn, room);
						try {
							//TODO 1: require password >>>>>>>>>>>>>>>>>>>>.
							Log.i("VideoStreamingF", "password: "+ password);
							muc.join(conn.getUser(), password);
							Password = password;
							
//							muc.join(conn.getUser());
							Log.i("INVITATION", "invite to join success!");
							Log.i("VideoStreamingF", password);
						} catch (XMPPException e) {
							e.printStackTrace();
						}
						final String inviterr = inviter;
						// streaming link listener
						muc.addMessageListener(new PacketListener() {
							@Override
							public void processPacket(Packet packet) {
								Message message = (Message) packet;
								if (message.getBody() != null) {
									Log.i("INVITATION-MULTI-ROOM RECEIVE MESSAGE:",
											"Text Recieved "
													+ message.getBody()
													+ " from "
													+ message.getFrom());
									final String msg = message.getBody()
											.toString();
									mHandler.post(new Runnable() {
										@SuppressLint("NewApi")
										public void run() {
											// notification or chat...
											if (msg.contains(streaminglinkTag))
												popupReceiveStreamingLinkMessage(
														inviterr, msg);
											else
												Toast.makeText(faActivity,msg,
														Toast.LENGTH_SHORT)
														.show();
										}
									});

								}
							}

						});
					}
				});
	}

	private void stopStream() {
		if (mClient != null) {
			mClient.release();
			mClient.stopStream();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession = null;
		}

	}

	/** Time Thread */
	private class CurrentTimeThread extends Thread {
		@Override
		public void run() {
			do {
				try {
					Thread.sleep(1000);
					android.os.Message msg = new android.os.Message();
					msg.what = DISPLAY;
					mHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (alive);
			if (!alive) {
				android.os.Message msg = new android.os.Message();
				msg.what = 2;
				mHandler.sendMessage(msg);
			}
		}

		@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
		private Handler mHandler = new Handler() {

			@Override
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case DISPLAY:
					String curDateTime = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(Calendar
							.getInstance().getTime());
					mTime.setText(curDateTime);
					break;
				case 2:
					mTime.setText("");
					break;
				default:
					break;
				}
			}
		};
	}

	private Handler mHandler = new Handler();
	private EditText textMessage;
	private static Button btn_Send;
	private ListView friendlistView;
	private PopupWindow popFriends;
	private static PopupWindow popStreamingLink;
	private FriendsAdapter friendsAdapter;
	private ArrayList<String> selectedListMap;

	// Select Fiends to Share the video
	@SuppressWarnings("deprecation")
	private void popupContactList(/* String entries */) {

		final View v = faActivity.getLayoutInflater().inflate(
				R.layout.friendlist, null, false);
		int h = faActivity.getWindowManager().getDefaultDisplay().getHeight();
		int w = faActivity.getWindowManager().getDefaultDisplay().getWidth();

		popFriends = new PopupWindow(v, w - 10, (int) (((3.52) * h) / 4));
		popFriends.setAnimationStyle(R.style.MyDialogStyleBottom);
		popFriends.setFocusable(true);
		popFriends.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				popFriends.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}
		}, 1000L);

		selectedListMap = new ArrayList<String>();
		friendlistView = (ListView) v.findViewById(R.id.friendlist);
		friendlistView.setItemsCanFocus(true);
		friendsAdapter = new FriendsAdapter(faActivity, friendList);

		friendlistView.setAdapter(friendsAdapter);
		friendlistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {

				CheckBox checkbox = (CheckBox) v.findViewById(R.id.check_box);
				checkbox.toggle();

				friendsAdapter.getIsSelected().put(position,
						checkbox.isChecked());

				if (checkbox.isChecked()) {
					Log.i("----------",friendList.get(position)
							.get("username"));
					selectedListMap.add(friendList.get(position)
							.get("username"));

				}
			}
		});

		btn_Send = (Button) v.findViewById(R.id.btn_play);
		btn_Send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				curDateTime = new SimpleDateFormat("yyyy_MMdd_HHmmss")
						.format(Calendar.getInstance().getTime());
				
				// generate a chat room according current time
				room = "room" + curDateTime;
				mRoom.setChatRoom(room);
				Log.i("MULTIROOM-ROOM", room);
		
				/** room message listening in back-end while video is playing */
				mRoom.RoomMsgListenerConnection(connection, mRoom.getChatRoom());
				/** draw circle on screen according the coordination */
				PAINTViewRoomMsgListener(connection, mRoom.getChatRoom());
				// START TO PUSH VIDEO
				PLAYVideoStreaming(room);

				
				// get the streaming link
				Log.i("VideoStreamingFragment:IP",mAddress+":"+mPort);
				
				streaminglinkTag = "rtsp://"+ mAddress +":"+ mPort +"/";				
				streaminglink = streaminglinkTag + room + ".sdp";
				
				
				if (popFriends != null)
					popFriends.dismiss();
				// SEND VIDOE NOTIFICATION TO SELECTED FRIENDS
				mHandler.post(new Runnable() {
					public void run() {
						Message msg = new Message(room + "@conference.myria",
								Message.Type.groupchat);
						msg.setBody(streaminglink);
						if (!connection.isConnected()) {
							Log.i("1-SECOND-CREATEROOM_BUG",
									"connection is dis-Connected");
							try {
								connection.connect();
								Presence presence = new Presence(
										Presence.Type.available);
								connection.sendPacket(presence);
							} catch (XMPPException e1) {
								e1.printStackTrace();
							}
						}
						connection.sendPacket(msg);
					}
				});
				// CREATE CHAT ROOM AND INVITE SELECTED FRIENDS TO JOIN
				if (selectedListMap.size() > 0) {
					if (!connection.isConnected()) {
						Log.i("2-SECOND-CREATEROOM_BUG", "connection == null!");
						try {
							connection.connect();
						} catch (XMPPException e) {
							e.printStackTrace();
						}
					}
					try {
	
						//TODO 2: require password >>>>>>>>>>>>>>>>>>>>.
						if (mRoom.createMultiUserRoom(connection, room, "1234"))
							Log.i("CREATEROOM", "require password >>>>>>>>>>>>>>>>>>>>.");
						if (mRoom.inviteToChatRoom(connection, room, selectedListMap, "1234"))
							Log.i("INVITEROOM", "require password >>>>>>>>>>>>>>>>>>>>.");
						
//						
//						if (mRoom.createMultiUserRoom(connection, room))
//							Log.i("CREATEROOM", "success!");
//						if (mRoom.inviteToChatRoom(connection, room, selectedListMap))
//							Log.i("INVITEROOM", "success!");

					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
				
				/*** send store video command to server */
//				mRoom.storeMediaStream(room);

			}
		});
		Button btn_send_cancel = (Button) v.findViewById(R.id.btn_send_cancel);
		btn_send_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				popFriends.dismiss();
			}
		});
	}

	/*** draw circle on surfaceView accodring to the coordinate */
	private void PAINTViewRoomMsgListener(XMPPConnection connection,
			String roomName) {

		if (!connection.isConnected()) {
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		// Add a packet listener to get messages sent to us
		MultiUserChat muc = new MultiUserChat(connection, roomName
				+ "@conference.myria");
		muc.addMessageListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
				Log.i("PAINTViewRoomMsgListener ", message.getFrom() + ":"
						+ message.getBody());
				// room3@conference.myria/admin@myria/Smack-owner:dggjjk
				final String[] fromName = message.getFrom().split("/");
				final String msg = message.getBody().toString();
				mHandler.post(new Runnable() {
					@SuppressLint("NewApi")
					public void run() {
						// notification or chat...
						if (msg.contains("PaintView")) {
							String[] coordination = msg.split(",");
							// Toast.makeText(getApplicationContext(),fromName[1]+
							// ": (" + coordination[1]+","+coordination[2]+")",
							// Toast.LENGTH_SHORT).show();
							/** REDRAW CIRCLE according to the Coordinate */
							paintThread.setBubble(Float.parseFloat(coordination[1]),Float.parseFloat(coordination[2]));
							Log.i("VideoStreamingFragment-REDRAW",coordination[1] + "," + coordination[2]);
						} else
							Toast.makeText(faActivity,
									fromName[1] + ": " + msg,
									Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	// receive video streaming link listener
	@SuppressWarnings("deprecation")
	public void popupReceiveStreamingLinkMessage(String inviter, String message) {

		final View v = faActivity.getLayoutInflater().inflate(
				R.layout.streaminglink, null, false);

		int h = faActivity.getWindowManager().getDefaultDisplay().getHeight();
		int w = faActivity.getWindowManager().getDefaultDisplay().getWidth();

		popStreamingLink = new PopupWindow(v, w - 10, 1 * h / 4);
		popStreamingLink.setAnimationStyle(R.style.MyDialogStyleBottom);
		popStreamingLink.setFocusable(true);
		popStreamingLink.setBackgroundDrawable(new BitmapDrawable());
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				popStreamingLink.showAtLocation(v, Gravity.BOTTOM, 0, 0);
			}
		}, 1000L);

		TextView stramingSender = (TextView) v
				.findViewById(R.id.streaming_sender);
		stramingSender.setText("invitation from: " + inviter);

		TextView stramingLink = (TextView) v.findViewById(R.id.streaming_link);
		String[] subject = message.split("room"); //TODO
		final String receiveStreaming = message.toString();
		Log.i("VideoStreamingFragment","receiveStreaming:"+ receiveStreaming);
		stramingLink.setText("subject: " + subject[1]);
		btn_Send = (Button) v.findViewById(R.id.btn_play_streaming);
		btn_Send.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				/** DO PLAYING */
				/* Start this in a new thread as to not block the UI thread */
				VLCCallbackTask task = new VLCCallbackTask(faActivity) {
					@Override
					public void run() {
						AudioServiceController audioServiceController = AudioServiceController
								.getInstance();
						// use audio as default player...
						audioServiceController.load(receiveStreaming, false);
					}
				};
				task.execute();

				popStreamingLink.dismiss();
			}
		});
		Button btn_cancel = (Button) v.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				popStreamingLink.dismiss();
			}
		});
	}

	// get all friends
	private List<Map<String, String>> getAllFriendsUser(
			XMPPConnection connection) {
		if (!connection.isConnected()) {
			Log.i("SECOND_GETUSERS", "connection=null");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}

		friendList = new ArrayList<Map<String, String>>();
		Roster roster11 = connection.getRoster();
		Collection<RosterEntry> entries11 = roster11.getEntries();

		for (RosterEntry entry : entries11) {
			Presence presence = roster11.getPresence(entry.getUser());
			Map<String, String> map = new HashMap<String, String>();
			if (presence.isAvailable()) {
				map.put("status", "online");
				Log.i("VideoStreaming", entry.getUser() + "--online");
			} else {
				map.put("status", "offline");
				Log.i("VideoStreaming", entry.getUser() + "--offline");
			}

			map.put("name", entry.getName());
			map.put("username", entry.getUser());

			friendList.add(map);
		}

		return friendList;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopStream();
		mRoom.userOffline(connection);
	}

	@Override
	public void onPause() {
		super.onPause();
		stopStream();
	}

	/**
	 * [solved problem] connection disconnected problem, when back to streaming
	 * Fragment from VideoPlayer]
	 * */
	@Override
	public void onResume() {
		super.onResume();
		// check for after videoPlaying back to streamingFragment
		if (!connection.isConnected()) {
			Log.i("0-SECOND-CREATEROOM_BUG", "connection == null!");
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}

	private void setStateDescription(byte state) {

		switch (state) {
		case EasyCameraApp.STATE_DISCONNECTED:
			// ipView.setText(null);
			break;
		case EasyCameraApp.STATE_CONNECTED:
			// ipView.setText(String.format(
			// "Input this URL in VLC player:\nrtsp://%s:%d/%s.sdp",
			// mAddress, mPort, mVideoName));
			break;
		case EasyCameraApp.STATE_CONNECTING:
			// ipView.setText(null);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBitrareUpdate(long bitrate) {
		if (mClient != null) {
			if (bitrate / 1000 < 200)
				ipView.setText(" The current network is not stable !  "
						+ bitrate / 1000 + " kbps");
			else{
				ipView.setText("");
			}
				
//			if (bitrate / 1000 < 150)
//				// stop streaming and destroy chat room
//				ipView.setText("Cannot streaming because of unstable network  " + bitrate / 1000 + " kbps");
		}
	}

	@Override
	public void onRtspUpdate(int message, Exception exception) {
		if (message == RtpThread.WHAT_THREAD_END_UNEXCEPTION) {
			faActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					btnSelectContact.setBackgroundResource(R.drawable.pause);
					alive = true;
					stopStream();
					// ipView.setText("Disconnect with server and stop transfer");

				}
			});
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mReceiver != null) {
			LocalBroadcastManager.getInstance(faActivity).unregisterReceiver(
					mReceiver);
			mReceiver = null;
		}
		if (mClient != null) {
			mClient.release();
			mClient.stopStream();
			mClient = null;
		}

		if (mSession != null) {
			mSession.release();
			mSession.stop();
			mSession = null;
		}
	}

	@Override
	public void surfaceCreated(final SurfaceHolder holder) {
		// Configures the SessionBuilder
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (EasyCameraApp.ACTION_COMMOND_STATE_CHANGED.equals(intent
						.getAction())) {
					byte state = intent.getByteExtra(EasyCameraApp.KEY_STATE,
							EasyCameraApp.STATE_DISCONNECTED);
					// setStateDescription(state);
					if (state == EasyCameraApp.STATE_CONNECTED) {
						// ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
						// mAddress, Integer.parseInt(mPort), mVideoName));
					}

				} else {
					if (intent.getAction().equals("REDIRECT")) {
						String location = intent.getStringExtra("location");
						if (!TextUtils.isEmpty(location)) {
							// ======================
						}
					} else if (intent.getAction().equals("PAUSE")) {
						// ==========================
					} else if (ConnectivityManager.CONNECTIVITY_ACTION
							.equals(intent.getAction())) {
						boolean success = false;
						// get the network connection
						ConnectivityManager connManager = (ConnectivityManager) faActivity
								.getSystemService(faActivity.CONNECTIVITY_SERVICE);
						State state = connManager.getNetworkInfo(
								ConnectivityManager.TYPE_WIFI).getState();
						if (State.CONNECTED == state) {
							success = true;
						}
						state = connManager.getNetworkInfo(
								ConnectivityManager.TYPE_MOBILE).getState();
						if (State.CONNECTED != state) {
							success = true;
						}
						if (success) {
							// startService(new Intent(MainActivity.this,
							// CommandService.class));
							// ipView.setText(String.format("rtsp://%s:%d/%s.sdp",
							// mAddress, Integer.parseInt(mPort),
							// mVideoName));
						}
					}
				}
			}

		};

		ConnectivityManager cm = (ConnectivityManager) faActivity
				.getSystemService(faActivity.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(faActivity);

			mAddress = pref.getString("key_server_address", null);
			mPort = pref.getString("key_server_port", null);
			mVideoName = pref.getString("key_device_id", null);
			boolean bParamInvalid = (TextUtils.isEmpty(mAddress)
					|| TextUtils.isEmpty(mPort) || TextUtils
					.isEmpty(mVideoName));
			if (!bParamInvalid) {
				// startService(new Intent(this, CommandService.class));
				//
				// IntentFilter inf = new
				// IntentFilter(EasyCameraApp.ACTION_COMMOND_STATE_CHANGED);
				// inf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
				// inf.addAction("REDIRECT");
				// inf.addAction("PAUSE");
				// LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mReceiver,
				// inf);
				// setStateDescription(EasyCameraApp.sState);
			}
		} else {
			// ipView.setText("Network is unavailable,please open the network and try again");
		}

	}

	public String getDefaultDeviceId() {
		return Build.MODEL.replaceAll(" ", "_");
	}

	/**
	 * Configure the provider manager
	 * 
	 * @param pm
	 */
	public void configureProviderManager(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());
		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
			Log.w("TestClient",
					"Can't load class for org.jivesoftware.smackx.packet.Time");
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());
		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());
		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void onSessionError(int reason, int streamType, Exception e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPreviewStarted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSessionConfigured() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSessionStarted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSessionStopped() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// nothing to do
		} else {
			// nothing to do
		}
	}


	/** Touch Event*/
	private final SurfaceHolder.Callback paintViewCallback = new android.view.SurfaceHolder.Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i("VideoStreamingFragment", "surface changed");
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			paintThread = new PaintThread(paintViewHolder);
			paintThread.setRunning(true);
//			paintThread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("VideoStreamingFragment2",
					"paintThread status::"+ paintThread.isAlive());
//			stopPaintThread();
		}
	};

	/** Paint thread: to draw a circle in a thread*/
	class PaintThread extends Thread {
		private boolean run = false;
		private float bubbleX = -100;
		private float bubbleY = -100;

		public PaintThread(SurfaceHolder surfaceHolder) {
			paintViewHolder = surfaceHolder;
		}

		protected void setBubble(float x, float y) {
			//[Sender] --> [Receiver]
			// because there's an 90 degree anti-clockwise rotation of the video,
			// so switch x and y coordinate
			// (x,y)-->(y,videoWidthX-x)
			// do not revise at here, do it in Receiver send-message function
			bubbleX = x;
			bubbleY = y;
		}

		public void setRunning(boolean b) {
			run = b;
		}

		public void run() {
			while (run) {
				Canvas c = null;
				try {// draw the circle on screen
					c = paintViewHolder.lockCanvas(null);
					synchronized (paintViewHolder) {
						/**IMPORTANT: clear old circle */
						//FIXME: clear paint only paint happened
						mPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
						c.drawPaint(mPaint);
						mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));

						// draw new circle
						c.drawColor(Color.TRANSPARENT);
						c.drawCircle(bubbleX, bubbleY, 50, mPaint);
					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} finally {
					if (c != null) {
						paintViewHolder.unlockCanvasAndPost(c);
					}
				}

			}
		}

	}

	public PaintThread getThread() {
		return paintThread;
	}
	
	public void stopPaintThread(){
		boolean retry = true;
		paintThread.setRunning(false);
//		while (retry) {
			try {
				paintThread.join();
				Log.i("VideoStreamingFragment3",
						"paintThread status::"+ paintThread.isAlive());
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
	}
	
	
	// touch listener
	private final OnTouchListener paintViewTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent e) {

			float touchX = e.getX();
			float touchY = e.getY();

			switch (e.getAction()) {

			case MotionEvent.ACTION_DOWN:
				
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
				
				paintThread.setBubble(touchX, touchY);
				/** send message */
				String coordinateMsg = "PaintView,"
						+ Float.toString(touchX) + ","
						+ Float.toString(touchY);
				mRoom.SendMessage(connection, room, coordinateMsg);

				String coordinate = Float.toString(touchX) + ","
						+ Float.toString(touchY);
				
		        /** store these data
		        [connection.getUser(), timestamp, (xTouch, yTouch), tag]*/
				// pop up annotation window to add some annotation
				// and Store touch info
				mRoom.touchAnnotation(connection, room, timestamp, coordinate);
				
				Log.i("VideoStreamingFragment", Float.toString(touchX)
						+ "," + Float.toString(touchY));
				break;
			}
			return true;
		}
	};

}
