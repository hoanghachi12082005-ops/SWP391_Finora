<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Forgot Password</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css"></head>
<body class="auth-page"><div class="auth-box"><h1>Forgot Password</h1>
<form method="post" action="${pageContext.request.contextPath}/forgot-password">
<div class="form-row"><label>Email</label><input name="email"></div>
<button class="btn">Send Reset Link</button> <a href="${pageContext.request.contextPath}/login">Back to login</a>
</form></div></body></html>
