package bo.org.tecnoweb.cpms;

import org.springframework.boot.SpringApplication;

public class TestCpmsApplication {

    public static void main(String[] args) {
        SpringApplication.from(CpmsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
