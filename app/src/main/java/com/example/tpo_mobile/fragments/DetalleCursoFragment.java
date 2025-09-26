package com.example.tpo_mobile.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.tpo_mobile.R;
import com.example.tpo_mobile.data.modelDTO.ClaseDTO;
import com.example.tpo_mobile.data.modelDTO.ShiftDTO;
import com.example.tpo_mobile.model.Clase;
import com.example.tpo_mobile.repository.GetClaseByIdCallback;
import com.example.tpo_mobile.repository.SimpleCallback;
import com.example.tpo_mobile.services.GymService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DetalleCursoFragment extends Fragment {

    @Inject GymService gymService;

    private Long idCurso;
    // Podemos recibir Clase (modelo) o ClaseDTO en distintos flujos.
    private Clase claseModel;      // lo setea obtenerCurso() hoy
    private ClaseDTO claseDTO;     // por si en el futuro cambiás a DTO

    private Button reservarBtn;
    private TextView tituloTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("pantalla fragment", "On create pantalla fragment");
        super.onCreate(savedInstanceState);
        Bundle argumentos = getArguments();
        if (argumentos != null) {
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
        super.onViewCreated(view, savedInstanceState);

        this.tituloTextView = view.findViewById(R.id.TituloNombreCurso);
        this.reservarBtn = view.findViewById(R.id.reservaButton);

        reservarBtn.setEnabled(false);

        reservarBtn.setOnClickListener(v -> {
            Log.d("Detalle", "Click reservar");
            List<ShiftItem> items = extractShiftItems();
            Log.d("Detalle", "Shifts para selector (primera pasada): " + items.size());
            if (items.isEmpty()) {
                cargarShiftsYMostrarSelector();  // <--- NUEVO
                return;
            }
            showShiftSelector(items);
        });

        obtenerCurso();
    }

    private void obtenerCurso() {
        if (idCurso == null || idCurso <= 0) {
            Toast.makeText(getContext(), "Curso inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        gymService.getClasePorId(this.idCurso, new GetClaseByIdCallback() {
            @Override public void onSuccess(Clase clase) {
                claseModel = clase;
                int cants = (clase.getShifts() != null) ? clase.getShifts().size() : 0;
                Log.d("Detalle", "Clase cargada. shifts=" + cants + " (modelo)");
                if (tituloTextView != null) {
                    tituloTextView.setText(clase.getNombre() != null ? clase.getNombre() : "Detalle del curso");
                }
                int cant = (clase.getShifts() != null) ? clase.getShifts().size() : 0;
                Log.d("Detalle", "DTO cargado. shifts=" + cant);
                reservarBtn.setEnabled(true);
            }
            @Override public void onError(Throwable error) {
                Toast.makeText(getContext(), "Error cargando curso: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // ================== Selector ==================
    private void showShiftSelector(List<ShiftItem> items) {
        String[] labels = new String[items.size()];
        for (int i = 0; i < items.size(); i++) labels[i] = items.get(i).label();

        new AlertDialog.Builder(requireContext())
                .setTitle("Elegí un horario")
                .setItems(labels, (dialog, which) -> {
                    Long shiftId = items.get(which).id;
                    reservarShift(shiftId);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void reservarShift(Long shiftId) {
        if (shiftId == null || shiftId <= 0) {
            Toast.makeText(getContext(), "Turno inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        reservarBtn.setEnabled(false);
        gymService.reservar(shiftId, new SimpleCallback<String>() {
            @Override public void onSuccess(String msg) {
                Toast.makeText(getContext(), "Reserva confirmada", Toast.LENGTH_SHORT).show();
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.alert_reservaste, new Bundle());
                } catch (Exception ignored) {}
                reservarBtn.setEnabled(true);
            }
            @Override public void onError(Throwable error) {
                Toast.makeText(getContext(), "No se pudo reservar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                reservarBtn.setEnabled(true);
            }
        });
    }

    private void cargarShiftsYMostrarSelector() {
        if (idCurso == null || idCurso <= 0) {
            Toast.makeText(getContext(), "Curso inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        gymService.getClaseDtoPorId(idCurso, new com.example.tpo_mobile.repository.SimpleCallback<ClaseDTO>() {
            @Override public void onSuccess(ClaseDTO dto) {
                claseDTO = dto;
                int cant = (dto.getShifts() != null) ? dto.getShifts().size() : 0;
                Log.d("Detalle", "DTO cargado. shifts=" + cant + " (DTO)");
                List<ShiftItem> items = extractShiftItems();
                if (items.isEmpty()) {
                    Toast.makeText(getContext(), "No hay turnos disponibles", Toast.LENGTH_SHORT).show();
                    return;
                }
                showShiftSelector(items);
            }
            @Override public void onError(Throwable error) {
                Toast.makeText(getContext(), "Error obteniendo turnos: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    // ================== Helpers ==================

    // Construye la lista de turnos a partir de lo que tenga cargado (Clase o ClaseDTO)
    @SuppressWarnings("unchecked")
    private List<ShiftItem> extractShiftItems() {
        List<ShiftItem> out = new ArrayList<>();

        // 1) Si tenés DTO tipado con List<ShiftDTO>
        if (claseDTO != null && claseDTO.getShifts() != null) {
            try {
                List<?> raw = (List<?>) claseDTO.getShifts();
                if (!raw.isEmpty() && raw.get(0) instanceof ShiftDTO) {
                    for (Object o : raw) {
                        ShiftDTO s = (ShiftDTO) o;
                        ShiftItem it = new ShiftItem();
                        it.id = s.getId();
                        // ajustá si tu ShiftDTO usa otros nombres:
                        it.dia = safeInt(s.getDiaEnQueSeDicta()); // o getDia()
                        it.inicio = s.getHoraInicio();            // o getInicio()
                        it.fin = s.getHoraFin();                  // o getFin()
                        it.vacancy = safeInt(s.getVacancy());     // o getCupoDisponible()
                        it.sede = toStr(s.getHeadquarterId());         // si lo tenés
                        out.add(it);
                    }
                    return out;
                }
            } catch (Throwable ignored) {}
            // Si no era tipado, cae al parser “raw” más abajo
            out.addAll(parseRawShifts((List<?>) claseDTO.getShifts()));
            return out;
        }

        // 2) Si tenés modelo de dominio (Clase) con shifts embebidos (raw)
        if (claseModel != null && claseModel.getShifts() != null) {
            out.addAll(parseRawShifts((List<?>) claseModel.getShifts()));
        }

        return out;
    }

    // Parser “raw” (LinkedTreeMap) según campos reales del backend (shift)
    private List<ShiftItem> parseRawShifts(List<?> raw) {
        List<ShiftItem> out = new ArrayList<>();
        if (raw == null) return out;

        for (Object o : raw) {
            if (o instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) o;
                ShiftItem it = new ShiftItem();
                it.id = toLong(m.get("id"));
                it.dia = toInt(m.get("diaEnQueSeDicta"));
                it.inicio = toStr(m.get("horaInicio"));
                it.fin = toStr(m.get("horaFin"));
                it.vacancy = toInt(m.get("vacancy"));

                Object hq = m.get("headquarter");
                if (hq instanceof Map) {
                    Object name = ((Map<?, ?>) hq).get("name");
                    it.sede = (name != null) ? String.valueOf(name) : null;
                } else {
                    Object hid = m.get("headquarter_id");
                    it.sede = (hid != null) ? ("Sede " + hid) : null;
                }
                out.add(it);
            }
        }
        return out;
    }

    private static class ShiftItem {
        Long id;
        String sede;   // nombre o "Sede <id>"
        int dia;       // 1..7
        String inicio; // "HH:mm"
        String fin;    // "HH:mm"
        int vacancy;

        String label() {
            return String.format(Locale.getDefault(),
                    "%s • %s %s-%s • %d vacantes",
                    (sede != null ? sede : "Sede"),
                    dayName(dia), inicio, fin, vacancy
            );
        }
    }

    private static String dayName(int d) {
        switch (d) {
            case 1: return "Lun";
            case 2: return "Mar";
            case 3: return "Mié";
            case 4: return "Jue";
            case 5: return "Vie";
            case 6: return "Sáb";
            case 7: return "Dom";
            default: return "Día";
        }
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }

    private static int safeInt(Object o) {
        try {
            if (o == null) return 0;
            if (o instanceof Number) return ((Number) o).intValue();
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) { return 0; }
    }

    private static String toStr(Object o) { return (o == null) ? "" : String.valueOf(o); }
}
