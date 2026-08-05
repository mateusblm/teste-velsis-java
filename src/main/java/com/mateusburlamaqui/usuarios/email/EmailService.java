package com.mateusburlamaqui.usuarios.email;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void enviar(String destinatario, String mensagem) {
        System.out.printf("E-mail enviado para %s: %s%n",destinatario,mensagem);
    }
}