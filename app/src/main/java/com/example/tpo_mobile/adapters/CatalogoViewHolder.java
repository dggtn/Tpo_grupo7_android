package com.example.tpo_mobile.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.R;

public class CatalogoViewHolder extends RecyclerView.ViewHolder {
    public TextView textView;
    //  public ImageView imageView;

    public CatalogoViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.tituloCurso);
//            imageView = itemView.findViewById(R.id.item_image);
    }

    public void setTituloCurso(String titulo){
        textView.setText(titulo);
    }
}

