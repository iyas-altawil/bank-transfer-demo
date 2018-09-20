package com.taskforce.moneyapp.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.iyas.transferdemo.service.AccountService;
import com.iyas.transferdemo.dao.DaoFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;


public abstract class TestService {
    private static Server server = null;
    private static PoolingHttpClientConnectionManager connMngr = new PoolingHttpClientConnectionManager();

    static HttpClient client;
    private static DaoFactory daoFactory = new DaoFactory();
    ObjectMapper mapper = new ObjectMapper();
    URIBuilder builder = new URIBuilder().setScheme("http").setHost("localhost:8084");

    static Integer PORT = 8084;

    @BeforeClass
    public static void setup() throws Exception {
        daoFactory.populateTestData();
        startServer();
        connMngr.setDefaultMaxPerRoute(100);
        connMngr.setMaxTotal(200);
        client= HttpClients.custom()
                .setConnectionManager(connMngr)
                .setConnectionManagerShared(true)
                .build();

    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @AfterClass
    public static void closeClient() {
        HttpClientUtils.closeQuietly(client);
    }

    private static void startServer() throws Exception {
        if (server == null) {
            server = new Server(PORT);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);
            ServletHolder servletHolder = context.addServlet(ServletContainer.class, "/*");
            servletHolder.setInitParameter("jersey.config.server.provider.classnames",
                            AccountService.class.getCanonicalName());
            server.start();
        }
    }

    String getUrl(String url){
        return "http://localhost:" + PORT + url;
    }
}
