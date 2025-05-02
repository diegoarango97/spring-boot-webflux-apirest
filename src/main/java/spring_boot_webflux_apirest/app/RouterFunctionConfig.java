package spring_boot_webflux_apirest.app;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import spring_boot_webflux_apirest.app.handler.ProductoHandler;

@Configuration
public class RouterFunctionConfig {

	/*
	 * Forma 1 de tener una API usango routerFunction
	 * 
	 * @Autowired private ProductoService productoService;
	 * 
	 * @Bean public RouterFunction<ServerResponse> routes(){ return
	 * route(GET("/api/v2/productos").or(GET("/api/v3/productos")), request -> {
	 * return ServerResponse.ok() .body(productoService.findAll(), Producto.class);
	 * }); }
	 */

	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
		return route(GET("/api/v2/productos").or(GET("/api/v3/productos")), handler::listar)
				.andRoute(GET("/api/v2/productos/{id}"), handler::ver)
				.andRoute(POST("/api/v2/productos/crear").and(contentType(MediaType.APPLICATION_JSON)),handler::crear)
				.andRoute(PUT("/api/v2/productos/{id}").and(contentType(MediaType.APPLICATION_JSON)), handler::editar)
				.andRoute(PUT("/api/versionProfesor/productos/{id}").and(contentType(MediaType.APPLICATION_JSON)), handler::editarFormaProfesor)
				.andRoute(DELETE("/api/v2/productos/{id}"), handler::eliminar)
				.andRoute(POST("/api/v2/productos/upload/{id}"),handler::upload)
				.andRoute(POST("/api/v2/productos/crearConFoto").and(contentType(MediaType.MULTIPART_FORM_DATA)),handler::crearConFoto);
	}

}
