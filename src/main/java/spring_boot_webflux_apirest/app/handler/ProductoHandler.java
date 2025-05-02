package spring_boot_webflux_apirest.app.handler;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.documents.Categoria;
import spring_boot_webflux_apirest.app.models.documents.Producto;
import spring_boot_webflux_apirest.app.models.services.ProductoService;

@Component
public class ProductoHandler {

	@Autowired
	private ProductoService productoService;
	
	@Value("${configuracion.cargarimagenes.ruta}")
	private String rutaArchivos;
	
	@Autowired
	private Validator validator;

	
	public Mono<ServerResponse> crearConFoto(ServerRequest request) {
		Mono<Producto> productoMono = request.multipartData().map(multipart -> {
			FormFieldPart nombre = (FormFieldPart) multipart.toSingleValueMap().get("nombre");
			FormFieldPart precio = (FormFieldPart) multipart.toSingleValueMap().get("precio");
			FormFieldPart categoriaId = (FormFieldPart) multipart.toSingleValueMap().get("categoria.id");
			FormFieldPart categoriaNombre = (FormFieldPart) multipart.toSingleValueMap().get("categoria.nombre");
			
			Categoria categoria = new Categoria(categoriaNombre.value());
			categoria.setId(categoriaId.value());

			 return new Producto(nombre.value(), Double.valueOf(precio.value()), categoria);
		});

		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> productoMono
								.flatMap(producto -> {
					producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("\\", ""));
					return file.transferTo(new File( rutaArchivos + producto.getFoto())).then(productoService.save(producto));
					
				})).flatMap(productoGuardado -> ServerResponse.created(URI.create("/api/v2/productos/".concat(productoGuardado.getId()))).bodyValue(productoGuardado));
	}
	
	
	public Mono<ServerResponse> upload(ServerRequest request) {
		String id = request.pathVariable("id");
		return request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> productoService.findById(id)
								.flatMap(producto -> {
					producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
					.replace(" ", "")
					.replace(":", "")
					.replace("\\", ""));
					return file.transferTo(new File( rutaArchivos + producto.getFoto())).then(productoService.save(producto));
					
				})).flatMap(productoGuardado -> ServerResponse.created(URI.create("/api/v2/productos/".concat(productoGuardado.getId()))).bodyValue(productoGuardado))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
		
	
	public Mono<ServerResponse> listar(ServerRequest request) {
		return ServerResponse.ok().body(productoService.findAll(), Producto.class);
	}

	public Mono<ServerResponse> ver(ServerRequest request) {
		String id = request.pathVariable("id");
		return productoService.findById(id).flatMap(producto -> {
			return ServerResponse.ok().bodyValue(producto);
		}).switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> crear(ServerRequest request) {
		Mono<Producto> productoMono = request.bodyToMono(Producto.class);
		return productoMono.flatMap(producto -> {
			
			
			Errors errors = new BeanPropertyBindingResult(producto, Producto.class.getName());
			this.validator.validate(producto, errors);
			if(errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().bodyValue(list));
			}
			
			return productoService.save(producto).flatMap(productoGuardado -> {
				return ServerResponse.created(URI.create("/api/v2/productos/".concat(productoGuardado.getId())))
						.bodyValue(productoGuardado);
			});

		});
	}

	public Mono<ServerResponse> editar(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Producto> productoMono = request.bodyToMono(Producto.class);
		return productoMono.flatMap(producto -> {
			return productoService.findById(id).flatMap(productoEncontrado -> {
				productoEncontrado.setNombre(producto.getNombre());
				productoEncontrado.setPrecio(producto.getPrecio());
				productoEncontrado.setCategoria(producto.getCategoria());

				return productoService.save(productoEncontrado).flatMap(productoGuardado -> {
					return ServerResponse.created(URI.create("/api/v2/productos/".concat(productoEncontrado.getId())))
							.bodyValue(productoGuardado);
				});

			}).switchIfEmpty(ServerResponse.notFound().build());
		});
	}

	public Mono<ServerResponse> editarFormaProfesor(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Producto> productoMono = request.bodyToMono(Producto.class);

		Mono<Producto> productoDB = productoService.findById(id);

		return productoDB.zipWith(productoMono, (productoDb, productoReq) -> {
			productoDb.setNombre(productoReq.getNombre());
			productoDb.setPrecio(productoReq.getPrecio());
			productoDb.setCategoria(productoReq.getCategoria());
			return productoDb;
		})
		.flatMap(productoDb -> ServerResponse.created(URI.create("/api/versionProfesor/productos/".concat(productoDb.getId())))
				.body(productoService.save(productoDb),Producto.class ))
		.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> eliminar (ServerRequest request) {
		String id = request.pathVariable("id");
		
		Mono<Producto> productoDB = productoService.findById(id);
		
		return productoDB.flatMap(producto-> productoService.delete(producto.getId()).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());

		
	}

}
