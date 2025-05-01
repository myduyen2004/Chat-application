package com.chat.websocket;

import com.chat.dao.MessageDAO;
import com.chat.dao.UserDAO;
import com.chat.model.Message;
import com.chat.model.User;

import javax.json.*;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.server.PathParam;

@ServerEndpoint("/chat/{userId}")
public class ChatEndpoint {
    private static final Logger LOGGER = Logger.getLogger(ChatEndpoint.class.getName());
    private static final Set<ChatEndpoint> chatEndpoints = new CopyOnWriteArraySet<>();
    private static final HashMap<String, Integer> users = new HashMap<>();
    private Session session;
    private int userId;
    private MessageDAO messageDAO = new MessageDAO();
    private UserDAO userDAO = new UserDAO();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdParam) {
        try {
            this.session = session;
            this.userId = Integer.parseInt(userIdParam);
            chatEndpoints.add(this);
            users.put(session.getId(), userId);

            // Thiết lập timeout cho phiên
            session.setMaxIdleTimeout(600000); // 10 phút

            // Gửi thông báo cho tất cả người dùng về người mới tham gia
            User user = userDAO.getUserById(userId);
            if (user != null) {
                JsonObject message = Json.createObjectBuilder()
                        .add("type", "join")
                        .add("userId", userId)
                        .add("username", user.getUsername())
                        .add("displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                        .build();

                broadcast(message.toString());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in onOpen: " + e.getMessage(), e);
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonReader reader = Json.createReader(new StringReader(message));
            JsonObject jsonMessage = reader.readObject();
            reader.close();

            String type = jsonMessage.getString("type");

            if ("message".equals(type)) {
                // Xử lý tin nhắn thông thường
                String content = jsonMessage.getString("content");
                Integer repliedToId = null;

                // Fix for the ClassCastException
                if (jsonMessage.containsKey("repliedToId") &&
                        jsonMessage.get("repliedToId").getValueType() == JsonValue.ValueType.NUMBER) {
                    repliedToId = jsonMessage.getJsonNumber("repliedToId").intValue();
                }

                boolean isSticker = jsonMessage.getBoolean("isSticker", false);
                String attachmentUrl = null;
                if (jsonMessage.containsKey("attachmentUrl") &&
                        !jsonMessage.isNull("attachmentUrl")) {
                    attachmentUrl = jsonMessage.getString("attachmentUrl");
                }

                User sender = userDAO.getUserById(userId);
                String senderName = sender.getDisplayName() != null ? sender.getDisplayName() : sender.getUsername();

                Message chatMessage = new Message(userId, senderName, content, repliedToId, isSticker, attachmentUrl);

                // Lưu vào database
                if (messageDAO.saveMessage(chatMessage)) {
                    // Nếu có reply, lấy nội dung tin nhắn được reply
                    if (repliedToId != null) {
                        Message repliedMessage = messageDAO.getMessageById(repliedToId);
                        if (repliedMessage != null) {
                            chatMessage.setRepliedContent(repliedMessage.getContent());
                        }
                    }

                    // Tạo JSON để gửi lại cho tất cả người dùng
                    JsonObjectBuilder responseBuilder = Json.createObjectBuilder()
                            .add("type", "message")
                            .add("messageId", chatMessage.getMessageId())
                            .add("senderId", chatMessage.getSenderId())
                            .add("senderName", chatMessage.getSenderName())
                            .add("content", chatMessage.getContent())
                            .add("sentTime", chatMessage.getSentTime().getTime())
                            .add("isSticker", chatMessage.isSticker());

                    // Add optional fields safely
                    if (repliedToId != null) {
                        responseBuilder.add("repliedToId", repliedToId);
                    } else {
                        responseBuilder.add("repliedToId", JsonValue.NULL);
                    }

                    responseBuilder.add("repliedContent",
                            chatMessage.getRepliedContent() != null ? chatMessage.getRepliedContent() : "");

                    responseBuilder.add("attachmentUrl",
                            chatMessage.getAttachmentUrl() != null ? chatMessage.getAttachmentUrl() : "");

                    JsonObject responseJson = responseBuilder.build();
                    broadcast(responseJson.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing message: " + e.getMessage(), e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        try {
            chatEndpoints.remove(this);
            Integer userId = users.get(session.getId());
            users.remove(session.getId());

            if (userId != null) {
                // Thông báo người dùng đã rời đi
                User user = userDAO.getUserById(userId);
                if (user != null) {
                    JsonObject message = Json.createObjectBuilder()
                            .add("type", "leave")
                            .add("userId", userId)
                            .add("username", user.getUsername())
                            .add("displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                            .build();

                    broadcast(message.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in onClose: " + e.getMessage(), e);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "WebSocket error: " + throwable.getMessage(), throwable);

        try {
            // Dọn dẹp kết nối
            chatEndpoints.remove(this);
            Integer userId = users.get(session.getId());
            users.remove(session.getId());

            // Đóng phiên nếu vẫn mở
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error closing session: " + e.getMessage(), e);
        }
    }

    /**
     * Gửi tin nhắn an toàn đến tất cả các kết nối
     */
    private static void broadcast(String message) {
        Set<ChatEndpoint> endpointsToRemove = new CopyOnWriteArraySet<>();

        for (ChatEndpoint endpoint : chatEndpoints) {
            try {
                // Kiểm tra xem phiên có mở không trước khi gửi
                if (endpoint.session.isOpen()) {
                    synchronized (endpoint.session) {
                        endpoint.session.getBasicRemote().sendText(message);
                    }
                } else {
                    // Phiên đã đóng, đánh dấu để xóa
                    endpointsToRemove.add(endpoint);
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Lỗi gửi tin nhắn: " + e.getMessage());
                // Kết nối có thể đã bị hỏng, đánh dấu để xóa
                endpointsToRemove.add(endpoint);
            } catch (IllegalStateException e) {
                LOGGER.log(Level.WARNING, "WebSocket trong trạng thái không hợp lệ: " + e.getMessage());
                // Có thể thử gửi tin nhắn không đồng bộ thay thế
                try {
                    if (endpoint.session.isOpen()) {
                        endpoint.session.getAsyncRemote().sendText(message);
                    } else {
                        endpointsToRemove.add(endpoint);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Lỗi gửi tin nhắn không đồng bộ: " + ex.getMessage());
                    endpointsToRemove.add(endpoint);
                }
            }
        }

        // Xóa các kết nối đã bị đánh dấu
        if (!endpointsToRemove.isEmpty()) {
            chatEndpoints.removeAll(endpointsToRemove);
        }
    }

    /**
     * Phương thức broadcast sử dụng gửi tin nhắn không đồng bộ
     * Sử dụng phương thức này thay thế cho broadcast thông thường nếu có vấn đề về hiệu suất
     */
    private static void broadcastAsync(String message) {
        Set<ChatEndpoint> endpointsToRemove = new CopyOnWriteArraySet<>();

        for (ChatEndpoint endpoint : chatEndpoints) {
            try {
                if (endpoint.session.isOpen()) {
                    endpoint.session.getAsyncRemote().sendText(message);
                } else {
                    endpointsToRemove.add(endpoint);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Lỗi gửi tin nhắn không đồng bộ: " + e.getMessage());
                endpointsToRemove.add(endpoint);
            }
        }

        // Xóa các kết nối đã bị đánh dấu
        if (!endpointsToRemove.isEmpty()) {
            chatEndpoints.removeAll(endpointsToRemove);
        }
    }
}