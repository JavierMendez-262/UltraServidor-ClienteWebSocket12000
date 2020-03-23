/*
 * ServerEndpointAnnotated.java
 * 
 * Creado en Marzo 23, 2020. 01:43.
 */
package websockets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author JavierMéndez 00000181816
 */
@ServerEndpoint("/chatroom")
public class ServerEndpointAnnotated {

    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connection Opened...");
        clients.add(session);
        try {
            for (Session client : clients) {
                if (!client.equals(session)) {
                    client.getBasicRemote().sendText("Client " + session.getId() + " has connected to the chat!");
                    client.getBasicRemote().sendText(clientsToString());
                }
            }
            session.getBasicRemote().sendText(clientsToString());
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        String[] messageAddressee = new String[2];

        messageAddressee[0] = message.substring(0, message.lastIndexOf("|"));
        messageAddressee[1] = message.substring(message.lastIndexOf("|") + 1);

        System.out.printf("%-100s; %30s, %-50s\n", "Message received: " + messageAddressee[0], "From: Client " + session.getId(), "To: " + messageAddressee[1]);

        String echoMsg = messageAddressee[0];

        synchronized (clients) {
            if (messageAddressee[1].toLowerCase().equals("all")) {
                for (Session client : clients) {
                    if (!client.equals(session)) {
                        try {
                            client.getBasicRemote().sendText("Client " + session.getId() + ": " + echoMsg);
                        } catch (IOException ex) {
                            System.out.println(ex);
                        }
                    }
                }
            } else {
                String[] addressees = messageAddressee[1].replace(" ", "").split(",");
                for (Session client : clients) {
                    for (String addressee : addressees) {
                        if (client.getId().equals(addressee)) {
                            try {
                                client.getBasicRemote().sendText("Client " + session.getId() + " (Whispers): " + echoMsg);
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }
                        }
                    }
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Connection Closed...");
        clients.remove(session);

        for (Session client : clients) {
            try {
                client.getBasicRemote().sendText("Client " + session.getId() + " has discconnected from the chat!");
                client.getBasicRemote().sendText(clientsToString());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    @OnError
    public void onError(Throwable e
    ) {
        e.printStackTrace();
    }

    private String clientsToString() {
        String collectionToString = "Clients: {";

        for (Session client : clients) {
            collectionToString += client.getId() + ", ";
        }
        collectionToString = collectionToString.substring(0, collectionToString.length() - 2) + "}";

        return collectionToString;
    }
}
