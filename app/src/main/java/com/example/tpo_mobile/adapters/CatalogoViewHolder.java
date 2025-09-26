package com.example.tpo_mobile.adapters;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.R;

public class CatalogoViewHolder extends RecyclerView.ViewHolder {

    private Long idCurso;
    public TextView textView;
    private Button botonVerDetalle;
    //  public ImageView imageView;

    public CatalogoViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.tituloCurso);
        botonVerDetalle = itemView.findViewById(R.id.verDetalle);
        botonVerDetalle.setOnClickListener((vista) -> {
            Bundle argumentos = new Bundle();
            argumentos.putLong("idCurso", this.idCurso);
            Navigation.findNavController(vista).navigate(R.id.curso, argumentos);
        });

//            imageView = itemView.findViewById(R.id.item_image);
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }

    public void setTituloCurso(String titulo){
        textView.setText(titulo);
    }
}


//falta agregar nombre de entrenador e imagen
//falta agregar listerner a los botones
//falta event listeners para los botones
//falta que boton de ver detalle me muestre detalle del curso(traiga esa info)