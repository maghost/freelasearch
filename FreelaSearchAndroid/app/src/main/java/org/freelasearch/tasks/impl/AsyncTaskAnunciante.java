package org.freelasearch.tasks.impl;

import org.freelasearch.dtos.DtoAnunciante;
import org.freelasearch.service.AnuncianteService;
import org.freelasearch.tasks.AbstractAsyncTask;

import java.io.IOException;

public class AsyncTaskAnunciante extends AbstractAsyncTask<DtoAnunciante, Void> {

    @Override
    protected Void executeService(DtoAnunciante dtoAnunciante) throws IOException {
        AnuncianteService service = new AnuncianteService();
        service.save(dtoAnunciante);
        return null;
    }

}

