package org.freelasearch.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.freelasearch.R;
import org.freelasearch.dtos.DtoUsuario;
import org.freelasearch.tasks.AsyncTaskListener;
import org.freelasearch.tasks.impl.AsyncTaskFacebookUsuario;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREF_NAME = "SignupActivityPreferences";
    private CallbackManager callbackManager;
    private SharedPreferences sharedpreferences;
    private ProgressDialog mProgressDialog;

    private AsyncTaskFacebookUsuario mAsyncTaskFacebookUsuario;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_welcome);

        //Verifica se está logado
        sharedpreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isLogged = sharedpreferences.getInt("id", 0) != 0;

        //Verifica se está logado pelo facebook
        callbackManager = CallbackManager.Factory.create();
        //boolean loggedByFacebook = AccessToken.getCurrentAccessToken() != null;

        if (isLogged) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //Se não estiver logado, monta a tela de Welcome com o botão do fb para login/cadastro
        LoginButton loginButton = (LoginButton) findViewById(R.id.lb_facebook);
        List<String> permissionNeeds = Arrays.asList("public_profile", "email");
        loginButton.setReadPermissions(permissionNeeds);

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                try {
                                    DtoUsuario dto = new DtoUsuario();
                                    dto.setEmail(object.getString("email"));
                                    dto.setNome(object.getString("name"));
                                    dto.setUrlFoto("https://graph.facebook.com/" + object.getString("id") + "/picture?type=large");
                                    loginOrSignupFacebook(dto);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
                Log.v("WelcomeActivity", exception.getCause().toString());
            }
        });

        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);

        Button btnSignup = (Button) findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(this);

        TextView tvResetarSenha = (TextView) findViewById(R.id.tv_resetar_senha);
        tvResetarSenha.setOnClickListener(this);
    }

    private void loginOrSignupFacebook(DtoUsuario dto) {

        mAsyncTaskFacebookUsuario = new AsyncTaskFacebookUsuario();
        mAsyncTaskFacebookUsuario.setAsyncTaskListener(new AsyncTaskListener() {
            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(WelcomeActivity.this);
                mProgressDialog.setMessage("Conectando-se pelo Facebook...");
                mProgressDialog.show();
            }

            @Override
            public <T> void onComplete(T obj) {
                if (obj != null) {
                    DtoUsuario dtoUsuario = (DtoUsuario) obj;

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("id", dtoUsuario.getId());
                    editor.putString("nome", dtoUsuario.getNome());
                    editor.putString("email", dtoUsuario.getEmail());
                    editor.putString("profile_pic", dtoUsuario.getUrlFoto());
                    editor.commit();

                    Intent intent = new Intent(WelcomeActivity.this, PerfisActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                } else {
                    Log.e("FreelaSearch", "Retorno nulo ao tentar logar/registar via Facebook.");
                }
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("FreelaSearch", errorMsg);
            }
        });

        mAsyncTaskFacebookUsuario.execute(dto);
    }

    @Override
    public void onClick(View v) {
        Intent activity;
        switch (v.getId()) {
            case R.id.btn_login:
                activity = new Intent(this, LoginActivity.class);
                startActivity(activity);
                break;
            case R.id.btn_signup:
                activity = new Intent(this, SignupActivity.class);
                startActivity(activity);
                break;
            case R.id.tv_resetar_senha:
                activity = new Intent(this, ResetPasswordActivity.class);
                startActivity(activity);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAsyncTaskFacebookUsuario != null) {
            mAsyncTaskFacebookUsuario.cancel(true);
        }
    }
}
