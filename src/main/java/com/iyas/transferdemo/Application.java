package com.iyas.transferdemo;

import com.iyas.transferdemo.dao.DaoFactory;
import com.iyas.transferdemo.service.AccountService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.omg.IOP.TransactionService;

public class Application {

    public static void main(String[] args) throws Exception {
        DaoFactory daoFactory = new DaoFactory();
        daoFactory.populateTestData();

        startServer();
    }

    private static void startServer() throws Exception {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitParameter("jersey.config.server.provider.classnames",
                AccountService.class.getCanonicalName() + ","
                       // + ServiceExceptionMapper.class.getCanonicalName() + ","
                        + TransactionService.class.getCanonicalName());
        try {
            server.start();
            server.join();
        } finally {
            server.destroy();
        }
    }
}
