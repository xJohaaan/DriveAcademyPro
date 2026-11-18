package com.driveacademy.pro.controller;

import com.driveacademy.pro.model.ClasePractica;
import com.driveacademy.pro.model.Rol;
import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.repository.ClasePracticaRepository;
import com.driveacademy.pro.repository.UsuarioRepository;
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
import java.util.List;

@Controller
@RequestMapping("/instructor")
public class InstructorController {

    private final UsuarioRepository usuarioRepository;
    private final ClasePracticaRepository clasePracticaRepository;

    public InstructorController(UsuarioRepository usuarioRepository,
                                ClasePracticaRepository clasePracticaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clasePracticaRepository = clasePracticaRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        Usuario instructor = usuarioActual(principal);
        List<ClasePractica> misClases = clasePracticaRepository.findByInstructorIdOrderByFechaHoraDesc(instructor.getId());
        long programadas = misClases.stream()
                .filter(c -> ClasePractica.PROGRAMADA.equals(c.getEstado()))
                .count();
        long completadas = misClases.stream()
                .filter(c -> ClasePractica.COMPLETADA.equals(c.getEstado()))
                .count();

        model.addAttribute("usuario", instructor);
        model.addAttribute("misClases", misClases);
        model.addAttribute("clasesProgramadas", programadas);
        model.addAttribute("clasesCompletadas", completadas);
        model.addAttribute("totalEstudiantes", usuarioRepository.countByRolNombre(Rol.ESTUDIANTE));
        return "instructor-dashboard";
    }

    @PostMapping("/clases/{id}/calificar")
    public String calificarClase(@PathVariable Long id,
                                 @RequestParam Integer calificacion,
                                 @RequestParam(required = false) String observaciones,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        Usuario instructor = usuarioActual(principal);
        ClasePractica clase = clasePracticaRepository.findById(id).orElse(null);

        if (clase == null || !clase.getInstructor().getId().equals(instructor.getId())) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la clase asignada.");
            return "redirect:/instructor/dashboard";
        }
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            redirectAttributes.addFlashAttribute("error", "La calificación debe estar entre 1 y 5.");
            return "redirect:/instructor/dashboard";
        }

        clase.setCalificacion(calificacion);
        clase.setObservaciones(observaciones != null ? observaciones.trim() : null);
        clase.setEstado(ClasePractica.COMPLETADA);
        clasePracticaRepository.save(clase);

        redirectAttributes.addFlashAttribute("exito",
                "Clase de " + clase.getEstudiante().getNombreCompleto() + " calificada y marcada como COMPLETADA.");
        return "redirect:/instructor/dashboard";
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + principal.getName()));
    }
}
