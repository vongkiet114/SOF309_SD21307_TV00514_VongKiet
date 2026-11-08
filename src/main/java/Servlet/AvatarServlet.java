package Servlet;

import Utils.FileUploadUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/media/avatar/*")
public class AvatarServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String CACHE_CONTROL = "public, max-age=86400";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String[] location = parseLocation(req.getPathInfo());
        if (location == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ServletContext context = req.getServletContext();
        final Path external;
        try {
            external = FileUploadUtils.resolveImage(
                    context, location[0], location[1]);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] content;
        if (external != null) {
            content = Files.readAllBytes(external);
        } else {
            String bundledPath = "/assets/img/" + location[0] + "/" + location[1];
            try (InputStream input = context.getResourceAsStream(bundledPath)) {
                if (input == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                content = input.readAllBytes();
            }
        }

        resp.setContentType(FileUploadUtils.imageContentType(location[1]));
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Cache-Control", CACHE_CONTROL);
        resp.setContentLengthLong(content.length);
        resp.getOutputStream().write(content);
    }

    private static String[] parseLocation(String pathInfo) {
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            return null;
        }
        String[] pieces = pathInfo.substring(1).split("/", -1);
        if (pieces.length != 2) {
            return null;
        }
        try {
            FileUploadUtils.validateImageLocation(pieces[0], pieces[1]);
            return pieces;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
