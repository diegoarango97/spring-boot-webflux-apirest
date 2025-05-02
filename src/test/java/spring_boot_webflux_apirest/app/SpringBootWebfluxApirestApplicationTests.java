package spring_boot_webflux_apirest.app;

import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import spring_boot_webflux_apirest.app.models.documents.Categoria;
import spring_boot_webflux_apirest.app.models.documents.Producto;
import spring_boot_webflux_apirest.app.models.services.ProductoService;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) --esta configuración levanta un servidor real para las pruebas
@AutoConfigureWebTestClient //esta anotacion solo se pone cuando el modo de pruebas es mock
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) 

class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	
	
	@Autowired
	private ProductoService productoService;
	

	@Test
	void listarTest() {
		client.get()
		.uri("/api/v2/productos")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class);
	}
	
	@Test
	void listarConOperadorConsumeWithTest() {
		client.get()
		.uri("/api/v2/productos")
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith(response -> {
			List<Producto> productos = response.getResponseBody();
			productos.forEach(producto ->{
				Assertions.assertThat(producto.getNombre().isEmpty()).isFalse();
			});
			Assertions.assertThat(productos.size() > 0 ).isTrue();
		});
	}
	
	@Test
	void verWithJSONResponseTest() {
		Producto producto = productoService.findByNombre("Xbox").block();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Xbox");
	}
	
	@Test
	void verWithProductoResponseTest() {
		Producto producto = productoService.findByNombre("Xbox").block();
		System.out.println("Successful test producto: " + producto.getNombre());
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto productoResponse = response.getResponseBody();
			Assertions.assertThat(productoResponse.getNombre()).isEqualTo("Xbox");
		});
	}
	
	@Test
	void crearTest() {
		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Termo", 50.0 , categoria);
		
		client.post()
		.uri("/api/v2/productos/crear")
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(producto)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto productoCreado = response.getResponseBody();
			Assertions.assertThat(productoCreado.getNombre()).isEqualTo("Termo");
			Assertions.assertThat(productoCreado.getCategoria().getNombre()).isEqualTo("Muebles");

		});
	}
	
	@Test
	void editarTest() {
		Producto producto = productoService.findByNombre("Cama").block();
		Categoria categoria = productoService.findCategoriaByNombre("Muebles").block();
		System.out.println("Categoria: " + categoria.getNombre());
		
		System.out.println("producto: " + producto);
		Producto productoEditado = new Producto("Sofa", 23.0 , categoria);
		
		
		client.put()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.bodyValue(productoEditado)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto productoActualizado = response.getResponseBody();
			Assertions.assertThat(productoActualizado.getNombre()).isEqualTo("Sofa");
			Assertions.assertThat(productoActualizado.getCategoria().getNombre()).isEqualTo("Muebles");

		});
	}
	
	@Test
	void eliminarTest() {
		Producto producto = productoService.findByNombre("Audifonos").block();

		client.delete()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNoContent()
		.expectBody().isEmpty();
		
		client.get()
		.uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()
		.expectStatus().isNotFound()
		.expectBody().isEmpty();
	}
	

}
