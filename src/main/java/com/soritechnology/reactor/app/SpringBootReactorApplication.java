package com.soritechnology.reactor.app;

import com.soritechnology.reactor.app.models.Comentarios;
import com.soritechnology.reactor.app.models.Usuario;
import com.soritechnology.reactor.app.models.UsuarioComentarios;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;


@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploContraPresion();

	}

	public void ejemploContraPresion() {

		Flux.range(1, 10)
				.log()
				.limitRate(5)
				.subscribe(/*new Subscriber<Integer> () {

					private Subscription subscription;

					private Integer limite = 5;
					private Integer consumido = 0;

					@Override
					public void onSubscribe(Subscription subscription) {
						this.subscription = subscription;
						this.subscription.request(limite);
					}

					@Override
					public void onNext(Integer t) {
						log.info(t.toString());
						consumido++;
						if (consumido ==  limite) {
							consumido = 0;
							subscription.request(limite);
						}

					}


					@Override
					public void onError(Throwable throwable) {

					}

					@Override
					public void onComplete() {

					}
				}*/);

	}

	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (contador == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5!"));
					}
				}
			}, 1000, 1000);
		})
				//.doOnNext(next -> log.info(next.toString()))
				//.doOnComplete(() -> log.info("Hemos acabado la ejecución"))
				.subscribe(next -> log.info(next.toString()),
						error -> log.error(error.getMessage()),
						() -> log.info("Hemos acabado la ejecución"));

	}

	public void ejemploIntervalInfinito() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Terminamos en el 5"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

		rango.zipWith(retraso, (ra, re) -> ra)
				.doOnNext(i -> log.info(i.toString()))
				.blockLast(); 	// Esto lo usamos para poder verlo desde la terminal. Ya que de lo contrario no se vería nada. Finalizaría el main pero por detras la maquina
								// virtual seguiría usando los hilos y no se vería nada.
				//.subscribe();
	}

	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12);
		rango.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()))
				.subscribe();
				//.blockLast();

		Thread.sleep(13000);
	}

	public void ejemploZipWithRangos() {

		Flux.just(1, 2, 3, 4)
				.map(i -> (i * 2))
				.zipWith(Flux.range(0, 4), (integer, integer2) -> String.format("Primer Flux: %d, Segundo Flux: %d", integer, integer2))
				.subscribe(texto -> log.info(texto));
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
