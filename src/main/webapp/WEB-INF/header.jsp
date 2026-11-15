<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Models.TaiKhoan" %>
<%@ page import="Utils.Html" %>
<%
    TaiKhoan currentUser = (TaiKhoan) session.getAttribute("user");
    String userRole = "";
    String hoTen = "Người dùng";
    String email = "";
    String initial = "U";

    if (currentUser != null) {
        userRole = currentUser.getTenVaiTro() != null ? currentUser.getTenVaiTro().toUpperCase() : "";
        hoTen = currentUser.getHoTen() != null ? currentUser.getHoTen() : "Người dùng";
        email = currentUser.getEmail() != null ? currentUser.getEmail() : "";
        if (!hoTen.trim().isEmpty()) {
            initial = hoTen.trim().substring(0, 1).toUpperCase();
        }
    }

    String roleDisplayName;
    switch (userRole) {
        case "ADMIN":
            roleDisplayName = "Quản trị viên";
            break;
        case "TEACHER":
            roleDisplayName = "Giảng viên";
            break;
        case "STUDENT":
            roleDisplayName = "Sinh viên";
            break;
        default:
            roleDisplayName = "Người dùng";
            break;
    }

    String currentPage = (String) request.getAttribute("trang");
    String pageTitle = (String) request.getAttribute("tieuDeTrang");
    if (pageTitle == null) pageTitle = "Hệ thống QLSV";
    String contextPath = request.getContextPath();
    String goc = contextPath;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="<%= Html.escape(request.getAttribute("csrfToken")) %>">
    <title><%= Html.escape(pageTitle) %> - QLSV</title>
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/admin.css?v=2">
    <link rel="stylesheet" href="<%= contextPath %>/assets/css/timetable.css">
</head>
<body>
<div class="lop-phu" id="lopPhu" onclick="dongSidebar()"></div>

<aside class="sidebar" id="sidebar">
    <div class="sidebar-thuonghieu">HỆ THỐNG QLSV</div>
    <div class="sidebar-nguoidung">
        <div class="avatar-tron"><%= Html.escape(initial) %></div>
        <div>
            <div class="ten"><%= Html.escape(hoTen) %></div>
            <div class="vaitro"><%= Html.escape(roleDisplayName) %></div>
        </div>
    </div>
    <nav class="sidebar-menu">
        <%@ include file="/WEB-INF/sidebar.jsp" %>
    </nav>
    <div class="sidebar-chan">
        <a class="nut-dangxuat" href="<%= contextPath %>/logout">Đăng xuất</a>
    </div>
</aside>

<div class="boc-chinh">
    <header class="header-tren">
        <button class="nut-hamburger" onclick="moSidebar()" aria-label="Mở menu">&#9776;</button>
        <div class="header-tieude"><%= Html.escape(pageTitle) %></div>
        <div class="header-phai"><%= Html.escape(email) %></div>
    </header>
    <main class="than-noidung">
