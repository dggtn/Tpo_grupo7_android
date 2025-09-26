package com.example.tpo_mobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.adapters.ProximasAdapter;
import com.example.tpo_mobile.data.modelDTO.ReservationStatusDTO;
import com.example.tpo_mobile.repository.SimpleCallback;
import com.example.tpo_mobile.services.GymService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProximaFragment extends Fragment {

    @Inject GymService gymService;

    private RecyclerView recycler;
    private TextView txtEmpty;
    private ProximasAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_proximos_clases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        recycler = v.findViewById(R.id.recyclerProximas);  // asegúrate que este id exista en el XML
        txtEmpty = v.findViewById(R.id.txtEmpty);          // idem

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProximasAdapter();
        recycler.setAdapter(adapter);

        // Listener para botón Cancelar de cada item
        adapter.setOnReservaActionListener(item -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar reserva")
                    .setMessage("¿Querés cancelar tu reserva de \"" + item.getNombreCurso() + "\"?")
                    .setPositiveButton("Sí", (d, w) -> cancelar(item))
                    .setNegativeButton("No", null)
                    .show();
        });

        cargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargar(); // refresca al volver
    }

    private void cargar() {
        gymService.getProximasReservas(new SimpleCallback<List<ReservationStatusDTO>>() {
            @Override public void onSuccess(List<ReservationStatusDTO> list) {
                adapter.submit(list);
                txtEmpty.setVisibility((list == null || list.isEmpty()) ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Throwable t) {
                adapter.submit(Collections.emptyList());
                txtEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Error cargando próximas: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelar(ReservationStatusDTO item) {
        Long shiftId = item.getShiftId(); // viene del DTO (lo agregamos en la opción B)
        if (shiftId == null || shiftId <= 0) {
            Toast.makeText(requireContext(), "Turno inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        gymService.cancelarReserva(shiftId, new SimpleCallback<String>() {
            @Override public void onSuccess(String s) {
                Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show();
                cargar(); // recargar para que ahora figure como CANCELADA
            }
            @Override public void onError(Throwable t) {
                Toast.makeText(requireContext(), "Error al cancelar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
