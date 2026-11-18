package com.driveacademy.pro;

import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacionTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void cadaRolEsRedirigidoASuDashboard() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("admin@driveacademy.com").password("123456"))
                .andExpect(redirectedUrl("/admin/dashboard"));
        mvc.perform(formLogin("/login").userParameter("email").user("instructor@driveacademy.com").password("123456"))
                .andExpect(redirectedUrl("/instructor/dashboard"));
        mvc.perform(formLogin("/login").userParameter("email").user("recepcion@driveacademy.com").password("123456"))
                .andExpect(redirectedUrl("/recepcion/dashboard"));
        mvc.perform(formLogin("/login").userParameter("email").user("estudiante@driveacademy.com").password("123456"))
                .andExpect(redirectedUrl("/estudiante/dashboard"));
    }

    @Test
    void contrasenaIncorrectaVuelveAlLoginConError() throws Exception {
        mvc.perform(formLogin("/login").userParameter("email").user("admin@driveacademy.com").password("mala"))
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void sinSesionSeRedirigeAlLogin() throws Exception {
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void paginasPublicasCargan() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Acceso rápido de prueba")));
        mvc.perform(get("/registro")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Registro de nuevos estudiantes")));
        mvc.perform(get("/css/neumorphism.css")).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("admin@driveacademy.com")
    void adminVeSuDashboard() throws Exception {
        mvc.perform(get("/admin/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Administrador General")))
                .andExpect(content().string(containsString("Usuarios registrados")));
    }

    @Test
    @WithUserDetails("instructor@driveacademy.com")
    void instructorVeSuDashboard() throws Exception {
        mvc.perform(get("/instructor/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Carlos Instructor")));
    }

    @Test
    @WithUserDetails("recepcion@driveacademy.com")
    void recepcionVeSuDashboard() throws Exception {
        mvc.perform(get("/recepcion/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Historial de pagos")));
    }

    @Test
    @WithUserDetails("estudiante@driveacademy.com")
    void estudianteVeSuDashboardYNoElDeAdmin() throws Exception {
        mvc.perform(get("/estudiante/dashboard")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Andrés Estudiante")));
        mvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/acceso-denegado"));
    }

    @Test
    void registroCreaUnEstudianteQuePuedeIniciarSesion() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombreCompleto", "María López")
                        .param("email", "Maria@Test.com")
                        .param("licenciaCategoria", "B1")
                        .param("password", "secreta1"))
                .andExpect(redirectedUrl("/login?registrado"));

        Usuario creado = usuarioRepository.findByEmail("maria@test.com").orElseThrow();
        assertThat(creado.getRol().getNombre()).isEqualTo("ROLE_ESTUDIANTE");
        assertThat(creado.getPassword()).startsWith("$2a$");

        mvc.perform(formLogin("/login").userParameter("email").user("maria@test.com").password("secreta1"))
                .andExpect(redirectedUrl("/estudiante/dashboard"));
    }

    @Test
    void registroRechazaEmailDuplicado() throws Exception {
        mvc.perform(post("/registro").with(csrf())
                        .param("nombreCompleto", "Otro Admin")
                        .param("email", "admin@driveacademy.com")
                        .param("password", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ya existe una cuenta")));
    }
}
