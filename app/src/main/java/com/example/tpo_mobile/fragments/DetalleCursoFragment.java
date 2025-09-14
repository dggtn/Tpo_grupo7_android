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

public class DetalleCursoFragment extends Fragment {
    private String nombreCurso;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("pantalla fragment", "On create pantalla fragment");
        super.onCreate(savedInstanceState);
        Bundle argumentos = getArguments();
        if(argumentos != null){
            nombreCurso = argumentos.getString("nombreCurso");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_curso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        TextView textViewTituloCurso = view.findViewById(R.id.TituloNombreCurso);
        textViewTituloCurso.setText(this.nombreCurso);

        Button button2 = view.findViewById(R.id.reservaButton);
        button2.setOnClickListener((view2) -> {
            Navigation.findNavController(view2).navigate(R.id.alert_reservaste, new Bundle());
            ;
        });

    }

}
