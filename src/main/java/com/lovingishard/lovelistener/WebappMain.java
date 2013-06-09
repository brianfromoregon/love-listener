package com.lovingishard.lovelistener;

import lang.Loggers;
import org.slf4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 */
public class WebappMain implements ServletContextListener {

    static final Logger log = Loggers.contextLogger();

    Main main;

    public void contextInitialized(ServletContextEvent sce) {
        log.info("WebappMain.contextInitialized");
        main = new Main();
        main.start();
    }

    public void contextDestroyed(ServletContextEvent sce){
        log.info("WebappMain.contextDestroyed");
        if (main != null) {
            main.shutdown();
        }
    }
}