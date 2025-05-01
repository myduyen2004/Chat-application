package com.chat.controller;

import com.chat.dao.UserDAO;
import com.chat.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check if user is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect("chat.jsp");
            return;
        }

        // Forward to registration page
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String displayName = request.getParameter("displayName");
        String avatarUrl = request.getParameter("avatarUrl");

        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and password are required");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Check if username already exists
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser != null) {
            request.setAttribute("error", "Username already exists");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // In production, use proper password hashing
        newUser.setDisplayName(displayName != null && !displayName.trim().isEmpty() ? displayName : username);
        newUser.setAvatarUrl(avatarUrl);

        boolean success = userDAO.createUser(newUser);

        if (success) {
            // Store user in session
            HttpSession session = request.getSession();
            session.setAttribute("user", newUser);

            // Redirect to chat page
            response.sendRedirect("chat.jsp");
        } else {
            request.setAttribute("error", "Registration failed, please try again");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}