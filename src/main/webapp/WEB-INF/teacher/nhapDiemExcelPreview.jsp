<%@ page import="Models.GradeImportPreview" %>
<%@ page import="Utils.Html" %>
<%@ include file="/WEB-INF/header.jsp" %>
<%
    GradeImportPreview preview =
            (GradeImportPreview) request.getAttribute("preview");
    String error = (String) request.getAttribute("error");
%>
<h1 class="trang-tieude">Xem trước điểm từ Excel</h1>
<p class="trang-mota">Kiểm tra toàn bộ thay đổi trước khi lưu vào bảng điểm.</p>

<% if (error != null) { %>
    <div class="bao-loi"><%= Utils.Html.escape(error) %></div>
<% } %>

<% if (preview != null) { %>
<div class="the">
    <p><strong>File:</strong> <%= Utils.Html.escape(preview.getFileName()) %></p>
    <p><strong>Lớp học phần:</strong> <%= Utils.Html.escape(preview.getSectionCode()) %></p>
    <div class="tom-tat-import" style="margin-top:12px;">
        <div class="the"><strong><%= preview.getAddedCount() %></strong><br>Điểm thêm mới</div>
        <div class="the"><strong><%= preview.getUpdatedCount() %></strong><br>Điểm cập nhật</div>
        <div class="the"><strong><%= preview.getDeletedCount() %></strong><br>Điểm sẽ xóa</div>
        <div class="the"><strong><%= preview.getUnchangedBlankCount() %></strong><br>Ô tiếp tục để trống</div>
    </div>
</div>

<% if (!preview.getErrors().isEmpty()) { %>
<div class="the">
    <h2>Lỗi cần sửa</h2>
    <div class="bang-cuon">
        <table>
            <thead><tr><th>STT</th><th>Chi tiết</th></tr></thead>
            <tbody>
            <% for (int i = 0; i < preview.getErrors().size(); i++) { %>
                <tr class="o-diem-loi">
                    <td><%= i + 1 %></td>
                    <td><%= Utils.Html.escape(preview.getErrors().get(i)) %></td>
                </tr>
            <% } %>
            </tbody>
        </table>
    </div>
</div>
<% } else { %>
<div class="the" style="display:flex; justify-content:flex-end; gap:10px; align-items:center;">
    <a class="nut nut-phu" href="<%= request.getContextPath() %>/giangvien/nhapdiem?maLHP=<%= preview.getSectionId() %>">Hủy</a>
    <form method="post" action="<%= request.getContextPath() %>/giangvien/nhapdiem/excel" style="margin:0;">
        <input type="hidden" name="action" value="confirm">
        <input type="hidden" name="token" value="<%= Utils.Html.escape(preview.getToken()) %>">
        <input type="hidden" name="_csrf" value="<%= Utils.Html.escape(request.getAttribute("csrfToken")) %>">
        <button type="submit" class="nut">Xác nhận lưu bảng điểm</button>
    </form>
</div>
<% } %>
<% } %>

<div style="margin-top:14px;">
    <a href="<%= request.getContextPath() %>/giangvien/nhapdiem">Quay lại nhập điểm</a>
</div>
<%@ include file="/WEB-INF/footer.jsp" %>
