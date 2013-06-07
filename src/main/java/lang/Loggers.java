package lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Loggers {
    public static Logger contextLogger() {
        String className = new Throwable().getStackTrace()[1].getClassName();
        return LoggerFactory.getLogger(className);
    }
}
