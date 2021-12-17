package com.patch.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class JettyServer implements Runnable{
    private final static int PORT = 7777;
    private static Server server;

    public void startServer() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run(){
        server = new Server(PORT);
        System.out.println("Patch Server run at " + PORT);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(GetPatchClassServlet.class, "/*");
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class GetPatchClassServlet extends HttpServlet {
        int len;
        byte[] buffer = new byte[1024];
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("patch/PatchClass.class");
            if (in != null) {
                OutputStream out = response.getOutputStream();
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                in.close();
            }
        }
    }
}
