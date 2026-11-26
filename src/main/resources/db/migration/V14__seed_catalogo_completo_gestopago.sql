-- Migracion V14: Catalogo completo oficial de servicios GestoPago con idServicio real
-- Cada id_servicio corresponde exactamente a https://gestopago.portalventas.net/sistema/images/gestopago/servicios/{id_servicio}.png
DELETE FROM catalogo_productos;

INSERT INTO catalogo_productos (id_producto, id_servicio, id_cat_tipo_servicio, tipo_front, servicio, producto, precio, tipo_referencia, legend, fecha_actualizacion)
VALUES
-- ============================================================
-- CATEGORIA 1: TELEFONIA Y RECARGAS (tipo_front = 1)
-- ============================================================
(1331, 133, 1, 1, 'Telcel', 'Telcel Recarga Tiempo Aire $50', '50.00', '1', 'Ingrese los 10 digitos de su numero celular Telcel', NOW()),
(1332, 133, 1, 1, 'Telcel', 'Telcel Paquete Amigo Sin Limite $100', '100.00', '1', 'Ingrese los 10 digitos de su numero celular Telcel', NOW()),
(1333, 133, 1, 1, 'Telcel', 'Telcel Paquete Amigo Sin Limite $200', '200.00', '1', 'Ingrese los 10 digitos de su numero celular Telcel', NOW()),
(1151, 115, 1, 1, 'AT&T', 'AT&T Recarga Prepago $50', '50.00', '1', 'Ingrese los 10 digitos de su numero celular AT&T', NOW()),
(1152, 115, 1, 1, 'AT&T', 'AT&T Plan Mas / Recarga $100', '100.00', '1', 'Ingrese los 10 digitos de su numero celular AT&T', NOW()),
(1153, 115, 1, 1, 'AT&T', 'AT&T Recarga $200', '200.00', '1', 'Ingrese los 10 digitos de su numero celular AT&T', NOW()),
(1021, 102, 1, 1, 'Movistar', 'Movistar Recarga Prepago $50', '50.00', '1', 'Ingrese los 10 digitos de su numero celular Movistar', NOW()),
(1022, 102, 1, 1, 'Movistar', 'Movistar Paquete Ilimitado $100', '100.00', '1', 'Ingrese los 10 digitos de su numero celular Movistar', NOW()),
(1161, 116, 1, 1, 'Unefon', 'Unefon Recarga Ilimitada $50', '50.00', '1', 'Ingrese los 10 digitos de su numero celular Unefon', NOW()),
(1162, 116, 1, 1, 'Unefon', 'Unefon Recarga Ilimitada $100', '100.00', '1', 'Ingrese los 10 digitos de su numero celular Unefon', NOW()),
(1041, 104, 1, 1, 'Virgin Mobile', 'Virgin Mobile Recarga $100', '100.00', '1', 'Ingrese los 10 digitos de su numero Virgin Mobile', NOW()),
(1011, 101, 1, 1, 'Tuenti', 'Tuenti Recarga Combo $100', '100.00', '1', 'Ingrese los 10 digitos de su numero celular Tuenti', NOW()),
(1531, 153, 1, 1, 'CFE Internet y Telefonia', 'CFE TEIT Recarga Paquete $100', '100.00', '1', 'Ingrese los 10 digitos de su linea CFE TEIT', NOW()),
(781,  78,  1, 1, 'Oui Movil', 'Oui Recarga Prepago $100', '100.00', '1', 'Ingrese los 10 digitos de su linea Oui Movil', NOW()),
(881,  88,  1, 1, 'Netwey Movil', 'Netwey Movil Recarga $150', '150.00', '1', 'Ingrese los 10 digitos de su linea Netwey Movil', NOW()),
(1401, 140, 1, 1, 'LikePhone', 'LikePhone Recarga $100', '100.00', '1', 'Ingrese los 10 digitos de su numero LikePhone', NOW()),
(1451, 145, 1, 1, 'Red Aguila', 'Aguila SIM Recarga $100', '100.00', '1', 'Ingrese los 10 digitos de su numero Aguila SIM', NOW()),
(1571, 157, 1, 1, 'ComparTfon', 'ComparTfon Recarga $100', '100.00', '1', 'Ingrese los 10 digitos de su numero ComparTfon', NOW()),
(1611, 161, 1, 1, 'Simon Movil', 'Simon Recarga Prepago $100', '100.00', '1', 'Ingrese los 10 digitos de su numero Simon', NOW()),

-- ============================================================
-- CATEGORIA 2: LUZ Y GAS (tipo_front = 2)
-- ============================================================
(1661, 166, 2, 2, 'CFE', 'CFE Suministrador de Servicios Basicos', '0.00', '2', 'Ingrese su numero de servicio de 12 a 30 digitos de su recibo CFE', NOW()),
(641,  64,  2, 2, 'Naturgy', 'Naturgy Gas Natural Mexico', '0.00', '2', 'Ingrese el numero de cuenta o poliza de su recibo Naturgy', NOW()),
(1281, 128, 2, 2, 'Z Gas', 'Pago de Servicio Z Gas', '0.00', '2', 'Ingrese su referencia de pago Z Gas', NOW()),
(1841, 184, 2, 2, 'Compania Mexicana de Gas', 'Pago de Recibo Gas Natural', '0.00', '2', 'Ingrese su numero de contrato de Compania Mexicana de Gas', NOW()),

-- ============================================================
-- CATEGORIA 3: AGUA Y SANEAMIENTO (tipo_front = 3)
-- ============================================================
(551,  55,  3, 3, 'Agua y Drenaje Monterrey', 'Servicios de Agua y Drenaje de Monterrey', '0.00', '2', 'Ingrese el NIS o numero de contrato de su recibo', NOW()),
(771,  77,  3, 3, 'SIAPA', 'SIAPA Agua Potable Guadalajara', '0.00', '2', 'Ingrese su cuenta contrato SIAPA', NOW()),
(971,  97,  3, 3, 'SEGIAGUA CDMX', 'Sistema de Aguas de la Ciudad de Mexico', '0.00', '2', 'Ingrese su linea de captura SEGIAGUA', NOW()),
(541,  54,  3, 3, 'JMAS Chihuahua', 'Junta Municipal de Agua Chihuahua', '0.00', '2', 'Ingrese su numero de cuenta JMAS', NOW()),
(561,  56,  3, 3, 'AGUAKAN', 'Aguakan Quintana Roo', '0.00', '2', 'Ingrese su numero de cliente Aguakan', NOW()),
(601,  60,  3, 3, 'CMAPAS', 'CMAPAS Salamanca Agua Potable', '0.00', '2', 'Ingrese su numero de cuenta CMAPAS', NOW()),
(661,  66,  3, 3, 'JUMAPA Celaya', 'JUMAPA Agua Potable Celaya', '0.00', '2', 'Ingrese su numero de contrato JUMAPA', NOW()),
(1111, 111, 3, 3, 'AMD Durango', 'Aguas del Municipio de Durango', '0.00', '2', 'Ingrese su numero de cuenta AMD', NOW()),
(1751, 175, 3, 3, 'CESPT Tijuana', 'CESPT Agua Potable Tijuana', '0.00', '2', 'Ingrese su numero de cuenta CESPT', NOW()),
(1761, 176, 3, 3, 'SEAPAL Vallarta', 'SEAPAL Puerto Vallarta', '0.00', '2', 'Ingrese su numero de cuenta SEAPAL', NOW()),
(1771, 177, 3, 3, 'INTERAPAS', 'INTERAPAS San Luis Potosi', '0.00', '2', 'Ingrese su numero de cuenta INTERAPAS', NOW()),
(1851, 185, 3, 3, 'JAD Matamoros', 'Junta de Aguas Matamoros', '0.00', '2', 'Ingrese su numero de cuenta JAD', NOW()),
(1861, 186, 3, 3, 'COMAPA Nuevo Laredo', 'COMAPA Agua Potable Nuevo Laredo', '0.00', '2', 'Ingrese su numero de cuenta COMAPA', NOW()),
(1871, 187, 3, 3, 'Aguas de Saltillo', 'Pago de Servicio Aguas de Saltillo', '0.00', '2', 'Ingrese su numero de contrato Aguas de Saltillo', NOW()),

-- ============================================================
-- CATEGORIA 4: INTERNET Y TV (tipo_front = 4)
-- ============================================================
(1071, 107, 4, 4, 'Telmex', 'Telmex Telefonia e Internet Infinitum', '0.00', '2', 'Ingrese los 10 digitos de su numero telefonico Telmex', NOW()),
(851,  85,  4, 4, 'Totalplay', 'Totalplay Fibra Optica y TV', '0.00', '2', 'Ingrese su numero de cuenta Totalplay de 10 digitos', NOW()),
(761,  76,  4, 4, 'izzi', 'izzi Internet, Telefonia y TV', '0.00', '2', 'Ingrese su numero de cuenta izzi', NOW()),
(1671, 167, 4, 4, 'Megacable', 'Megacable Internet y Television', '0.00', '2', 'Ingrese su numero de suscriptor Megacable', NOW()),
(1681, 168, 4, 4, 'Sky', 'Sky / VeTV Prepago Television Satelital', '0.00', '2', 'Ingrese su numero de cuenta Sky o tarjeta inteligente', NOW()),
(611,  61,  4, 4, 'Dish', 'Dish Mexico Television Satelital', '0.00', '2', 'Ingrese su referencia de pago Dish', NOW()),
(1831, 183, 4, 4, 'Star TV', 'Star TV Television Satelital', '0.00', '2', 'Ingrese su numero de suscriptor Star TV', NOW()),
(571,  57,  4, 4, 'Axtel', 'Axtel Internet y Telefonia', '0.00', '2', 'Ingrese su numero de cuenta Axtel', NOW()),
(741,  74,  4, 4, 'Telnor', 'Telnor Internet y Telefonia', '0.00', '2', 'Ingrese los 10 digitos de su linea Telnor', NOW()),
(671,  67,  4, 4, 'Maxcom', 'Maxcom Telecomunicaciones', '0.00', '2', 'Ingrese su numero de cuenta Maxcom', NOW()),
(1261, 126, 4, 4, 'Netwey', 'Netwey Internet Hogar', '0.00', '2', 'Ingrese su numero de servicio Netwey', NOW()),
(1301, 130, 4, 4, 'NetTV', 'NetTV Television Prepago', '0.00', '2', 'Ingrese su numero de suscriptor NetTV', NOW()),
(421,  42,  4, 4, 'Newww', 'Newww Internet Inalambrico', '0.00', '2', 'Ingrese su numero de cuenta Newww', NOW()),

-- ============================================================
-- CATEGORIA 5: STREAMING, GAMING Y TARJETAS DE REGALO (tipo_front = 5)
-- ============================================================
(1781, 178, 5, 5, 'Netflix', 'Tarjeta de Regalo Netflix $300', '300.00', '2', 'Ingrese su correo o referencia de cliente', NOW()),
(711,  71,  5, 5, 'Amazon Mexico', 'Tarjeta de Regalo Amazon $300', '300.00', '2', 'Ingrese su referencia de cliente Amazon', NOW()),
(1431, 143, 5, 5, 'PlayStation', 'PlayStation Store Gift Card $200', '200.00', '2', 'Ingrese su referencia de cliente PlayStation', NOW()),
(1441, 144, 5, 5, 'Xbox Game Pass', 'Xbox Game Pass Ultimate $249', '249.00', '2', 'Ingrese su referencia de cuenta Xbox', NOW()),
(1411, 141, 5, 5, 'Nintendo eShop', 'Tarjeta Nintendo eShop $300', '300.00', '2', 'Ingrese su referencia de cuenta Nintendo', NOW()),
(1471, 147, 5, 5, 'Roblox', 'Tarjeta de Regalo Roblox $200', '200.00', '2', 'Ingrese su referencia de cuenta Roblox', NOW()),
(1341, 134, 5, 5, 'Apple iTunes', 'Tarjeta App Store & iTunes $200', '200.00', '2', 'Ingrese su referencia Apple ID', NOW()),
(1421, 142, 5, 5, 'Deezer', 'Suscripcion Deezer Premium $129', '129.00', '2', 'Ingrese su referencia de cuenta Deezer', NOW()),
(1511, 151, 5, 5, 'Cinepolis', 'Cinepolis Entradas Digitales $200', '200.00', '2', 'Ingrese su referencia Cinepolis', NOW()),
(1181, 118, 5, 5, 'Cinepolis Klic', 'Cinepolis Klic Peliculas $150', '150.00', '2', 'Ingrese su referencia Cinepolis Klic', NOW()),
(701,  70,  5, 5, 'Blim TV', 'Suscripcion Blim TV $109', '109.00', '2', 'Ingrese su referencia Blim', NOW()),
(1291, 129, 5, 5, 'Rappi', 'Tarjeta de Regalo Rappi $200', '200.00', '2', 'Ingrese su referencia de cliente Rappi', NOW()),
(1581, 158, 5, 5, 'Pines Digitales', 'Google Play / Uber / Spotify PIN $200', '200.00', '2', 'Ingrese su referencia de pin digital', NOW()),
(1491, 149, 5, 5, 'Kaspersky', 'Licencia Kaspersky Antivirus $399', '399.00', '2', 'Ingrese su referencia de licencia Kaspersky', NOW()),

-- ============================================================
-- CATEGORIA 6: PEAJES Y TRANSPORTE (tipo_front = 6)
-- ============================================================
(381,  38,  6, 6, 'Televia', 'Televia TAG Peaje Carretero $150', '150.00', '2', 'Ingrese el numero de serie de su TAG Televia', NOW()),
(1541, 154, 6, 6, 'PASE', 'Recarga TAG PASE $200', '200.00', '2', 'Ingrese el numero de su TAG PASE', NOW()),
(631,  63,  6, 6, 'Omnibus de Mexico', 'Pago de Boleto Omnibus de Mexico', '0.00', '2', 'Ingrese su clave de reservacion Omnibus de Mexico', NOW()),
(1741, 174, 6, 6, 'Transpais', 'Pago de Boleto Transpais', '0.00', '2', 'Ingrese su codigo de reservacion Transpais', NOW()),
(1911, 191, 6, 6, 'Cabify', 'Saldo Digital Cabify $150', '150.00', '2', 'Ingrese su referencia de cuenta Cabify', NOW()),

-- ============================================================
-- CATEGORIA 7: GOBIERNO, CATALOGOS Y FINANCIEROS (tipo_front = 7)
-- ============================================================
(531,  53,  7, 7, 'INFONAVIT', 'Pago de Credito INFONAVIT', '0.00', '2', 'Ingrese su numero de credito INFONAVIT de 10 digitos', NOW()),
(941,  94,  7, 7, 'Gobierno CDMX', 'Tesoreria Gobierno de la Ciudad de Mexico', '0.00', '2', 'Ingrese su linea de captura de Tesoreria CDMX', NOW()),
(1881, 188, 7, 7, 'Gobierno de Queretaro', 'Pago de Contribuciones Estado de Queretaro', '0.00', '2', 'Ingrese su linea de captura Gobierno de Queretaro', NOW()),
(651,  65,  7, 7, 'Gobierno de Chihuahua', 'Pago de Derechos Estado de Chihuahua', '0.00', '2', 'Ingrese su referencia Gobierno de Chihuahua', NOW()),
(1121, 112, 7, 7, 'Gobierno de Zacatecas', 'Pago de Contribuciones Zacatecas', '0.00', '2', 'Ingrese su linea de captura Gobierno de Zacatecas', NOW()),
(1641, 164, 7, 7, 'Coppel', 'Abono a Credito Coppel', '0.00', '2', 'Ingrese su numero de cliente Coppel', NOW()),
(1631, 163, 7, 7, 'Mercado Pago', 'Deposito / Pago Mercado Pago', '0.00', '2', 'Ingrese su referencia de pago Mercado Pago', NOW()),
(751,  75,  7, 7, 'Paynet Openpay', 'Pago Referenciado Paynet', '0.00', '2', 'Ingrese su referencia Paynet', NOW()),
(1251, 125, 7, 7, 'Paysafecard', 'PIN Paysafecard $250', '250.00', '2', 'Ingrese su referencia Paysafecard', NOW()),
(1891, 189, 7, 7, 'Sofipa', 'Pago de Credito Sofipa', '0.00', '2', 'Ingrese su numero de credito Sofipa', NOW()),
(791,  79,  7, 7, 'Avon', 'Pago de Factura Avon', '0.00', '2', 'Ingrese su numero de registro de representante Avon', NOW()),
(801,  80,  7, 7, 'Oriflame', 'Pago de Pedido Oriflame', '0.00', '2', 'Ingrese su numero de socio Oriflame', NOW()),
(811,  81,  7, 7, 'Yves Rocher', 'Pago de Pedido Yves Rocher', '0.00', '2', 'Ingrese su numero de asesora Yves Rocher', NOW()),
(821,  82,  7, 7, 'SwissJust', 'Pago de Pedido SwissJust', '0.00', '2', 'Ingrese su numero de consultora SwissJust', NOW()),
(831,  83,  7, 7, 'Fuller', 'Pago de Factura Fuller Cosmetics', '0.00', '2', 'Ingrese su numero de cuenta Fuller', NOW()),
(861,  86,  7, 7, 'Tupperware', 'Pago de Pedido Tupperware', '0.00', '2', 'Ingrese su referencia de vendedora Tupperware', NOW()),
(1211, 121, 7, 7, 'Arabela', 'Pago de Factura Arabela', '0.00', '2', 'Ingrese su numero de dama Arabela', NOW()),
(1221, 122, 7, 7, 'Ilusion', 'Pago de Pedido Ilusion', '0.00', '2', 'Ingrese su numero de experta Ilusion', NOW()),
(1231, 123, 7, 7, 'L''BEL', 'Pago de Pedido L''BEL / Belcorp', '0.00', '2', 'Ingrese su codigo de consultora Belcorp', NOW()),
(1271, 127, 7, 7, 'Natura', 'Pago de Factura Natura', '0.00', '2', 'Ingrese su numero de consultora Natura', NOW()),
(1311, 131, 7, 7, 'Yanbal', 'Pago de Pedido Yanbal', '0.00', '2', 'Ingrese su codigo de consultora Yanbal', NOW()),
(1321, 132, 7, 7, 'Herbalife Nutrition', 'Pago de Pedido Herbalife', '0.00', '2', 'Ingrese su ID de distribuidor Herbalife', NOW()),
(1561, 156, 7, 7, 'Jafra', 'Pago de Factura Jafra', '0.00', '2', 'Ingrese su numero de consultora Jafra', NOW()),
(1731, 173, 7, 7, 'Terramar Brands', 'Pago de Pedido Terramar', '0.00', '2', 'Ingrese su numero de kit Terramar', NOW()),
(1921, 192, 7, 7, 'Stanhome', 'Pago de Factura Stanhome', '0.00', '2', 'Ingrese su numero de consejera Stanhome', NOW()),
(1931, 193, 7, 7, 'Zermat', 'Pago de Pedido Zermat', '0.00', '2', 'Ingrese su numero de emprendedora Zermat', NOW()),
(1811, 181, 7, 7, 'Smart Fit', 'Pago de Mensualidad Smart Fit', '0.00', '2', 'Ingrese su referencia de membresia Smart Fit', NOW()),
(1461, 146, 7, 7, 'Krispy Kreme', 'Tarjeta de Regalo Krispy Kreme $150', '150.00', '2', 'Ingrese su referencia de cliente', NOW()),
(1901, 190, 7, 7, 'Alsea Wow Rewards', 'Tarjeta Alsea Wow Rewards $200', '200.00', '2', 'Ingrese su referencia Wow Rewards', NOW()),
(1481, 148, 7, 7, 'FacturaFiel', 'Compra de Timbres FacturaFiel', '0.00', '2', 'Ingrese su referencia FacturaFiel', NOW()),
(841,  84,  7, 7, 'Reforma', 'Suscripcion Periodico Reforma', '0.00', '2', 'Ingrese su numero de suscriptor Reforma', NOW()),
(691,  69,  7, 7, 'MexBet', 'Recarga de Saldo MexBet $200', '200.00', '2', 'Ingrese su ID de usuario MexBet', NOW()),
(1351, 135, 7, 7, 'AME Shop', 'Pago de Pedido AME Shop', '0.00', '2', 'Ingrese su referencia AME Shop', NOW())
ON CONFLICT (id_producto) DO UPDATE SET
    id_servicio = EXCLUDED.id_servicio,
    id_cat_tipo_servicio = EXCLUDED.id_cat_tipo_servicio,
    tipo_front = EXCLUDED.tipo_front,
    servicio = EXCLUDED.servicio,
    producto = EXCLUDED.producto,
    precio = EXCLUDED.precio,
    tipo_referencia = EXCLUDED.tipo_referencia,
    legend = EXCLUDED.legend,
    fecha_actualizacion = EXCLUDED.fecha_actualizacion;
