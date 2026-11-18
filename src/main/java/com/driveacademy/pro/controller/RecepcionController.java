package com.driveacademy.pro.controller;

import com.driveacademy.pro.model.ClasePractica;
import com.driveacademy.pro.model.PagoMatricula;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/recepcion")
public class RecepcionController {

    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PagoMatriculaRepository pagoMatriculaRepository;
    private final ClasePracticaRepository clasePracticaRepository;

    public RecepcionController(UsuarioRepository usuarioRepository,
                               VehiculoRepository vehiculoRepository,
                               PagoMatriculaRepository pagoMatriculaRepository,
                               ClasePracticaRepository clasePracticaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.pagoMatriculaRepository = pagoMatriculaRepository;
        this.clasePracticaRepository = clasePracticaRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        List<Usuario> estudiantes = usuarioRepository.findByRolNombreOrderByNombreCompletoAsc(Rol.ESTUDIANTE);
        List<Usuario> instructores = usuarioRepository.findByRolNombreOrderByNombreCompletoAsc(Rol.INSTRUCTOR);
        List<Vehiculo> vehiculosDisponibles = vehiculoRepository.findByEstadoOrderByMarcaAsc(Vehiculo.DISPONIBLE);

        model.addAttribute("usuario", usuarioActual(principal));
        model.addAttribute("totalEstudiantes", estudiantes.size());
        model.addAttribute("totalInstructores", instructores.size());
        model.addAttribute("totalVehiculosDisponibles", vehiculosDisponibles.size());
        model.addAttribute("totalRecaudado", pagoMatriculaRepository.sumarTotalRecaudado());
        model.addAttribute("estudiantes", estudiantes);
        model.addAttribute("instructores", instructores);
        model.addAttribute("vehiculosDisponibles", vehiculosDisponibles);
        model.addAttribute("pagos", pagoMatriculaRepository.findAllByOrderByFechaPagoDesc());
        model.addAttribute("clases", clasePracticaRepository.findAllByOrderByFechaHoraDesc());
        return "recepcion-dashboard";
    }

    @PostMapping("/pagos/guardar")
    public String guardarPago(@RequestParam Long estudianteId,
                              @RequestParam String concepto,
                              @RequestParam Double monto,
                              @RequestParam Integer horasIncluidas,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        Usuario estudiante = usuarioRepository.findById(estudianteId).orElse(null);
        if (estudiante == null || !Rol.ESTUDIANTE.equals(estudiante.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error", "Estudiante no válido.");
            return "redirect:/recepcion/dashboard";
        }
        if (concepto.isBlank() || monto == null || monto <= 0 || horasIncluidas == null || horasIncluidas <= 0) {
            redirectAttributes.addFlashAttribute("error", "Ingresa un concepto, monto y horas válidas mayores a 0.");
            return "redirect:/recepcion/dashboard";
        }

        Usuario recepcionista = usuarioActual(principal);
        PagoMatricula pago = new PagoMatricula(
                estudiante, recepcionista, concepto.trim(), monto, horasIncluidas, LocalDateTime.now());
        pagoMatriculaRepository.save(pago);

        redirectAttributes.addFlashAttribute("exito",
                "Pago registrado para " + estudiante.getNombreCompleto() + " (" + horasIncluidas + " horas).");
        return "redirect:/recepcion/dashboard";
    }

    @PostMapping("/clases/asignar")
    public String asignarClase(@RequestParam Long estudianteId,
                               @RequestParam Long instructorId,
                               @RequestParam Long vehiculoId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                               @RequestParam String tema,
                               RedirectAttributes redirectAttributes) {
        Usuario estudiante = usuarioRepository.findById(estudianteId).orElse(null);
        Usuario instructor = usuarioRepository.findById(instructorId).orElse(null);
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId).orElse(null);

        if (estudiante == null || !Rol.ESTUDIANTE.equals(estudiante.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un estudiante válido.");
            return "redirect:/recepcion/dashboard";
        }
        if (instructor == null || !Rol.INSTRUCTOR.equals(instructor.getRol().getNombre())) {
            redirectAttributes.addFlashAttribute("error", "Selecciona un instructor válido.");
            return "redirect:/recepcion/dashboard";
        }
        if (vehiculo == null || !Vehiculo.DISPONIBLE.equals(vehiculo.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "El vehículo seleccionado no está disponible.");
            return "redirect:/recepcion/dashboard";
        }
        if (tema.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Indica el tema de la clase práctica.");
            return "redirect:/recepcion/dashboard";
        }
        if (clasePracticaRepository.existsByVehiculoIdAndFechaHoraAndEstado(vehiculoId, fechaHora, ClasePractica.PROGRAMADA)) {
            redirectAttributes.addFlashAttribute("error",
                    "El vehículo " + vehiculo.getPlaca() + " ya tiene una clase programada en esa fecha y hora.");
            return "redirect:/recepcion/dashboard";
        }
        if (clasePracticaRepository.existsByInstructorIdAndFechaHoraAndEstado(instructorId, fechaHora, ClasePractica.PROGRAMADA)) {
            redirectAttributes.addFlashAttribute("error",
                    "El instructor " + instructor.getNombreCompleto() + " ya tiene una clase programada en esa fecha y hora.");
            return "redirect:/recepcion/dashboard";
        }

        clasePracticaRepository.save(new ClasePractica(estudiante, instructor, vehiculo, fechaHora, tema.trim()));
        redirectAttributes.addFlashAttribute("exito",
                "Clase asignada a " + estudiante.getNombreCompleto() + " con " + instructor.getNombreCompleto() + ".");
        return "redirect:/recepcion/dashboard";
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + principal.getName()));
    }
}
