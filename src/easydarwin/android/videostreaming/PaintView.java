package easydarwin.android.videostreaming;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class PaintView extends View implements OnTouchListener {

	private Paint mPaint;
	private float mX;
	private float mY;
	/** touch message listener*/
	private MultiRoom mRoom;
	private Handler mHandler = new Handler();
	
	public PaintView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		/** Initializing the variables */
		mPaint = new Paint();
		mX = mY = -100;
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Setting the color of the circle
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Style.STROKE);

		// Draw the circle at (x,y) with radius 60
		canvas.drawCircle(mX, mY, 60, mPaint);

		// Redraw the canvas
		invalidate();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mRoom = VideoStreamingFragment.mRoom;
		switch (event.getAction()) {
		// When user touches the screen
		case MotionEvent.ACTION_DOWN:
			// Getting X,Y coordinate
			mX = event.getX();
			mY = event.getY();

			SendMessage(VideoStreamingFragment.connection, mRoom.getChatRoom());
			//ReceiveMsgListenerConnection(VideoStreamingFragment.connection);
			break;
		}
		return true;
	}

	private void SendMessage(XMPPConnection connection, String room) {

		//check for after videoPlaying back to streamingFragment
		if(!connection.isConnected()){
			try {
				connection.connect();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}

		Message msg = new Message(room + "@conference.myria", Message.Type.groupchat);
		msg.setBody("PaintView," + Float.toString(mX) + "," + Float.toString(mY));

		connection.sendPacket(msg);
		
		Log.i("PAINTVIEW-SENDMSG", msg.getBody());

	}
	
	/*** draw circle on surfaceView */
	private void PAINTViewRoomMsgListener(XMPPConnection connection, String roomName) {

		if(!connection.isConnected()) {
			try {
				connection.connect();
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Add a packet listener to get messages sent to us
		MultiUserChat muc = new MultiUserChat(connection, roomName +"@conference.myria");
		muc.addMessageListener(new PacketListener() {  
            @Override  
            public void processPacket(Packet packet) {  
            	org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
                Log.i("PAINTViewRoomMsgListener ", message.getFrom() + ":" + message.getBody());
                //room3@conference.myria/admin@myria/Smack-owner:dggjjk
                final String[] fromName = message.getFrom().split("/");
                final String msg = message.getBody().toString();
                mHandler.post(new Runnable() {
					@SuppressLint("NewApi")
					public void run() {
						// notification or chat...	
						if(msg.contains("PaintView")){
							String[] coordination = msg.split(",");
							//Toast.makeText(getApplicationContext(),fromName[1]+ ": (" + coordination[1]+","+coordination[2]+")", Toast.LENGTH_SHORT).show();
							/** REDRAW CIRCLE according to the Coordinate*/ //(not test now)
//							thread.setBubble(Float.parseFloat(coordination[1]), Float.parseFloat(coordination[2]));
							
							Log.i("REDRAW============", coordination[1]+","+coordination[2]);
						}else{}
							//Toast.makeText(getApplicationContext(),fromName[1]+ ": " + msg, Toast.LENGTH_SHORT).show(); 
					}
				}); 
            }  
        });  
	}
	
}