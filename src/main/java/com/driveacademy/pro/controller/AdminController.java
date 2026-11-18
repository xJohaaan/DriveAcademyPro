package com.driveacademy.pro.controller;

import com.driveacademy.pro.model.Rol;
import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.model.Vehiculo;
import com.driveacademy.pro.repository.ClasePracticaRepository;
import com.driveacademy.pro.repository.RolRepository;
import com.driveacademy.pro.repository.UsuarioRepository;
import com.driveacademy.pro.repository.VehiculoRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClasePracticaRepository clasePracticaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           VehiculoRepository vehiculoRepository,
                           ClasePracticaRepository clasePracticaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.clasePracticaRepository = clasePracticaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("usuario", usuarioActual(principal));
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        model.addAttribute("usuariosActivos", usuarioRepository.countByActivo(true));
        model.addAttribute("totalAdmins", usuarioRepository.countByRolNombre(Rol.ADMIN));
        model.addAttribute("totalInstructores", usuarioRepository.countByRolNombre(Rol.INSTRUCTOR));
        model.addAttribute("totalRecepcionistas", usuarioRepository.countByRolNombre(Rol.RECEPCIONISTA));
        model.addAttribute("totalEstudiantes", usuarioRepository.countByRolNombre(Rol.ESTUDIANTE));
        model.addAttribute("totalVehiculos", vehiculoRepository.count());
        model.addAttribute("vehiculosDisponibles", vehiculoRepository.countByEstado(Vehiculo.DISPONIBLE));
        model.addAttribute("totalClases", clasePracticaRepository.count());
        model.addAttribute("usuarios", usuarioRepository.findAllByOrderByIdDesc());
        model.addAttribute("vehiculos", vehiculoRepository.findAllByOrderByIdDesc());
        model.addAttribute("categorias", AuthController.CATEGORIAS_LICENCIA);
        return "admin-dashboard";
    }

    @PostMapping("/vehiculos/guardar")
    public String guardarVehiculo(@RequestParam String placa,
                                  @RequestParam String marca,
                                  @RequestParam String modelo,
                                  @RequestParam String tipoCaja,
                                  RedirectAttributes redirectAttributes) {
        String placaLimpia = placa.trim().toUpperCase();
        String marcaLimpia = marca.trim();
        String modeloLimpio = modelo.trim();

        if (placaLimpia.isBlank() || marcaLimpia.isBlank() || modeloLimpio.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Completa todos los campos del vehículo.");
            return "redirect:/admin/dashboard";
        }
        if (vehiculoRepository.existsByPlacaIgnoreCase(placaLimpia)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un vehículo registrado con la placa " + placaLimpia + ".");
            return "redirect:/admin/dashboard";
        }
        if (!List.of("Mecánico", "Automático").contains(tipoCaja)) {
            redirectAttributes.addFlashAttribute("error", "El tipo de caja debe ser Mecánico o Automático.");
            return "redirect:/admin/dashboard";
        }

        vehiculoRepository.save(new Vehiculo(placaLimpia, marcaLimpia, modeloLimpio, tipoCaja, Vehiculo.DISPONIBLE));
        redirectAttributes.addFlashAttribute("exito", "Vehículo " + placaLimpia + " registrado correctamente.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/vehiculos/{id}/estado")
    public String cambiarEstadoVehiculo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Vehiculo vehiculo = vehiculoRepository.findById(id).orElse(null);
        if (vehiculo == null) {
            redirectAttributes.addFlashAttribute("error", "Vehículo no encontrado.");
            return "redirect:/admin/dashboard";
        }

        String nuevoEstado = Vehiculo.DISPONIBLE.equals(vehiculo.getEstado())
                ? Vehiculo.MANTENIMIENTO
                : Vehiculo.DISPONIBLE;
        vehiculo.setEstado(nuevoEstado);
        vehiculoRepository.save(vehiculo);

        redirectAttributes.addFlashAttribute("exito",
                "Vehículo " + vehiculo.getPlaca() + " actualizado a estado " + nuevoEstado + ".");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarEmpleado(@RequestParam String nombreCompleto,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String rolNombre,
                                  @RequestParam(required = false) String licenciaCategoria,
                                  RedirectAttributes redirectAttributes) {
        String nombre = nombreCompleto.trim();
        String correo = email.trim().toLowerCase();
        String categoria = (licenciaCategoria == null || licenciaCategoria.isBlank())
                ? null : licenciaCategoria.trim().toUpperCase();

        if (nombre.length() < 3) {
            redirectAttributes.addFlashAttribute("error", "El nombre completo debe tener al menos 3 caracteres.");
            return "redirect:/admin/dashboard";
        }
        if (!correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            redirectAttributes.addFlashAttribute("error", "El email no tiene un formato válido.");
            return "redirect:/admin/dashboard";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/admin/dashboard";
        }
        if (!List.of(Rol.INSTRUCTOR, Rol.RECEPCIONISTA).contains(rolNombre)) {
            redirectAttributes.addFlashAttribute("error", "Solo se pueden crear empleados con rol Instructor o Recepcionista.");
            return "redirect:/admin/dashboard";
        }
        if (usuarioRepository.existsByEmail(correo)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario registrado con el email " + correo + ".");
            return "redirect:/admin/dashboard";
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseGet(() -> rolRepository.save(new Rol(rolNombre)));

        Usuario nuevo = new Usuario(nombre, correo, passwordEncoder.encode(password), categoria, rol);
        usuarioRepository.save(nuevo);

        redirectAttributes.addFlashAttribute("exito", "Empleado " + nombre + " (" + rol.getEtiqueta() + ") creado correctamente.");
        return "redirect:/admin/dashboard";
    }

    private Usuario usuarioActual(Principal principal) {
        return usuarioRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + principal.getName()));
    }
}
