UPDATE categorias SET nombre = 'Libros y material académico', descripcion = 'Libros, copias y material de estudio' WHERE id = 1;
UPDATE categorias SET nombre = 'Tecnología y accesorios', descripcion = 'Laptops, calculadoras, cables' WHERE id = 2;
UPDATE categorias SET nombre = 'Ropa y artículos personales', descripcion = 'Polos, poleras, mochilas' WHERE id = 3;
UPDATE categorias SET nombre = 'Servicios estudiantiles', descripcion = 'Tutorías, impresiones, asesorías' WHERE id = 4;
UPDATE categorias SET nombre = 'Otros productos', descripcion = 'Artículos varios no categorizados' WHERE id = 5;
DELETE FROM categorias WHERE id > 5;
