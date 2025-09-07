package com.example.tpo_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class CatalogoFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("pantalla fragment", "On create pantalla fragment");

        super.onCreate(savedInstanceState);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_curso, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button button = view.findViewById(R.id.verDetalle);
        button.setOnClickListener((view1) -> {
            Navigation.findNavController(view1).navigate(R.id.curso, new Bundle());
            ;
        });
        Button button2 = view.findViewById(R.id.button_inscribirse);
        button2.setOnClickListener((view2) -> {
            Navigation.findNavController(view2).navigate(R.id.alert_reservaste, new Bundle());
            ;
        });
    }
}

