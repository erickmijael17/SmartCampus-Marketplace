-- Mantener las 5 categorias especificas, actualizar las existentes a estos nombres
UPDATE categorias SET nombre = 'Libros y material académico', descripcion = 'Libros, copias y material de estudio', icono = 'book' WHERE id = 1;
UPDATE categorias SET nombre = 'Tecnología y accesorios', descripcion = 'Laptops, calculadoras, cables', icono = 'laptop' WHERE id = 2;
UPDATE categorias SET nombre = 'Ropa y artículos personales', descripcion = 'Polos, poleras, mochilas', icono = 'shirt' WHERE id = 3;
UPDATE categorias SET nombre = 'Servicios estudiantiles', descripcion = 'Tutorias, impresiones, asesorias', icono = 'tools' WHERE id = 4;
UPDATE categorias SET nombre = 'Otros productos', descripcion = 'Artículos varios no categorizados', icono = 'box' WHERE id = 5;

-- Eliminar cualquier categoria que tenga id mayor a 5
DELETE FROM categorias WHERE id > 5;
