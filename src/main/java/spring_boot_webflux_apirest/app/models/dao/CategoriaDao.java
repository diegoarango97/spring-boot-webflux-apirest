package spring_boot_webflux_apirest.app.models.dao;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.documents.Categoria;


public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

	public Mono<Categoria> findByNombre(String nombre);
	
	@Query("{'nombre': ?0}")
	public Mono<Categoria> buscarPorNombre(String nombre);
}
