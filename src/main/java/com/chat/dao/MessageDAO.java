package com.chat.dao;

import com.chat.model.Message;
import com.chat.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean saveMessage(Message message) {
        String query = "INSERT INTO Messages (sender_id, content, replied_to_id, is_sticker, attachment_url) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, message.getSenderId());
            ps.setString(2, message.getContent());

            if (message.getRepliedToId() != null) {
                ps.setInt(3, message.getRepliedToId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setBoolean(4, message.isSticker());
            ps.setString(5, message.getAttachmentUrl());

            int result = ps.executeUpdate();

            if (result > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    message.setMessageId(rs.getInt(1));
                }
                return true;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getRecentMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT m.*, u.username, u.display_name, " +
                "rm.content as replied_content " +
                "FROM Messages m " +
                "JOIN Users u ON m.sender_id = u.user_id " +
                "LEFT JOIN Messages rm ON m.replied_to_id = rm.message_id " +
                "ORDER BY m.sent_time ASC " +
                "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setMessageId(rs.getInt("message_id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setSenderName(rs.getString("display_name") != null ?
                            rs.getString("display_name") : rs.getString("username"));
                    message.setContent(rs.getString("content"));
                    message.setSentTime(rs.getTimestamp("sent_time"));

                    Integer repliedToId = rs.getInt("replied_to_id");
                    if (!rs.wasNull()) {
                        message.setRepliedToId(repliedToId);
                        message.setRepliedContent(rs.getString("replied_content"));
                    }

                    message.setSticker(rs.getBoolean("is_sticker"));
                    message.setAttachmentUrl(rs.getString("attachment_url"));

                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    public Message getMessageById(int messageId) {
        String query = "SELECT m.*, u.username, u.display_name " +
                "FROM Messages m " +
                "JOIN Users u ON m.sender_id = u.user_id " +
                "WHERE m.message_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, messageId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message();
                    message.setMessageId(rs.getInt("message_id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setSenderName(rs.getString("display_name") != null ?
                            rs.getString("display_name") : rs.getString("username"));
                    message.setContent(rs.getString("content"));
                    message.setSentTime(rs.getTimestamp("sent_time"));

                    Integer repliedToId = rs.getInt("replied_to_id");
                    if (!rs.wasNull()) {
                        message.setRepliedToId(repliedToId);
                        // Lấy nội dung tin nhắn được reply trong một truy vấn khác
                        message.setRepliedContent(getMessageContent(repliedToId));
                    }

                    message.setSticker(rs.getBoolean("is_sticker"));
                    message.setAttachmentUrl(rs.getString("attachment_url"));

                    return message;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getMessageContent(int messageId) {
        String query = "SELECT content FROM Messages WHERE message_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, messageId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}