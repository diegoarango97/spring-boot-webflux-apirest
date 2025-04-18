package spring_boot_webflux_apirest.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import spring_boot_webflux_apirest.app.models.documents.Producto;


public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

}
