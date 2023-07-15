package senti;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication   // <1>
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args); // <2>
  }

}
