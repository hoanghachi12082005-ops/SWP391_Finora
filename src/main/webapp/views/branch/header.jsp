<%@ page contentType="text/html;charset=UTF-8" %>

<header class="topbar">

    <div class="search-box">
        <form action="${pageContext.request.contextPath}/branch" method="get">

            <input type="text"
                   name="keyword"
                   value="${param.keyword}"
                   placeholder="Tìm kiếm cửa hàng, mã số...">

            <button class="search-box-btn" type="submit">
                Search
            </button>

        </form>
    </div>

    <div class="topbar-right">

        <button>🔔</button>

        <button>❓</button>

        <div class="user-info">
            <div class="avatar">
                A
            </div>

            <div>
                <strong>Admin</strong>
                <p>Administrator</p>
            </div>
        </div>

    </div>

</header>