package com.chatapp.dao;

import com.chatapp.model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class MessageDAO {
    private DataSource dataSource = DatabaseConfig.getDataSource();

    public void saveMessage(Message message) {
        String sql = "INSERT INTO Messages (sender_id, content, attachment_path, is_sticker, sticker_id, reply_to_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getSenderId());
            stmt.setString(2, message.getContent());
            stmt.setString(3, message.getAttachmentPath());
            stmt.setBoolean(4, message.isSticker());
            stmt.setObject(5, message.getStickerId(), Types.INTEGER);
            stmt.setObject(6, message.getReplyToId(), Types.INTEGER);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    message.setMessageId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username as sender_name, rm.content as reply_content " +
                "FROM Messages m " +
                "JOIN Users u ON m.sender_id = u.user_id " +
                "LEFT JOIN Messages rm ON m.reply_to_id = rm.message_id " +
                "ORDER BY m.created_at";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(rs.getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setSender(rs.getString("sender_name"));
                message.setContent(rs.getString("content"));
                message.setAttachmentPath(rs.getString("attachment_path"));
                message.setSticker(rs.getBoolean("is_sticker"));
                message.setStickerId(rs.getInt("sticker_id"));
                message.setReplyToId(rs.getInt("reply_to_id"));
                message.setReplyToContent(rs.getString("reply_content"));
                message.setCreatedAt(rs.getTimestamp("created_at"));

                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    public Message getMessageById(int messageId) {
        String sql = "SELECT m.*, u.username as sender_name, rm.content as reply_content " +
                "FROM Messages m " +
                "JOIN Users u ON m.sender_id = u.user_id " +
                "LEFT JOIN Messages rm ON m.reply_to_id = rm.message_id " +
                "WHERE m.message_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message();
                    message.setMessageId(rs.getInt("message_id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setSender(rs.getString("sender_name"));
                    message.setContent(rs.getString("content"));
                    message.setAttachmentPath(rs.getString("attachment_path"));
                    message.setSticker(rs.getBoolean("is_sticker"));
                    message.setStickerId(rs.getInt("sticker_id"));
                    message.setReplyToId(rs.getInt("reply_to_id"));
                    message.setReplyToContent(rs.getString("reply_content"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));

                    return message;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}