<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.chat.model.User" %>
<%@ page import="com.chat.dao.MessageDAO" %>
<%@ page import="com.chat.model.Message" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect("login");
        return;
    }

    MessageDAO messageDAO = new MessageDAO();
    // Lấy tin nhắn gần đây, đảm bảo thứ tự từ cũ đến mới
    List<Message> recentMessages = messageDAO.getRecentMessages(50);
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chat Room</title>
    <link rel="stylesheet" href="css/style.css">
    <script>
        let userId = <%= currentUser.getUserId() %>;
        let replyingTo = null;
    </script>
    <style>
        /* CSS đảm bảo hiển thị đúng thứ tự tin nhắn */
        .chat-messages {
            display: flex;
            flex-direction: column;
            overflow-y: auto;
            height: 70vh;
        }
    </style>
</head>
<body>
<div class="chat-container">
    <div class="chat-header">
        <h2>Chat Room</h2>
        <div class="user-info">
            Xin chào, <%= currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getUsername() %>
            <a href="login?logout=true" class="btn-small">Đăng xuất</a>
        </div>
    </div>

    <div class="chat-messages" id="chatMessages">
        <% for (int i = 0; i < recentMessages.size(); i++) {
            Message msg = recentMessages.get(i);
            boolean isOwnMessage = msg.getSenderId() == currentUser.getUserId();
            String messageClass = isOwnMessage ? "message own-message" : "message other-message";
        %>
        <div class="<%= messageClass %>" data-message-id="<%= msg.getMessageId() %>">
            <div class="message-header">
                <span class="sender-name"><%= msg.getSenderName() %></span>
                <span class="message-time"><%= sdf.format(msg.getSentTime()) %></span>
            </div>

            <% if (msg.getRepliedToId() != null) { %>
            <div class="replied-message">
                <div class="replied-content"><%= msg.getRepliedContent() %></div>
            </div>
            <% } %>

            <% if (msg.isSticker()) { %>
            <div class="message-sticker">
                <img src="<%= msg.getContent() %>" alt="Sticker">
            </div>
            <% } else { %>
            <div class="message-content"><%= msg.getContent() %></div>
            <% } %>

            <% if (msg.getAttachmentUrl() != null && !msg.getAttachmentUrl().isEmpty()) { %>
            <div class="message-attachment">
                <a href="<%= msg.getAttachmentUrl() %>" target="_blank">Tệp đính kèm</a>
            </div>
            <% } %>

            <div class="message-actions">
                <button class="btn-reply" onclick="replyToMessage(<%= msg.getMessageId() %>, '<%= msg.getContent().replace("'", "\\'") %>')">Reply</button>
            </div>
        </div>
        <% } %>
    </div>

    <div id="replyPreview" class="reply-preview" style="display: none;">
        <div class="reply-content" id="replyContent"></div>
        <button class="btn-cancel-reply" onclick="cancelReply()">×</button>
    </div>

    <div class="chat-input">
        <div class="sticker-panel">
            <button class="btn-sticker" onclick="toggleStickerPanel()">😊</button>
            <div class="stickers" id="stickerPanel" style="display: none;">
                <div class="sticker" onclick="sendSticker('/stickers/smile.png')">😊</div>
                <div class="sticker" onclick="sendSticker('/stickers/laugh.png')">😂</div>
                <div class="sticker" onclick="sendSticker('/stickers/sad.png')">😢</div>
                <div class="sticker" onclick="sendSticker('/stickers/love.png')">❤️</div>
            </div>
        </div>

        <div class="file-upload">
            <label for="fileInput" class="btn-file">📎</label>
            <input type="file" id="fileInput" style="display: none;">
            <span id="uploadStatus"></span>
        </div>

        <input type="text" id="messageInput" placeholder="Nhập tin nhắn...">
        <button id="sendButton" onclick="sendMessage()">Gửi</button>
    </div>
</div>

<script src="js/chat.js"></script>
<script>
    // Auto-scroll to bottom after loading messages
    window.addEventListener('DOMContentLoaded', () => {
        const chatMessages = document.getElementById('chatMessages');
        chatMessages.scrollTop = chatMessages.scrollHeight;
    });
</script>
</body>
</html>