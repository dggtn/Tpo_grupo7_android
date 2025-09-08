package com.example.tpo_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.model.Clase;

import java.util.List;

public class CatalogoAdapter extends RecyclerView.Adapter<CatalogoViewHolder>{

    private List<Clase> clases;

    public CatalogoAdapter(List<Clase> clases) {
        this.clases = clases;
    }

    @NonNull
    @Override
    public CatalogoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_curso, parent, false);

        return new CatalogoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogoViewHolder holder, int position) {
        String nobreClase = this.clases.get(position).getName();
        holder.setTituloCurso(nobreClase);
    }

    @Override
    public int getItemCount() {
        return clases.size();
    }
}
