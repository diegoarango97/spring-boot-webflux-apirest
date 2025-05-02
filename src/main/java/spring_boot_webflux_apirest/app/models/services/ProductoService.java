package spring_boot_webflux_apirest.app.models.services;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.documents.Categoria;
import spring_boot_webflux_apirest.app.models.documents.Producto;

public interface ProductoService {
	
	public Flux<Producto> findAll();
	
	public Flux<Producto> findAllUpperCaseName();
	
	public Flux<Producto> findAllUpperCaseNameRepeat();


	public Mono<Producto> findById(String id);
	
	public Mono<Producto> save (Producto producto);
	
	public Mono<Boolean> delete(String id);
	
	public Flux<Categoria> findAllCategoria();
	
	public Mono<Categoria> findCategoriaById(String id);
	
	public Mono<Categoria> save (Categoria categoria);
	
	public Mono<Producto> findByNombre(String nombre);
	
	public Mono<Producto> buscarPorNombre(String nombre);
	
	public Mono<Categoria> findCategoriaByNombre (String nombre);
	
	public Mono<Categoria> buscarCategoriaPorNombre(String nombre);

	
	

}
