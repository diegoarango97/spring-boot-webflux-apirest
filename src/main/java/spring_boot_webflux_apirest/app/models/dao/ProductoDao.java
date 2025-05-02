package spring_boot_webflux_apirest.app.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.documents.Producto;


public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

	public Mono<Producto> findByNombre(String nombre);
	
	@Query("{'nombre': ?0}")
	public Mono<Producto> buscarPorNombre(String nombre);

}
