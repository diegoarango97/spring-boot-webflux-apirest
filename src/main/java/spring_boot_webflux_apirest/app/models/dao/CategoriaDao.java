package spring_boot_webflux_apirest.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import spring_boot_webflux_apirest.app.models.documents.Categoria;


public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

}
