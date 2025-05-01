<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Đăng ký</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
    <h1>Đăng ký tài khoản</h1>

    <% if(request.getAttribute("error") != null) { %>
    <div class="error-message"><%= request.getAttribute("error") %></div>
    <% } %>

    <form action="register" method="post">
        <div class="form-group">
            <label for="username">Tên đăng nhập:</label>
            <input type="text" id="username" name="username" required>
        </div>
        <div class="form-group">
            <label for="displayName">Tên hiển thị:</label>
            <input type="text" id="displayName" name="displayName">
        </div>
        <div class="form-group">
            <label for="password">Mật khẩu:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <label for="confirmPassword">Xác nhận mật khẩu:</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required>
        </div>
        <button type="submit" class="btn">Đăng ký</button>
    </form>
    <p class="text-center">
        Đã có tài khoản? <a href="login">Đăng nhập</a>
    </p>
</div>
</body>
</html>