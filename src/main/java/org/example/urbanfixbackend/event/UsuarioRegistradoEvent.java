package org.example.urbanfixbackend.event;

import org.example.urbanfixbackend.entity.Usuario;
import org.springframework.context.ApplicationEvent;

public class UsuarioRegistradoEvent extends ApplicationEvent {

    private final Usuario usuario;

    public UsuarioRegistradoEvent(Usuario usuario) {
        super(usuario);
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
