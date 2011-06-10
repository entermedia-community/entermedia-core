package com.openedit.servlet.gzip;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GzipFilter implements Filter {
	private static final Log log = LogFactory.getLog(GzipFilter.class);

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		if (req instanceof HttpServletRequest) {

			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;
			String rce = request.getHeader("Content-Encoding");
			if (rce != null && (rce.equals("gzip"))) {
				request = new GzipRequestWrapper(request);
				log.info("browser is sending gzip-decoding");
			}
			if (canCompress(request)) {
				GzipResponseWrapper wrappedResponse = new GzipResponseWrapper(response);
				chain.doFilter(req, wrappedResponse);
				wrappedResponse.finishResponse();
				return;
			}
			chain.doFilter(req, res);
		}
	}

	private boolean canCompress(HttpServletRequest inRequest) {
		String ae = inRequest.getHeader("accept-encoding");
		if (ae != null && ae.indexOf("gzip") != -1) {
			// We no longer need to check since most browser pass the smart
			// header
			// String path = inRequest.getRequestURI();
			// if( path != null || path.length() > 3)
			// {
			// path = path.substring(path.length() -4).toLowerCase();
			// }
			// if( path.equals(".jpg") || path.equals(".gif") ||
			// path.equals(".png") || path.equals(".zip") )
			// {
			// return false; //already compressed
			// }
			return true;
		}
		return false;
	}

	public void init(FilterConfig filterConfig) {
		// noop
	}

	public void destroy() {
		// noop
	}
}
