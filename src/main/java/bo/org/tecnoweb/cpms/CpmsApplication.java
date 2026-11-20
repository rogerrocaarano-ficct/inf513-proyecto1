package bo.org.tecnoweb.cpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of CPMS, the system that manages the graduation project procedures.
 */
@SpringBootApplication
public class CpmsApplication {

    /**
     * Not instantiated: Spring Boot creates the application context.
     */
    CpmsApplication() {
    }

    /**
     * Starts the application.
     *
     * @param args command line arguments, passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CpmsApplication.class, args);
    }

}
