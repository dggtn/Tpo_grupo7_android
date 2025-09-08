package com.example.tpo_mobile;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.Repository.ClasesServiceCallBack;
import com.example.tpo_mobile.adapters.CatalogoAdapter;
import com.example.tpo_mobile.model.Clase;
import com.example.tpo_mobile.services.ClaseService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProximaFragment extends Fragment {

    @Inject
    ClaseService clasesService;
    private ListView listView;
    private List<String> claseDisplayList;
    private ArrayAdapter<String> adapter;

    private List<Clase> clasesMostradas;
    private CatalogoAdapter catalogoAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("pantalla fragment", "On create pantalla fragment");

        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_proximos_clases, container, false);
    }

    private void loadclases() {
        clasesService.getAllClases(new ClasesServiceCallBack() {
            @Override
            public void onSuccess(List<Clase> clases) {
                clasesMostradas.clear();
                clasesMostradas.addAll(clases);
                requireActivity().runOnUiThread(() -> catalogoAdapter.notifyDataSetChanged());
            }

            @Override
            public void onError(Throwable error) {
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(),
                        "Error al cargar clases: " + error.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycle_catalog1);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.clasesMostradas = new ArrayList<>();
        this.catalogoAdapter = new CatalogoAdapter(this.clasesMostradas);
        recyclerView.setAdapter(this.catalogoAdapter);
        loadclases();

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





