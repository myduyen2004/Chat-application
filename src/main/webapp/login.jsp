<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Đăng nhập</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="container">
  <h1>Đăng nhập</h1>

  <% if(request.getParameter("registered") != null) { %>
  <div class="success-message">Đăng ký thành công! Vui lòng đăng nhập.</div>
  <% } %>

  <% if(request.getAttribute("error") != null) { %>
  <div class="error-message"><%= request.getAttribute("error") %></div>
  <% } %>

  <form action="login" method="post">
    <div class="form-group">
      <label for="username">Tên đăng nhập:</label>
      <input type="text" id="username" name="username" required>
    </div>
    <div class="form-group">
      <label for="password">Mật khẩu:</label>
      <input type="password" id="password" name="password" required>
    </div>
    <button type="submit" class="btn">Đăng nhập</button>
  </form>
  <p class="text-center">
    Chưa có tài khoản? <a href="register">Đăng ký ngay</a>
  </p>
</div>
</body>
</html>