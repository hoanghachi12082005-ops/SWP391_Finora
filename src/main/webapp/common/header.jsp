<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div class="flex items-center gap-4 ml-auto">
    <button class="relative p-2 rounded-lg hover:bg-surface-container transition-colors" title="Thông báo">
        <span class="material-symbols-outlined text-on-surface-variant">notifications</span>
    </button>
    <div class="flex items-center gap-3 pl-4 border-l border-outline-variant">
        <div class="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-on-primary text-sm font-bold">
            ${fn:substring(sessionScope.currentUser.fullName, 0, 1)}
        </div>
        <div class="text-sm leading-tight hidden sm:block">
            <div class="font-semibold text-on-surface">${sessionScope.currentUser.fullName}</div>
            <div class="text-outline text-xs">${sessionScope.currentUser.roleName}</div>
        </div>
    </div>
</div>