package Servlet;

import Models.DiemSo;
import Models.TaiKhoan;
import Repository.RepositoryException;
import Services.AcademicException;
import Services.LopHocPhanService;
import Services.NhapDiemService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/giangvien/nhapdiem")
public class NhapDiemServlet extends HttpServlet {
    private final NhapDiemService nhapDiemService;
    private final LopHocPhanService lhpService;

    public NhapDiemServlet() {
        this(new NhapDiemService(), new LopHocPhanService());
    }

    NhapDiemServlet(NhapDiemService nhapDiemService,
                    LopHocPhanService lhpService) {
        this.nhapDiemService = nhapDiemService;
        this.lhpService = lhpService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TaiKhoan teacher = getGiangVien(req, resp);
        if (teacher == null) return;
        Integer sectionId = parsePositive(req.getParameter("maLHP"));
        try {
            showPage(req, resp, teacher.getMaNguoidung(), sectionId);
        } catch (AcademicException e) {
            handleAcademicError(resp, e);
        } catch (RepositoryException e) {
            handleRepositoryError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        TaiKhoan teacher = getGiangVien(req, resp);
        if (teacher == null) return;

        Integer sectionId = parsePositive(req.getParameter("maLHP"));
        Map<String, String> submitted = submittedScores(req);
        try {
            if (sectionId == null) {
                throw invalid("Dữ liệu nhập điểm không hợp lệ.");
            }
            List<DiemSo> values = parseCells(req);
            nhapDiemService.saveDiem(
                    teacher.getMaNguoidung(), sectionId, values);
            redirect(req, resp, sectionId, "msg", "Lưu điểm thành công.");
        } catch (AcademicException e) {
            if (e.getCode() == AcademicException.Code.FORBIDDEN) {
                handleAcademicError(resp, e);
                return;
            }
            req.setAttribute("error", e.getMessage());
            req.setAttribute("submittedScores", submitted);
            try {
                showPage(req, resp, teacher.getMaNguoidung(), sectionId);
            } catch (AcademicException reloadError) {
                handleAcademicError(resp, reloadError);
            } catch (RepositoryException reloadError) {
                handleRepositoryError(resp, reloadError);
            }
        } catch (RepositoryException e) {
            handleRepositoryError(resp, e);
        }
    }

    private void showPage(HttpServletRequest req,
                          HttpServletResponse resp,
                          int teacherId,
                          Integer sectionId)
            throws ServletException, IOException, AcademicException {
        req.setAttribute("trang", "nhapdiem");
        req.setAttribute("tieuDeTrang", "Nhập điểm");
        req.setAttribute("dsLHP", lhpService.getAll(null, teacherId));
        if (sectionId != null) {
            req.setAttribute("maLHP", sectionId);
            req.setAttribute("nhapDiemData",
                    nhapDiemService.getNhapDiemData(teacherId, sectionId));
        }
        req.getRequestDispatcher("/WEB-INF/teacher/nhapDiem.jsp").forward(req, resp);
    }

    private List<DiemSo> parseCells(HttpServletRequest req) throws AcademicException {
        String[] memberships = req.getParameterValues("maDanhSach");
        String[] components = req.getParameterValues("maThanhPhan");
        String[] scores = req.getParameterValues("diem");
        if (memberships == null || components == null || scores == null
                || memberships.length == 0
                || memberships.length != components.length
                || memberships.length != scores.length) {
            throw invalid("Dữ liệu nhập điểm không hợp lệ.");
        }

        List<DiemSo> result = new ArrayList<>();
        for (int i = 0; i < scores.length; i++) {
            Integer membership = parsePositive(memberships[i]);
            Integer component = parsePositive(components[i]);
            if (membership == null || component == null) {
                throw invalid("Dữ liệu nhập điểm không hợp lệ.");
            }
            DiemSo cell = new DiemSo();
            cell.setMaDanhsach(membership);
            cell.setMaThanhphan(component);
            cell.setGiaTriDiem(parseScore(scores[i]));
            result.add(cell);
        }
        return result;
    }

    private Map<String, String> submittedScores(HttpServletRequest req) {
        Map<String, String> result = new LinkedHashMap<>();
        String[] memberships = req.getParameterValues("maDanhSach");
        String[] components = req.getParameterValues("maThanhPhan");
        String[] scores = req.getParameterValues("diem");
        if (memberships == null || components == null || scores == null) return result;
        int length = Math.min(memberships.length,
                Math.min(components.length, scores.length));
        for (int i = 0; i < length; i++) {
            result.put(memberships[i].trim() + ":" + components[i].trim(), scores[i]);
        }
        return result;
    }

    private static BigDecimal parseScore(String raw) throws AcademicException {
        if (raw == null || raw.trim().isEmpty()) return null;
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0
                    || value.compareTo(BigDecimal.TEN) > 0
                    || Math.max(0, value.stripTrailingZeros().scale()) > 2) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw invalid("Điểm phải từ 0 đến 10 và có tối đa hai chữ số thập phân.");
        }
    }

    private TaiKhoan getGiangVien(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        TaiKhoan account = session == null ? null : (TaiKhoan) session.getAttribute("user");
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (!"TEACHER".equalsIgnoreCase(account.getTenVaiTro())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Chức năng này chỉ dành cho giảng viên.");
            return null;
        }
        return account;
    }

    private void handleAcademicError(HttpServletResponse resp, AcademicException e)
            throws IOException {
        if (e.getCode() == AcademicException.Code.FORBIDDEN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập lớp học phần này.");
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void handleRepositoryError(HttpServletResponse resp, RepositoryException e)
            throws IOException {
        getServletContext().log("Lỗi dữ liệu bảng điểm", e);
        resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Không thể xử lý yêu cầu lúc này.");
    }

    private void redirect(HttpServletRequest req,
                          HttpServletResponse resp,
                          int sectionId,
                          String type,
                          String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        resp.sendRedirect(req.getContextPath() + "/giangvien/nhapdiem?maLHP="
                + sectionId + "&" + type + "=" + encoded);
    }

    private static Integer parsePositive(String value) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static AcademicException invalid(String message) {
        return new AcademicException(AcademicException.Code.INVALID_INPUT, message);
    }
}
