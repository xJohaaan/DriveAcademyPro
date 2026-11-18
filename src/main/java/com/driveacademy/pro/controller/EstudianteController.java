package com.driveacademy.pro.controller;

import com.driveacademy.pro.model.ClasePractica;
import com.driveacademy.pro.model.Rol;
import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.model.Vehiculo;
import com.driveacademy.pro.repository.ClasePracticaRepository;
import com.driveacademy.pro.repository.PagoMatriculaRepository;
import com.driveacademy.pro.repository.UsuarioRepository;
import com.driveacademy.pro.repository.VehiculoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final PagoMatriculaRepository pagoMatriculaRepository;

    public EstudianteController(UsuarioRepository usuarioRepository,
                                VehiculoRepository vehiculoRepository,
                                ClasePracticaRepository clasePracticaRepository,
                                PagoMatriculaRepository pagoMatriculaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clasePracticaRepository = clasePracticaRepository;
        this.pagoMatriculaRepository = pagoMatriculaRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Usuario estudiante = usuarioActual(principal);
        List<Usuario> instructores = usuarioRepository.findByRolNombreOrderByNombreCompletoAsc(Rol.INSTRUCTOR);
        List<Vehiculo> vehiculos = vehiculoRepository.findByEstadoOrderByMarcaAsc(Vehiculo.DISPONIBLE);
        List<ClasePractica> misClases = clasePracticaRepository.findByEstudianteIdOrderByFechaHoraDesc(estudiante.getId());

        model.addAttribute("usuario", estudiante);
        model.addAttribute("instructores", instructores);
        model.addAttribute("vehiculos", vehiculos);
        model.addAttribute("misClases", misClases);
        model.addAttribute("horasPagadas", pagoMatriculaRepository.sumarHorasPorEstudiante(estudiante.getId()));
        return "estudiante-dashboard";
    }

    @PostMapping("/clases/agendar")
    public String agendarClase(@RequestParam Long instructorId,
                               @RequestParam Long vehiculoId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                               @RequestParam String tema,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Usuario estudiante = usuarioActual(principal);
        Usuario instructor = usuarioRepository.findById(instructorId).orElse(null);
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId).orElse(null);

        if (instructor == null || !Rol.INSTRUCTOR.equals(instructor.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error", "El instructor seleccionado no es válido.");
            return "redirect:/estudiante/dashboard";
        }
        if (vehiculo == null || !Vehiculo.DISPONIBLE.equals(vehiculo.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "El vehículo seleccionado no está disponible.");
            return "redirect:/estudiante/dashboard";
        }
        if (tema.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Debes indicar el tema de la clase.");
            return "redirect:/estudiante/dashboard";
        }
        if (clasePracticaRepository.existsByVehiculoIdAndFechaHoraAndEstado(vehiculoId, fechaHora, ClasePractica.PROGRAMADA)) {
            redirectAttributes.addFlashAttribute("error",
                    "Cruce de horario: el vehículo " + vehiculo.getPlaca() + " ya tiene una clase programada a esa hora.");
            return "redirect:/estudiante/dashboard";
        }
        if (clasePracticaRepository.existsByInstructorIdAndFechaHoraAndEstado(instructorId, fechaHora, ClasePractica.PROGRAMADA)) {
            redirectAttributes.addFlashAttribute("error",
                    "Cruce de horario: el instructor " + instructor.getNombreCompleto() + " ya tiene una clase programada a esa hora.");
            return "redirect:/estudiante/dashboard";
        }

        clasePracticaRepository.save(new ClasePractica(estudiante, instructor, vehiculo, fechaHora, tema.trim()));
        redirectAttributes.addFlashAttribute("exito", "Clase práctica agendada correctamente.");
        return "redirect:/estudiante/dashboard";
    }

    @PostMapping("/clases/{id}/cancelar")
    public String cancelarClase(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Usuario estudiante = usuarioActual(principal);
        ClasePractica clase = clasePracticaRepository.findById(id).orElse(null);

        if (clase == null || !clase.getEstudiante().getId().equals(estudiante.getId())) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la clase solicitada.");
            return "redirect:/estudiante/dashboard";
        }
        if (!ClasePractica.PROGRAMADA.equals(clase.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo puedes cancelar clases en estado PROGRAMADA.");
            return "redirect:/estudiante/dashboard";
        }

        clase.setEstado(ClasePractica.CANCELADA);
        clasePracticaRepository.save(clase);

        redirectAttributes.addFlashAttribute("exito", "Clase cancelada correctamente.");
        return "redirect:/estudiante/dashboard";
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + principal.getName()));
    }
}
