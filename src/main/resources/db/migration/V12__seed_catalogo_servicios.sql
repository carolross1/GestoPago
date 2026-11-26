INSERT INTO catalogo_productos (id_producto, id_servicio, id_cat_tipo_servicio, tipo_front, servicio, producto, precio, tipo_referencia, legend, fecha_actualizacion)
VALUES
(101, 1, 1, 2, 'CFE', 'CFE Suministrador de Servicios Basicos', '0.00', '1', 'Ingrese su numero de servicio de 12 a 30 digitos que aparece en su recibo de luz', NOW()),
(102, 2, 1, 1, 'Telcel', 'Telcel Recarga Tiempo Aire', '50.00', '1', 'Ingrese los 10 digitos de su numero celular Telcel', NOW()),
(103, 2, 1, 1, 'Telcel', 'Telcel Paquete Amigo Sin Limite', '100.00', '1', 'Ingrese los 10 digitos de su numero celular Telcel', NOW()),
(104, 3, 1, 4, 'Telmex', 'Telmex Telefonia e Internet Infinitum', '0.00', '1', 'Ingrese los 10 digitos de su numero de telefono Telmex', NOW()),
(105, 4, 1, 1, 'Movistar', 'Movistar Recarga Prepago', '50.00', '1', 'Ingrese su numero celular Movistar a 10 digitos', NOW()),
(106, 5, 1, 1, 'AT&T', 'AT&T Recarga / Plan Mas', '100.00', '1', 'Ingrese su numero celular AT&T a 10 digitos', NOW()),
(107, 6, 1, 2, 'Naturgy', 'Naturgy Gas Natural Mexico', '0.00', '1', 'Ingrese el numero de cuenta o poliza de su recibo', NOW()),
(108, 7, 1, 4, 'Sky', 'Sky / VeTV Prepago Television', '0.00', '1', 'Ingrese su numero de cuenta Sky o numero de tarjeta inteligente', NOW()),
(109, 8, 1, 4, 'Totalplay', 'Totalplay Telecomunicaciones', '0.00', '1', 'Ingrese el numero de cuenta Totalplay de 10 u 11 digitos', NOW()),
(110, 9, 1, 4, 'Izzi', 'Izzi Telecomunicaciones', '0.00', '1', 'Ingrese el numero de cuenta de 8 digitos de su estado de cuenta', NOW()),
(111, 10, 1, 3, 'Agua y Drenaje', 'Servicio de Agua Potable y Alcantarillado', '0.00', '1', 'Ingrese el numero de contrato o NIS que aparece en su factura', NOW()),
(112, 11, 1, 6, 'Televia', 'Televia TAG Peaje Carretero', '150.00', '1', 'Ingrese el numero de serie de su dispositivo TAG o numero de cuenta', NOW())
ON CONFLICT (id_producto) DO NOTHING;