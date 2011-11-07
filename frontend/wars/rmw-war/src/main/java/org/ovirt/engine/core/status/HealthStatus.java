package org.ovirt.engine.core.status;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

/***
 * This class is a servlet implementation, aimed to report fundemental health of engine.
 * The servlet URL is: /ENGINEanagerWeb/HealthStatus (as defined in web.xml), and these
 * are the possible HTTP return codes:
 * - 200: OK
 * - 500: Unable to connect to DB.
 * - 404/other: Depending on Tomcat/Jboss state, servlet may be unavailable or connection refused.
 * If the servlet is unable to contact the backend bean, it'll write an apropriate message to out.
 *
 */
public class HealthStatus extends HttpServlet {

	private static LogCompat log = LogFactoryCompat.getLog(HealthStatus.class);

	private boolean runQuery(HttpServletRequest request, PrintWriter out) {
		boolean fReturn = false;
		BackendInternal backend = null;
		VdcQueryParametersBase params = null;
		VdcQueryReturnValue v = null;

		try {
			backend = (BackendInternal) EjbUtils.findBean(BeanType.BACKEND, BeanProxyType.LOCAL);
			log.debug("Calling CheckDBConnection query");

			params = new VdcQueryParametersBase();

			v = backend.runInternalQuery(VdcQueryType.CheckDBConnection, params);
			if (v != null) {
				fReturn = v.getSucceeded();
				out.print(fReturn ? "DB Up!" : "DB Down!");
			} else {
				log.error("Got NULL from backend.RunQuery!");
			}
		} catch (Throwable t) {
			String msg = "Unable to contact Database!";
			if (backend == null)
			{
				msg = "Unable to contact Backend!";
			}
			out.print(msg);
			log.error(msg + " Caught exception while trying to run query: ", t);
			fReturn = false;
		}

		return fReturn;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("Health Status servlet: entry");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		try {
			if (runQuery(request, out)) {
				out.print("Welcome to Health Status!");
				log.debug("Succeeded to run Health Status.");
			} else {
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
				log.error("Failed to run Health Status.");
			}
		} catch (Exception e) {
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
			log.error("Error calling runQuery: ", e);
		} finally {
			out.close();
		}
		log.debug("Health Status servlet: close");
	}
}
