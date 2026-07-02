-- Reasignar cualquier producto con categoria antigua (ID > 5) a la categoria 'Otros productos' (ID = 5)
UPDATE productos SET categoria_id = 5 WHERE categoria_id > 5;
