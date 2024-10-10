package com.soritechnology.reactor.app;

import com.soritechnology.reactor.app.models.Comentarios;
import com.soritechnology.reactor.app.models.Usuario;
import com.soritechnology.reactor.app.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
		ejemploUsuarioComentariosZipWithSegundaForma();

	}

	public void ejemploUsuarioComentariosZipWithSegundaForma() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal estamos?");
			comentarios.addComentario("Mañana tengo una reunión muy importante");
			comentarios.addComentario("Estoy muy emocionado con el curso de Reactor");
			return comentarios;
		});

		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
						.map(tuple -> {
							Usuario u = tuple.getT1();
							Comentarios c = tuple.getT2();
							return new UsuarioComentarios(u, c);
						});


		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal estamos?");
			comentarios.addComentario("Mañana tengo una reunión muy importante");
			comentarios.addComentario("Estoy muy emocionado con el curso de Reactor");
			return comentarios;
		});


		usuarioMono.zipWith(comentariosUsuarioMono, UsuarioComentarios::new)
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));

		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, que tal estamos?");
			comentarios.addComentario("Mañana tengo una reunión muy importante");
			comentarios.addComentario("Estoy muy emocionado con el curso de Reactor");
			return comentarios;
		});


		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Pepito", "Grillo"));
		usuariosList.add(new Usuario("Fulanito" , "Pérez"));
		usuariosList.add(new Usuario("Menganito", "López"));
		usuariosList.add(new Usuario("Juanito", "Pérez"));
		usuariosList.add(new Usuario("Omar", "Montes"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> {
					lista.forEach(item -> log.info(item.toString()));
				});

	}

	public void ejemploToString() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Pepito", "Grillo"));
		usuariosList.add(new Usuario("Fulanito" , "Pérez"));
		usuariosList.add(new Usuario("Menganito", "López"));
		usuariosList.add(new Usuario("Juanito", "Pérez"));
		usuariosList.add(new Usuario("Omar", "Montes"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				})
				.map(nombre -> {
					return nombre.toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));

	}

	public void ejemploFlatMap() throws Exception {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Pepito Grillo");
		usuariosList.add("Fulanito Pérez");
		usuariosList.add("Menganito López");
		usuariosList.add("Juanito Pérez");
		usuariosList.add("Omar Montes");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				})
				.map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u -> log.info(u.toString()));

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
