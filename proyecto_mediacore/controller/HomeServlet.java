package proyecto_mediacore.controller;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
@WebServlet("/home") // URL mapeada
public class HomeServlet extends HttpServlet {
private static final long serialVersionUID = 1L;
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
IOException {
resp.getWriter().println("¡Hola mundo desde HomeServlet!");
}
}