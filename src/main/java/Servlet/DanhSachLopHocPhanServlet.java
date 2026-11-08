package Servlet;

import Models.LopHocPhan;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.DanhSachLopHocPhanService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/admin/lophocphan/sinhvien")
public class DanhSachLopHocPhanServlet extends HttpServlet {
    private final DanhSachLopHocPhanService service;

    public DanhSachLopHocPhanServlet() {
        this(new DanhSachLopHocPhanService());
    }

    DanhSachLopHocPhanServlet(DanhSachLopHocPhanService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        int maLHP = intValue(req.getParameter("maLHP"));
        if (maLHP <= 0) {
            redirectToList(req, resp, "Mã lớp học phần không hợp lệ.");
            return;
        }

        try {
            LopHocPhan section = service.getSection(maLHP);
            if (section == null) {
                redirectToList(req, resp, "Không tìm thấy lớp học phần.");
                return;
            }

            String keyword = req.getParameter("q");
            req.setAttribute("trang", "lophocphan");
            req.setAttribute("tieuDeTrang", "Danh sách sinh viên lớp học phần");
            req.setAttribute("lhp", section);
            req.setAttribute("members", service.getMembers(maLHP));
            req.setAttribute("candidates", service.findCandidates(maLHP, keyword));
            req.setAttribute("q", keyword == null ? "" : keyword.trim());
            req.getRequestDispatcher("/WEB-INF/admin/lhpSinhVien.jsp").forward(req, resp);
        } catch (RepositoryException ignored) {
            handleRepositoryError(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!isAdmin(req, resp)) {
            return;
        }

        int maLHP = intValue(req.getParameter("maLHP"));
        if (maLHP <= 0) {
            redirectToList(req, resp, "Mã lớp học phần không hợp lệ.");
            return;
        }

        String action = req.getParameter("action");
        try {
            if ("add".equals(action)) {
                service.addStudent(maLHP, intValue(req.getParameter("maNguoidung")));
                redirectToSection(req, resp, maLHP, "msg",
                        "Thêm sinh viên vào lớp học phần thành công.");
            } else if ("remove".equals(action)) {
                service.removeStudent(maLHP, intValue(req.getParameter("maDanhsach")));
                redirectToSection(req, resp, maLHP, "msg",
                        "Gỡ sinh viên khỏi lớp học phần thành công.");
            } else {
                redirectToSection(req, resp, maLHP, "error", "Hành động không hợp lệ.");
            }
        } catch (AcademicException e) {
            redirectToSection(req, resp, maLHP, "error", e.getMessage());
        } catch (RepositoryException ignored) {
            handleRepositoryError(resp);
        }
    }

    private boolean isAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan account = session == null ? null : (TaiKhoan) session.getAttribute("user");
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!"ADMIN".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho Admin.");
            return false;
        }
        return true;
    }

    private void handleRepositoryError(HttpServletResponse resp) throws IOException {
        if (getServletContext() != null) {
            getServletContext().log("Lỗi dữ liệu danh sách lớp học phần.");
        }
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Không thể xử lý yêu cầu lúc này.");
    }

    private void redirectToSection(HttpServletRequest req, HttpServletResponse resp,
                                   int maLHP, String type, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/lophocphan/sinhvien?maLHP="
                + maLHP + "&" + type + "=" + encoded);
    }

    private void redirectToList(HttpServletRequest req, HttpServletResponse resp,
                                String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/admin/lophocphan?error=" + encoded);
    }

    private static int intValue(String value) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
