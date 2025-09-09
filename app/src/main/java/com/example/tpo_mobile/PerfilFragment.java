package com.example.tpo_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tpo_mobile.Repository.GetUserCallback;
import com.example.tpo_mobile.model.User;
import com.example.tpo_mobile.services.GymService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PerfilFragment extends Fragment {

    @Inject
    GymService gymService;

    private TextView emailUsuario;
    private TextView nameUsuario;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("Perfil", "On create perfil fragment");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_usuario_perfil, container, false);
    }


    private void loadUsuario() {
        Log.d("perfil", "Obteniendo usuarios");
        gymService.getUser(new GetUserCallback() {
            @Override
            public void onSuccess(User user) {
                emailUsuario.setText(user.getEmail());
                nameUsuario.setText(user.getName());
            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.emailUsuario = view.findViewById(R.id.email_text_view);
        this.nameUsuario = view.findViewById(R.id.name_text_view);
        loadUsuario();


    }
}