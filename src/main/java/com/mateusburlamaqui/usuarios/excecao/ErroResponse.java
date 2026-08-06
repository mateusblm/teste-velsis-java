package com.mateusburlamaqui.usuarios.excecao;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        LocalDateTime dataHora,
        int status,
        String mensagem,
        Map<String, String> campos
) {
}