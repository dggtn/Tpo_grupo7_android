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
                Log.d("getuser", user.getEmail());
                emailUsuario.setText(user.getEmail());
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
        loadUsuario();


//        Button button = view.findViewById(R.id.verDetalle);
//        button.setOnClickListener((view1) -> {
//            Navigation.findNavController(view1).navigate(R.id.curso, new Bundle());
//            ;
//            listView = view.findViewById(R.id.listView);
//            claseDisplayList = new ArrayList<>();
//            adapter = new ArrayAdapter<>(requireContext(),
//                    android.R.layout.simple_list_item_1,
//                    claseDisplayList);
//            listView.setAdapter(adapter);
//            loadclases();
//
//            listView.setOnItemClickListener((parent, v, position, id) -> {
//                String selectedClase = claseDisplayList.get(position);
//                String claseName = selectedClase.split(" - ")[0];
//
//            });
//        });


    }
}