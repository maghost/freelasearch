package org.freelasearch.tasks.impl;

import org.freelasearch.dtos.DtoUsuario;
import org.freelasearch.service.UsuarioService;
import org.freelasearch.tasks.AbstractAsyncTask;

import java.io.IOException;

public class AsyncTaskUsuario extends AbstractAsyncTask<DtoUsuario, DtoUsuario> {

    @Override
    protected DtoUsuario executeService(DtoUsuario dtoUsuario) throws IOException {
        UsuarioService service = new UsuarioService();
        return service.save(dtoUsuario);
    }

}

