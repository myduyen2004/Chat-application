let socket = null;
const connectWebSocket = () => {
    socket = new WebSocket(`ws://${window.location.host}/chat/${userId}`);

    socket.onopen = () => {
        console.log('WebSocket connection established');
    };

    socket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        handleIncomingMessage(message);
    };

    socket.onclose = () => {
        console.log('WebSocket connection closed');
        // Reconnect after a delay
        setTimeout(connectWebSocket, 3000);
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
};

// Connect on page load
document.addEventListener('DOMContentLoaded', connectWebSocket);

// Handle incoming messages
const handleIncomingMessage = (message) => {
    const chatMessages = document.getElementById('chatMessages');

    if (message.type === 'message') {
        // Create message element
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message');
        messageDiv.classList.add(message.senderId === userId ? 'own-message' : 'other-message');
        messageDiv.setAttribute('data-message-id', message.messageId);

        // Message header
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

        // Replied message if any
        if (message.repliedToId && message.repliedToId !== -1) {
            const repliedDiv = document.createElement('div');
            repliedDiv.classList.add('replied-message');

            const repliedContent = document.createElement('div');
            repliedContent.classList.add('replied-content');
            repliedContent.textContent = message.repliedContent;

            repliedDiv.appendChild(repliedContent);
            messageDiv.appendChild(repliedDiv);
        }

        // Message content
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

        // Attachment if any
        if (message.attachmentUrl && message.attachmentUrl !== '') {
            const attachmentDiv = document.createElement('div');
            attachmentDiv.classList.add('message-attachment');

            const link = document.createElement('a');
            link.href = message.attachmentUrl;
            link.textContent = 'Tệp đính kèm';
            link.target = '_blank';

            attachmentDiv.appendChild(link);
            messageDiv.appendChild(attachmentDiv);
        }

        // Message actions
        const actionsDiv = document.createElement('div');
        actionsDiv.classList.add('message-actions');

        const replyButton = document.createElement('button');
        replyButton.classList.add('btn-reply');
        replyButton.textContent = 'Reply';
        replyButton.onclick = () => replyToMessage(message.messageId, message.content);

        actionsDiv.appendChild(replyButton);
        messageDiv.appendChild(actionsDiv);

        // Add to chat
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    } else if (message.type === 'join') {
        // Display join notification
        const joinDiv = document.createElement('div');
        joinDiv.classList.add('system-message');
        joinDiv.textContent = `${message.displayName} đã tham gia cuộc trò chuyện`;
        chatMessages.appendChild(joinDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    } else if (message.type === 'leave') {
        // Display leave notification
        const leaveDiv = document.createElement('div');
        leaveDiv.classList.add('system-message');
        leaveDiv.textContent = `${message.displayName} đã rời cuộc trò chuyện`;
        chatMessages.appendChild(leaveDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
};

// Send message
const sendMessage = () => {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();

    if (content === '' && !currentAttachment) return;

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

    // Reset reply
    cancelReply();

    // Reset attachment
    currentAttachment = null;
    document.getElementById('uploadStatus').textContent = '';
};

// Reply to message
const replyToMessage = (messageId, content) => {
    replyingTo = messageId;

    const replyPreview = document.getElementById('replyPreview');
    const replyContent = document.getElementById('replyContent');

    replyContent.textContent = content;
    replyPreview.style.display = 'flex';

    // Focus on input
    document.getElementById('messageInput').focus();
};

// Cancel reply
const cancelReply = () => {
    replyingTo = null;
    const replyPreview = document.getElementById('replyPreview');
    replyPreview.style.display = 'none';
};

// Toggle sticker panel
const toggleStickerPanel = () => {
    const stickerPanel = document.getElementById('stickerPanel');
    stickerPanel.style.display = stickerPanel.style.display === 'none' ? 'grid' : 'none';
};

// Send sticker
const sendSticker = (stickerUrl) => {
    const message = {
        type: 'message',
        content: stickerUrl,
        isSticker: true,
        repliedToId: replyingTo
    };

    socket.send(JSON.stringify(message));

    // Hide sticker panel
    document.getElementById('stickerPanel').style.display = 'none';

    // Reset reply
    cancelReply();
};

// Handle file upload
let currentAttachment = null;

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

// Send message on Enter key
document.getElementById('messageInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});