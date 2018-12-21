import java.io.*;
import java.lang.Error;
import java.util.Collection;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.vcardtemp.VCardManager;

public class ChattyXMPPConnection
{
    public AbstractXMPPConnection connection;
    public ChattyXMPPConnection(String username, String password) throws XMPPException, SmackException, IOException, InterruptedException
    {
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setXmppDomain("localhost")
                .setHost("localhost")
                .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                .setPort(5222)
                .build();

        connection = new XMPPTCPConnection(config);
    }

    public Collection<RosterEntry> getFriend() throws Exception {
        Roster roster = Roster.getInstanceFor(connection);
        if(!roster.isLoaded()) {
            roster.reloadAndWait();;
        }
        System.out.println(roster.toString());
        return roster.getEntries();
    }

    public void disconnect() throws XMPPException, SmackException, IOException, InterruptedException {
        connection.disconnect();
    }
} 