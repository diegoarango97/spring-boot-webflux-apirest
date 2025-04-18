package spring_boot_webflux_apirest.app.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.documents.Producto;
import spring_boot_webflux_apirest.app.models.services.ProductoService;

@RestController
@RequestMapping("/api")
public class ProductoController {

	@Autowired
	ProductoService productoService;

	@Value("${configuracion.cargarimagenes.ruta}")
	private String rutaCargaArchivos;
	
	@PostMapping("/productos/crear-v2")
	public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto ,@RequestPart FilePart file) {
		
		producto.setFoto(UUID.randomUUID().toString()
				+ file.filename().replace(" ", "_").replace(":", "").replace("\\", ""));
		
		return file.transferTo(new File(rutaCargaArchivos + producto.getFoto()))
				.then(productoService.save(producto))
				.map(productoGuardado -> {
			return ResponseEntity.created(URI.create("/api/productos/crear".concat(producto.getId())))
					.body(productoGuardado);
		});
	}

	@PostMapping("/productos/subir-foto/{id}")
	public Mono<ResponseEntity<Producto>> subirFoto(@PathVariable String id, @RequestPart FilePart file) {
		return productoService.findById(id).flatMap(producto -> {
			producto.setFoto(UUID.randomUUID().toString()
					+ file.filename().replace(" ", "_").replace(":", "").replace("\\", ""));
			return file.transferTo(new File(rutaCargaArchivos + producto.getFoto()))
					.then(productoService.save(producto));
		})
	    .map(producto -> ResponseEntity.ok(producto))
		.defaultIfEmpty(ResponseEntity.noContent().build());
	}

	@GetMapping("/productos")
	public Flux<Producto> listarProductos() {
		return productoService.findAll();
	}

	@GetMapping("/productos-v2")
	public Mono<ResponseEntity<Flux<Producto>>> listarProductosForma2() {
		return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(productoService.findAll()));
	}

	@GetMapping("/productos/{id}")
	public Mono<ResponseEntity<Producto>> ver(@PathVariable String id) {
		return productoService.findById(id).map(producto -> {
			return ResponseEntity.ok(producto);
		}).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@PostMapping("/productos/crear")
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> respuesta = new HashMap<>();
		
		
		return monoProducto.flatMap(producto -> {
			return productoService.save(producto).map(productoGuardado -> {
				respuesta.put("producto", productoGuardado);
				respuesta.put("mensaje", "Producto creado con éxito");
				respuesta.put("timestamp", new Date());
				return ResponseEntity.created(URI.create("/api/productos/crear".concat(producto.getId())))
						.body(respuesta);
			});
		}).onErrorResume(error-> {
			return Mono.just(error).cast(WebExchangeBindException.class)
					.flatMap(exception-> Mono.just(exception.getFieldErrors()))
					.flatMapMany(errores-> Flux.fromIterable(errores))
					.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(lista -> {
						respuesta.put("errors", lista);
						respuesta.put("timestamp", new Date());
						respuesta.put("status", HttpStatus.BAD_REQUEST.value());
						return Mono.just(ResponseEntity.badRequest().body(respuesta));
						});
		});
		
	
	}

	@PutMapping("/productos/{id}")
	public Mono<ResponseEntity<Producto>> editar(@PathVariable String id, @RequestBody Producto producto) {
		return productoService.findById(id).flatMap(productoEncontrado -> {
			productoEncontrado.setNombre(producto.getNombre());
			productoEncontrado.setPrecio(producto.getPrecio());
			productoEncontrado.setCategoria(producto.getCategoria());
			return productoService.save(productoEncontrado);
		}).map(productoGuardado -> {
			return ResponseEntity.created(URI.create("/api/productos/".concat(producto.getId())))
					.body(productoGuardado);
		}).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
	}

	@DeleteMapping("/productos/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id) {

		return productoService.findById(id).flatMap(producto -> {
			return productoService.delete(producto.getId())
					.then(Mono.just(new ResponseEntity<Void>(HttpStatusCode.valueOf(HttpStatus.NO_CONTENT.value()))));

		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value())));
	}
}
