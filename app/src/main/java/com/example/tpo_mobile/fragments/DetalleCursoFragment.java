package com.example.tpo_mobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.model.Clase;
import com.example.tpo_mobile.repository.GetClaseByIdCallback;
import com.example.tpo_mobile.services.GymService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetalleCursoFragment extends Fragment {

    @Inject
    GymService gymService;
    private Long idCurso;

    // COMPONENTES
    private TextView tituloTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("pantalla fragment", "On create pantalla fragment");
        super.onCreate(savedInstanceState);
        Bundle argumentos = getArguments();
        if(argumentos != null){
            this.idCurso = argumentos.getLong("idCurso");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_curso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        this.tituloTextView = view.findViewById(R.id.TituloNombreCurso);

        Button button2 = view.findViewById(R.id.reservaButton);
        button2.setOnClickListener((view2) -> {
            Navigation.findNavController(view2).navigate(R.id.alert_reservaste, new Bundle());
            ;
        });

        this.obtenerCurso();

    }

    private void obtenerCurso() {
        // mostrar spinner
        this.gymService.getClasePorId(this.idCurso, new GetClaseByIdCallback() {
            @Override
            public void onSuccess(Clase clase) {
                tituloTextView.setText(clase.getNombre());
            }

            @Override
            public void onError(Throwable error) {

            }
        });
    }

}
