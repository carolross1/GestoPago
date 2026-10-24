package com.proyecto.servicios.client;

import com.proyecto.servicios.config.GestoPagoServiceFeignConfig;
import com.proyecto.servicios.model.gestopago.tx.GestoPagoTxResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "gestoPagoTxClient",
        url = "${gestopago.auth.url}",
        configuration = GestoPagoServiceFeignConfig.class
)
public interface GestoPagoTxClient {

    @PostMapping(
            value = "/sistema/service/sendTx.do",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    GestoPagoTxResponse enviarTransaccion(
            @RequestParam("random") String random,
            @RequestParam("signed") String signed
    );
}
