package org.freelasearch.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.satsuware.usefulviews.LabelledSpinner;

import org.freelasearch.R;
import org.freelasearch.dtos.DtoAnunciante;
import org.freelasearch.dtos.DtoAnuncio;
import org.freelasearch.dtos.DtoCategoria;
import org.freelasearch.dtos.DtoLocalizacao;
import org.freelasearch.dtos.DtoUsuario;
import org.freelasearch.tasks.AsyncTaskListener;
import org.freelasearch.tasks.impl.AsyncTaskAnuncio;
import org.freelasearch.tasks.impl.AsyncTaskListaCategoria;
import org.freelasearch.utils.CategoriaUtils;
import org.freelasearch.utils.EstadoUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnuncioActivity extends AppCompatActivity implements LabelledSpinner.OnItemChosenListener, View.OnClickListener {

    private static final String PREF_NAME = "SignupActivityPreferences";

    private Toolbar mToolbar;

    private AnuncioActivity activity = this;
    private ProgressDialog progress;

    private List<EstadoUtils> estadoUtils = new EstadoUtils().preencherEstados();
    private String ufSelecionada;

    private List<DtoCategoria> listDtoCategoria;
    private Integer categoriaSelecionada;
    private LabelledSpinner categoriaSpinner;
    private AsyncTaskListaCategoria mAsyncTaskListaCategoria;

    private DtoAnuncio dtoAnuncio;
    private AsyncTaskAnuncio mAsyncTaskAnuncio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio);

        SharedPreferences sharedpreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Bundle b = getIntent().getExtras();
        dtoAnuncio = b != null ? (DtoAnuncio) b.getSerializable("anuncio") : null;

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(dtoAnuncio == null ? "Criar Anúncio" : "Editar Anúncio");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        categoriaSpinner = (LabelledSpinner) findViewById(R.id.categoria_anuncio);
        categoriaSpinner.setOnItemChosenListener(this);
        buscarCategorias();

        LabelledSpinner estadoSpinner = (LabelledSpinner) findViewById(R.id.estado_anuncio);
        estadoSpinner.setItemsArray(estadoUtils);
        estadoSpinner.setOnItemChosenListener(this);

        AppCompatButton btnCadastrarAnuncio = (AppCompatButton) findViewById(R.id.btn_cadastrar_anuncio);
        btnCadastrarAnuncio.setOnClickListener(this);

        AppCompatButton btnEditarAnuncio = (AppCompatButton) findViewById(R.id.btn_editar_anuncio);
        btnEditarAnuncio.setOnClickListener(this);

        if (dtoAnuncio != null) {
            btnEditarAnuncio.setVisibility(View.VISIBLE);

            // Pegando os campos da tela
            TextInputEditText tituloAnuncio = (TextInputEditText) findViewById(R.id.titulo_anuncio);
            TextInputEditText cidadeAnuncio = (TextInputEditText) findViewById(R.id.cidade_anuncio);
            LabelledSpinner estadoAnuncio = (LabelledSpinner) findViewById(R.id.estado_anuncio);
            TextInputEditText descricaoAnuncio = (TextInputEditText) findViewById(R.id.descricao_anuncio);

            // Preenchendo os dados conforme dto (Edição)
            tituloAnuncio.setText(dtoAnuncio.getTitulo());

            //Categoria não pode preencher aqui pois pode não ter terminado a thread de buscar a lista

            cidadeAnuncio.setText(dtoAnuncio.getLocalizacao().getCidade());

            int posicaoEstado = new EstadoUtils().getPositionByUf(dtoAnuncio.getLocalizacao().getEstado());
            estadoAnuncio.setSelection(posicaoEstado);
            ufSelecionada = estadoUtils.get(posicaoEstado).getUf();

            descricaoAnuncio.setText(dtoAnuncio.getDescricao());
        } else {
            btnCadastrarAnuncio.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
        switch (labelledSpinner.getId()) {
            case R.id.estado_anuncio:
                ufSelecionada = estadoUtils.get(position).getUf();
                break;
            case R.id.categoria_anuncio:
                categoriaSelecionada = listDtoCategoria.get(position).getId();
                break;
        }

    }

    @Override
    public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cadastrar_anuncio:
                cadastrarAnuncio();
                break;
            case R.id.btn_editar_anuncio:
                editarAnuncio();
                break;
        }
    }

    public void buscarCategorias() {
        mAsyncTaskListaCategoria = new AsyncTaskListaCategoria();
        mAsyncTaskListaCategoria.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
            }

            @Override
            public <T> void onComplete(T obj) {
                listDtoCategoria = new ArrayList<>();
                listDtoCategoria.addAll((List<DtoCategoria>) obj);

                if (categoriaSpinner == null) {
                    categoriaSpinner = (LabelledSpinner) activity.findViewById(R.id.categoria_anuncio);
                }
                categoriaSpinner.setItemsArray(CategoriaUtils.getNamesByList(listDtoCategoria));

                if (dtoAnuncio != null && dtoAnuncio.getCategoria() != null) {
                    categoriaSpinner.setSelection(CategoriaUtils.getPositionById(listDtoCategoria, dtoAnuncio.getCategoria().getId()));
                    categoriaSelecionada = dtoAnuncio.getCategoria().getId();
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getApplicationContext(), "Erro ao buscar categorias", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        Map<String, String> filtro = new HashMap<>();
        filtro.put("status", "1"); // Categorias ativas
        mAsyncTaskListaCategoria.execute(filtro);
    }

    private void cadastrarAnuncio() {
        if (!isValid()) {
            Toast.makeText(getApplicationContext(), R.string.required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        preencheDto(dtoAnuncio);

        mAsyncTaskAnuncio = new AsyncTaskAnuncio();
        mAsyncTaskAnuncio.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
                progress = new ProgressDialog(activity);
                progress.setMessage("Cadastrando anúncio, aguarde...");
                progress.show();
            }

            @Override
            public <T> void onComplete(T obj) {
                progress.dismiss();

                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra("idNavigationItem", R.id.nav_meus_anuncios);
                intent.putExtra("msgExtras", "Anúncio cadastrado com sucesso.");
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(activity, "Falha ao cadastrar o anúncio.", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });

        mAsyncTaskAnuncio.execute(dtoAnuncio);
    }

    private void editarAnuncio() {
        if (!isValid()) {
            Toast.makeText(getApplicationContext(), R.string.required_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        preencheDto(dtoAnuncio);

        mAsyncTaskAnuncio = new AsyncTaskAnuncio();
        mAsyncTaskAnuncio.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
                progress = new ProgressDialog(activity);
                progress.setMessage("Editando anúncio, aguarde...");
                progress.show();
            }

            @Override
            public <T> void onComplete(T obj) {
                progress.dismiss();

                Intent intent = new Intent(activity, AnuncioDetalharActivity.class);
                // TODO: Após arrumar lógicas dessa tela, verificar necessidade de passar o dtoAnuncio ao invés do id
                intent.putExtra("id", dtoAnuncio.getId());
                intent.putExtra("backMeusAnuncios", true);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(activity, "Falha ao editar o anúncio.", Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });

        mAsyncTaskAnuncio.execute(dtoAnuncio);
    }

    private boolean isValid() {
        if (((TextInputEditText) findViewById(R.id.titulo_anuncio)).getText().toString().trim().isEmpty()
                || categoriaSelecionada == null
                || ((TextInputEditText) findViewById(R.id.cidade_anuncio)).getText().toString().trim().isEmpty()
                || ufSelecionada == null
                || ((TextInputEditText) findViewById(R.id.descricao_anuncio)).getText().toString().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private void preencheDto(DtoAnuncio dto) {
        if (dto == null) {
            dto = new DtoAnuncio();
        }

        dto.setTitulo(((TextInputEditText) findViewById(R.id.titulo_anuncio)).getText().toString().trim());

        DtoCategoria dtoCategoria = new DtoCategoria();
        dtoCategoria.setId(categoriaSelecionada);
        dto.setCategoria(dtoCategoria);

        DtoLocalizacao dtoLocalizacao = new DtoLocalizacao();
        dtoLocalizacao.setCidade(((TextInputEditText) findViewById(R.id.cidade_anuncio)).getText().toString().trim());
        dtoLocalizacao.setEstado(ufSelecionada);
        dto.setLocalizacao(dtoLocalizacao);

        dto.setDescricao(((TextInputEditText) findViewById(R.id.descricao_anuncio)).getText().toString().trim());

        DtoAnunciante dtoAnunciante = new DtoAnunciante();
        dtoAnunciante.setId(1); // TODO: sharedpreferences.getInt("id", 0) = Aqui está pegando o id do Usuário, deve ser do Anunciante
        DtoUsuario dtoUsuario = new DtoUsuario();
        dtoUsuario.setId(1);
        dtoAnunciante.setUsuario(dtoUsuario);
        dto.setAnunciante(dtoAnunciante);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAsyncTaskAnuncio != null) {
            mAsyncTaskAnuncio.cancel(true);
        }
        if (mAsyncTaskListaCategoria != null) {
            mAsyncTaskListaCategoria.cancel(true);
        }
    }

}
