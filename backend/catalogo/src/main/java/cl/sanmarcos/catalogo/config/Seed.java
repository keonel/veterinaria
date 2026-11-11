package cl.sanmarcos.catalogo.config;

import cl.sanmarcos.catalogo.model.Producto;
import cl.sanmarcos.catalogo.repository.ProductosRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Productos iniciales de la tienda (los mismos que antes vivían en el frontend). */
@Component
public class Seed implements CommandLineRunner {
  private final ProductosRepository productos;

  public Seed(ProductosRepository productos) {
    this.productos = productos;
  }

  @Override
  public void run(String... args) {
    if (productos.count() > 0) return;
    crear(
        "SM001",
        "Milpro 2.5 mg - Antiparasitario para Perros y Gatos",
        "Medicamentos y Suplementos",
        "/assets/img/milpro-2.png",
        15000,
        10,
        3,
        "Milpro es un medicamento antiparasitario que ayuda a eliminar y prevenir infestaciones"
            + " de parásitos internos en perros y gatos. Contiene praziquantel y milbemicina"
            + " oxima, que actúan contra una amplia gama de parásitos intestinales.");
    crear(
        "SM002",
        "FLT 150 ML SHAMPOO MEDICADO PERROS Y GATOS",
        "Higiene y Cuidado",
        "/assets/img/pixelcut-export-57612.png",
        12000,
        15,
        5,
        "FLT es un shampoo medicado diseñado para perros y gatos, formulado para tratar"
            + " afecciones de la piel como dermatitis, alergias y problemas de caspa. Contiene"
            + " ingredientes activos que ayudan a aliviar la irritación y mantener la piel"
            + " saludable.");
    crear(
        "SM003",
        "Alimento para conejos Puki 1 kg",
        "Alimentos",
        "/assets/img/Alimento-para-conejos-Puki-1-kg.jpg",
        8000,
        20,
        5,
        "Puki es un alimento balanceado para conejos, formulado con ingredientes de alta calidad"
            + " que proporcionan los nutrientes esenciales para mantener la salud y vitalidad de"
            + " tu mascota. Contiene fibras, vitaminas y minerales necesarios para su"
            + " desarrollo.");
  }

  private void crear(
      String codigo,
      String nombre,
      String categoria,
      String imagen,
      int precio,
      int stock,
      int stockCritico,
      String descripcion) {
    Producto p = new Producto();
    p.setCodigo(codigo);
    p.setTipo("producto");
    p.setNombre(nombre);
    p.setCategoria(categoria);
    p.setImagen(imagen);
    p.setPrecio(precio);
    p.setStockActual(stock);
    p.setStockCritico(stockCritico);
    p.setDescripcion(descripcion);
    productos.save(p);
  }
}
