package com.example.tpo_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;

import java.util.ArrayList;
import java.util.List;

public class ProximasAdapter extends RecyclerView.Adapter<ProximasAdapter.VH> {

    public interface OnReservaActionListener {
        void onCancelarClick(ReservationStatusDTO item);
    }

    private final List<ReservationStatusDTO> data = new ArrayList<>();
    private OnReservaActionListener listener;

    public void setOnReservaActionListener(OnReservaActionListener l) { this.listener = l; }

    public void submit(List<ReservationStatusDTO> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_reserva, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ReservationStatusDTO it = data.get(pos);
        h.titulo.setText(it.getNombreCurso());

        // Subtítulo con estado visible
        String sub = it.getDiaClase() + " " + it.getHoraClase()
                + (it.getEstadoReserva() != null ? "  • Estado: " + it.getEstadoReserva() : "");
        h.sub.setText(sub);

        // Botón cancelar sólo para ACTIVA + cancelable
        boolean mostrarCancelar = "ACTIVA".equalsIgnoreCase(it.getEstadoReserva()) && it.isCancelable();
        h.btnCancelar.setVisibility(mostrarCancelar ? View.VISIBLE : View.GONE);
        h.btnCancelar.setOnClickListener(v -> {
            if (listener != null) listener.onCancelarClick(it);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView titulo, sub;
        Button btnCancelar;
        VH(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.txtTitulo);
            sub = itemView.findViewById(R.id.txtSub);
            btnCancelar = itemView.findViewById(R.id.btnCancelar);
        }
    }
}
