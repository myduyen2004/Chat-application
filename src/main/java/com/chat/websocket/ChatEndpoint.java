package com.chat.websocket;

import com.chat.dao.MessageDao;
import com.chat.model.Message;
import com.google.gson.Gson;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/chat", configurator = WebSocketConfig.class)
public class ChatWebSocket {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Gson gson = new Gson();
    private static MessageDao messageDao = new MessageDao();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String messageJson, Session session) {
        try {
            Message message = gson.fromJson(messageJson, Message.class);
            messageDao.saveMessage(message);

            for (Session s : sessions) {
                if (s.isOpen()) {
                    s.getBasicRemote().sendText(messageJson);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageToAll(Message message) {
        String messageJson = gson.toJson(message);
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(messageJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}