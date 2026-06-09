<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body class="auth-page">
<div class="auth-box">
    <h1>Login</h1>
    <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="form-row"><label>Username</label><input name="username" required></div>
        <div class="form-row"><label>Password</label><input type="password" name="password" required></div>
        <button class="btn" type="submit">Login</button>
        <a href="${pageContext.request.contextPath}/register">Register Account</a> |
        <a href="${pageContext.request.contextPath}/forgot-password">Forgot Password</a>
    </form>
</div>
</body>
</html>
