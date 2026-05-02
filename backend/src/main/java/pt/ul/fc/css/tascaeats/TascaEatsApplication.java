// ##############################################################
// 						  TASCAEATS								#
// 						CSS GRUPO: 25							#
//           Autores:                Numero Aluno:              #
//        - Omeir Haroon               61810                    #
//        - Isa David                  61871                    #
//        - Diogo Roque                61857                    #
// ##############################################################
package pt.ul.fc.css.tascaeats;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import jakarta.transaction.Transactional;

@SpringBootApplication
public class TascaEatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TascaEatsApplication.class, args);
    }


    @Bean
    @Transactional
    public CommandLineRunner demo() {
        return (args) -> {
            
            try{
                System.out.println("\u001B[32m\u001B[1mIt is alive! ⚡🏰⚡🧟‍♂️ \u001B[0m");
            } catch (Exception e) {
                System.out.println("\u001B[31m\u001B[1mSomething went wrong 🪦\u001B[0m");
                e.printStackTrace();
            }
        };
    }



}
