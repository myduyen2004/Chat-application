let socket = null;
let currentAttachment = null;

const connectWebSocket = () => {
    socket = new WebSocket(`ws://${window.location.host}/chat-application/chat/${userId}`);

    socket.onopen = () => console.log('WebSocket connection established');

    socket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        handleIncomingMessage(message);
    };

    socket.onclose = () => {
        console.log('WebSocket connection closed');
        setTimeout(connectWebSocket, 3000);
    };

    socket.onerror = (error) => console.error('WebSocket error:', error);
};

document.addEventListener('DOMContentLoaded', connectWebSocket);

const handleIncomingMessage = (message) => {
    // Xử lý các loại tin nhắn khác nhau
    if (message.type === 'join' || message.type === 'leave') {
        // Xử lý thông báo tham gia/rời khỏi chat
        // Có thể hiển thị một thông báo hệ thống ở đây
        return;
    }

    // Xử lý tin nhắn thông thường
    const chatMessages = document.getElementById('chatMessages');

    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', message.senderId === userId ? 'own-message' : 'other-message');
    messageDiv.setAttribute('data-message-id', message.messageId);

    const headerDiv = document.createElement('div');
    headerDiv.classList.add('message-header');

    const senderSpan = document.createElement('span');
    senderSpan.classList.add('sender-name');
    senderSpan.textContent = message.senderName;

    const timeSpan = document.createElement('span');
    timeSpan.classList.add('message-time');
    timeSpan.textContent = new Date(message.sentTime).toLocaleString();

    headerDiv.appendChild(senderSpan);
    headerDiv.appendChild(timeSpan);
    messageDiv.appendChild(headerDiv);

    if (message.repliedToId && message.repliedToId !== -1) {
        const repliedDiv = document.createElement('div');
        repliedDiv.classList.add('replied-message');

        const repliedContent = document.createElement('div');
        repliedContent.classList.add('replied-content');
        repliedContent.textContent = message.repliedContent;

        repliedDiv.appendChild(repliedContent);
        messageDiv.appendChild(repliedDiv);
    }

    if (message.isSticker) {
        const stickerDiv = document.createElement('div');
        stickerDiv.classList.add('message-sticker');

        const img = document.createElement('img');
        img.src = message.content;
        img.alt = 'Sticker';

        stickerDiv.appendChild(img);
        messageDiv.appendChild(stickerDiv);
    } else {
        const contentDiv = document.createElement('div');
        contentDiv.classList.add('message-content');
        contentDiv.textContent = message.content;
        messageDiv.appendChild(contentDiv);
    }

    if (message.attachmentUrl) {
        const attachmentDiv = document.createElement('div');
        attachmentDiv.classList.add('message-attachment');

        const link = document.createElement('a');
        link.href = message.attachmentUrl;
        link.textContent = 'Tệp đính kèm';
        link.target = '_blank';

        attachmentDiv.appendChild(link);
        messageDiv.appendChild(attachmentDiv);
    }

    const actionsDiv = document.createElement('div');
    actionsDiv.classList.add('message-actions');

    const replyButton = document.createElement('button');
    replyButton.classList.add('btn-reply');
    replyButton.textContent = 'Reply';
    replyButton.onclick = () => replyToMessage(message.messageId, message.content);

    actionsDiv.appendChild(replyButton);
    messageDiv.appendChild(actionsDiv);

    // Thêm tin nhắn vào CUỐI danh sách tin nhắn
    chatMessages.appendChild(messageDiv);

    // Cuộn xuống dưới để hiển thị tin nhắn mới nhất
    chatMessages.scrollTop = chatMessages.scrollHeight;
};

const sendMessage = () => {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();
    if (!content && !currentAttachment) return;

    const message = {
        type: 'message',
        content: content,
        isSticker: false,
        repliedToId: replyingTo
    };

    if (currentAttachment) {
        message.attachmentUrl = currentAttachment;
    }

    socket.send(JSON.stringify(message));
    messageInput.value = '';
    cancelReply();
    currentAttachment = null;
    document.getElementById('uploadStatus').textContent = '';
};

const replyToMessage = (messageId, content) => {
    replyingTo = messageId;
    document.getElementById('replyContent').textContent = content;
    document.getElementById('replyPreview').style.display = 'flex';
    document.getElementById('messageInput').focus();
};

const cancelReply = () => {
    replyingTo = null;
    document.getElementById('replyPreview').style.display = 'none';
};

const toggleStickerPanel = () => {
    const stickerPanel = document.getElementById('stickerPanel');
    stickerPanel.style.display = stickerPanel.style.display === 'none' ? 'grid' : 'none';
};

const sendSticker = (stickerUrl) => {
    const message = {
        type: 'message',
        content: stickerUrl,
        isSticker: true,
        repliedToId: replyingTo
    };
    socket.send(JSON.stringify(message));
    document.getElementById('stickerPanel').style.display = 'none';
    cancelReply();
};

document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    const uploadStatus = document.getElementById('uploadStatus');
    uploadStatus.textContent = 'Đang tải lên...';

    fetch('upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                currentAttachment = data.fileUrl;
                uploadStatus.textContent = `Đã tải lên: ${data.fileName}`;
            } else {
                uploadStatus.textContent = `Lỗi: ${data.error}`;
            }
        })
        .catch(error => {
            console.error('Upload error:', error);
            uploadStatus.textContent = 'Lỗi tải lên';
        });
});

document.getElementById('messageInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});