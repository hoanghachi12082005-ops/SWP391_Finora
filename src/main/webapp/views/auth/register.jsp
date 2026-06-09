<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><title>Register Account</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css"></head>
<body class="auth-page"><div class="auth-box"><h1>Register Account</h1>
<form method="post" action="${pageContext.request.contextPath}/register">
<div class="form-row"><label>Full name</label><input name="name"></div>
<div class="form-row"><label>Email</label><input name="email"></div>
<div class="form-row"><label>Username</label><input name="username"></div>
<div class="form-row"><label>Password</label><input type="password" name="password"></div>
<button class="btn">Create Account</button> <a href="${pageContext.request.contextPath}/login">Back to login</a>
</form></div></body></html>
