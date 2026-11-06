package edu.pucp.plg_back;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import edu.pucp.plg_back.simulation.SimulationService;
import edu.pucp.plg_back.util.FileLogger;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // Desactivar la configuración automática de la base de datos
public class PlgBackApplication {

	public static void main(String[] args) {
		FileLogger.log("Application starting...");
		@SuppressWarnings("unused")
		ConfigurableApplicationContext context = SpringApplication
				.run(PlgBackApplication.class, args);
		FileLogger.log("Application started successfully.");
	}

	// Define un CommandLineRunner que se ejecuta al inicio de la aplicación para 
	// crear una instancia de SimulationService y ejecutar una simulación.
	@Bean
	CommandLineRunner demo() {
		
		return args -> {
			//@SuppressWarnings("unused");
			//SimulationService simulation = new SimulationService();
			//SseEmitter emitter = new SseEmitter();
			//simulation.iniciarSimulacion("2025-01-01",null);
		};
	}
}
