package spring_boot_webflux_apirest.app.models.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import spring_boot_webflux_apirest.app.models.dao.CategoriaDao;
import spring_boot_webflux_apirest.app.models.dao.ProductoDao;
import spring_boot_webflux_apirest.app.models.documents.Categoria;
import spring_boot_webflux_apirest.app.models.documents.Producto;

@Service
public class ProductoServiceImpl implements ProductoService{
	
	private static final Logger log = LoggerFactory.getLogger(ProductoServiceImpl.class);
	
	@Autowired
	ProductoDao productoDao;
	
	@Autowired
	CategoriaDao categoriaDao;

	@Override
	public Flux<Producto> findAll() {
		return productoDao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return productoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		if(producto.getFechaCreacion() == null) {
			producto.setFechaCreacion(new Date());
		}
		return productoDao.save(producto);
	}

	@Override
	public Mono<Boolean> delete(String id) {
		return productoDao.findById(id).flatMap(producto -> {
			log.info("producto encontrado: " + producto.toString());
			return productoDao.delete(producto).thenReturn(Boolean.TRUE);
		}).switchIfEmpty(Mono.just(Boolean.FALSE));
	}

	@Override
	public Flux<Producto> findAllUpperCaseName() {
		return productoDao.findAll()
		 .map(producto -> {
			 producto.setNombre(producto.getNombre().toUpperCase());
			 return producto;
		 });
	}

	@Override
	public Flux<Producto> findAllUpperCaseNameRepeat() {
		return findAllUpperCaseName().repeat(5000);
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return categoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return categoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> save(Categoria categoria) {
		return categoriaDao.save(categoria);
	}

}
