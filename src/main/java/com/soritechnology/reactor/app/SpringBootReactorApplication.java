package com.soritechnology.reactor.app;

import com.soritechnology.reactor.app.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploIterable();
		segundoEjemploIterable();

	}


	public void ejemploIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Pepito Grillo");
		usuariosList.add("Fulanito Pérez");
		usuariosList.add("Menganito López");
		usuariosList.add("Juanito Pérez");
		usuariosList.add("Omar Montes");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux<String> nombres = Flux.fromIterable(usuariosList);

		Flux <Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacíos");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		usuarios.subscribe(e -> log.info(e.toString()),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecución del observable con éxito");
					}
				});
	}

	public void segundoEjemploIterable() {
		Flux< String > flux = Flux.just("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")
				.doOnNext(elemento -> {
					if (elemento.isEmpty()) {
						throw new RuntimeException("Elemento vacío");
					}
					System.out.println(elemento);

				})
				.map(elemento -> {
					return elemento + " - " + elemento;
				});

		//flux.subscribe(log::info);
		flux.subscribe(
				elemento -> log.info(elemento),
				error -> log.error(error.getMessage()),
				new Runnable() {
					@Override
					public void run() {
						log.info("Ha finalizado la ejecución del observable con éxito");
					}
				}
		);
	}


}
