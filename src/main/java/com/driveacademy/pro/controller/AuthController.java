package com.driveacademy.pro.controller;

import com.driveacademy.pro.config.CustomSuccessHandler;
import com.driveacademy.pro.model.Rol;
import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.repository.RolRepository;
import com.driveacademy.pro.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    public static final List<String> CATEGORIAS_LICENCIA =
            List.of("A1", "A2", "B1", "B2", "B3", "C1", "C2", "C3");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String inicio() {
        return "landing";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (estaAutenticado(authentication)) {
            return "redirect:" + CustomSuccessHandler.obtenerDestino(authentication);
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registroForm(Model model, Authentication authentication) {
        if (estaAutenticado(authentication)) {
            return "redirect:" + CustomSuccessHandler.obtenerDestino(authentication);
        }
        model.addAttribute("categorias", CATEGORIAS_LICENCIA);
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombreCompleto,
                            @RequestParam String email,
                            @RequestParam(required = false) String licenciaCategoria,
                            @RequestParam String password,
                            Model model) {

        String nombre = nombreCompleto.trim();
        String correo = email.trim().toLowerCase();
        String categoria = (licenciaCategoria == null || licenciaCategoria.isBlank())
                ? null : licenciaCategoria.trim().toUpperCase();

        String error = null;
        if (nombre.length() < 3) {
            error = "Escribe tu nombre completo.";
        } else if (!correo.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            error = "El email no tiene un formato válido.";
        } else if (password.length() < 6) {
            error = "La contraseña debe tener al menos 6 caracteres.";
        } else if (categoria != null && !CATEGORIAS_LICENCIA.contains(categoria)) {
            error = "La categoría de licencia no es válida.";
        } else if (usuarioRepository.existsByEmail(correo)) {
            error = "Ya existe una cuenta registrada con ese email.";
        }

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("nombreCompleto", nombre);
            model.addAttribute("email", correo);
            model.addAttribute("licenciaCategoria", categoria);
            model.addAttribute("categorias", CATEGORIAS_LICENCIA);
            return "registro";
        }

        Rol rolEstudiante = rolRepository.findByNombre(Rol.ESTUDIANTE)
                .orElseGet(() -> rolRepository.save(new Rol(Rol.ESTUDIANTE)));

        Usuario nuevo = new Usuario(nombre, correo, passwordEncoder.encode(password), categoria, rolEstudiante);
        usuarioRepository.save(nuevo);

        return "redirect:/login?registrado";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado(Authentication authentication, Model model) {
        if (estaAutenticado(authentication)) {
            model.addAttribute("volverA", CustomSuccessHandler.obtenerDestino(authentication));
        } else {
            model.addAttribute("volverA", "/login");
        }
        return "acceso-denegado";
    }

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
