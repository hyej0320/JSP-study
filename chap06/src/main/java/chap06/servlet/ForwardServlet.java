package chap06.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chap06.dao.OjdbcConnection;
import chap06.web.WebProcess;
import chap06.webprocess.EmpDeleteProcess;
import chap06.webprocess.EmpDetailProcess;
import chap06.webprocess.EmpListProcess;
import chap06.webprocess.EmpUpdateFormProcess;
import chap06.webprocess.EmpUpdateProcess;
import chap06.webprocess.HelloProcess;

public class ForwardServlet extends HttpServlet{

	// 생성한 WebProcess 인터페이스 import
	// String에는 method + uri가 들어오게 된다
	final private static HashMap<String, WebProcess> URI_MAPPING = new HashMap<>();
	final private static String PREFIX = "/WEB-INF/views";
	final private static String SUFFIX = ".jsp";
	
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		// 서버(application)의 어트리뷰트에 DB연결 객체를 미리 추가해 둔다
		config.getServletContext().setAttribute("ojdbc", new OjdbcConnection(
				config.getInitParameter("jdbcUrl"), 
				config.getInitParameter("user"), 
				config.getInitParameter("pwd")));
		
		URI_MAPPING.put("GET:/hello", new HelloProcess());
		URI_MAPPING.put("GET:/emp/list", new EmpListProcess());
		URI_MAPPING.put("GET:/emp/detail", new EmpDetailProcess());
		URI_MAPPING.put("GET:/emp/delete", new EmpDeleteProcess());
		// ※ 같은 uri여도 메서드마다 다른 처리 적용하기
		URI_MAPPING.put("GET:/emp/update", new EmpUpdateFormProcess());
		URI_MAPPING.put("GET:/emp/update", new EmpUpdateProcess());
		
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String method = req.getMethod();
		String uri = req.getRequestURI();
		
		System.out.println("요청방식: " + method);
		System.out.println("요청URI: " + uri);
		System.out.println("만들어내는 키 - " + method + ":" + uri);
		
		// 요청방식과 URI에 따라 알맞은 처리를 진행한다
		// 처리를 진행한 후에는 다음에 어떤 JSP로 포워드 해야할 지를 알아야 한다		
		WebProcess wp = URI_MAPPING.get(method + ":" + uri);
		
		String nextView = null;
		
		if (wp != null) {
			nextView = wp.process(req, resp);
		} else {
			resp.sendRedirect(req.getContextPath() + "/hello");
			return;
		}
		
		// 화면을 그리기 위해 JSP페이지로 포워드한다
		
		if(nextView.startsWith("redirect:")) {
			resp.sendRedirect(nextView.substring("redirect:".length()));
		} else {
			req.getRequestDispatcher(PREFIX + nextView + SUFFIX).forward(req, resp);
		}
	
	}
}
