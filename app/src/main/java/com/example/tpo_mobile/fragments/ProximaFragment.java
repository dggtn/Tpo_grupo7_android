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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    private View scrim;
    private View spinner;

    private void showLoading(boolean show) {
        if (!isAdded()) return;
        int v = show ? View.VISIBLE : View.GONE;
        if (scrim != null) scrim.setVisibility(v);
        if (spinner != null) spinner.setVisibility(v);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_proximos_clases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        recycler = v.findViewById(R.id.recyclerProximas);
        txtEmpty = v.findViewById(R.id.txtEmpty);
        scrim = v.findViewById(R.id.proxProgressScrim);
        spinner = v.findViewById(R.id.proxProgressSpinner);


        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProximasAdapter();
        recycler.setAdapter(adapter);

        // Listener para botón Cancelar de cada item
        adapter.setOnReservaActionListener(item -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar reserva")
                    .setMessage("¿Querés cancelar tu reserva de \"" + item.getNombreCurso() + "\"?")
                    .setPositiveButton("Sí", (d, w) -> cancelar(item.getShiftId()))
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

    private void cancelar(Long shiftId) {
        if (shiftId == null || shiftId <= 0) {
            showMessageDialog("Cancelar reserva", "Turno inválido.", null);
            return;
        }
        showLoading(true);
        gymService.cancelarReserva(shiftId, new SimpleCallback<String>() {
            @Override public void onSuccess(String msg) {
                showLoading(false);
                showMessageDialog("Reserva cancelada",
                        (msg != null && !msg.trim().isEmpty()) ? msg : "Se canceló la reserva y se liberó el cupo.",
                        ProximaFragment.this::cargar);
            }
            @Override public void onError(Throwable error) {
                showLoading(false);
                String em = (error != null && error.getMessage()!=null) ? error.getMessage()
                        : "No se pudo cancelar la reserva.";
                showMessageDialog("No se pudo cancelar", em, null);
            }
        });
    }

    private void showMessageDialog(String title, String message, Runnable onOk) {
        if (!isAdded()) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    if (onOk != null) onOk.run();
                })
                .show();
    }

}
