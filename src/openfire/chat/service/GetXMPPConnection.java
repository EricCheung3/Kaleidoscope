package openfire.chat.service;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

public class GetXMPPConnection {

	public XMPPConnection connection;
	public String username;
	public String password;

	public GetXMPPConnection(String username, String password) {
		this.username = username;
		this.password = password;

		connection = getXMPPConnection(username, password);
	}


	public XMPPConnection getXMPPConnection(String username, String password){
		try {
			if (null == connection || !connection.isAuthenticated()) {
				XMPPConnection.DEBUG_ENABLED = true;

				ConnectionConfiguration config = new ConnectionConfiguration(
						UserServiceImpl.SERVER_HOST,
						UserServiceImpl.SERVER_PORT,
						UserServiceImpl.SERVER_NAME);
				config.setReconnectionAllowed(true);
				config.setSendPresence(true);
				config.setSASLAuthenticationEnabled(true);
				connection = new XMPPConnection(config);
				connection.connect();
				connection.login(username, password);
				// Set the status to available
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				// get message listener
//				ReceiveMsgListenerConnection(connection);

			}
			return connection;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return null;
	}

}
