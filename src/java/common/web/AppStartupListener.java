package common.web;

import common.util.DatabaseUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/** Initializes shared infrastructure for the web application. */
public class AppStartupListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent sce) { DatabaseUtil.configure(sce.getServletContext()); }
}
