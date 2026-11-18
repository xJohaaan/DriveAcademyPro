package com.driveacademy.pro.config;

import com.driveacademy.pro.model.Rol;
import com.driveacademy.pro.model.Usuario;
import com.driveacademy.pro.model.Vehiculo;
import com.driveacademy.pro.repository.RolRepository;
import com.driveacademy.pro.repository.UsuarioRepository;
import com.driveacademy.pro.repository.VehiculoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Precarga roles, usuarios de prueba y vehículos iniciales si las tablas están vacías.
 * Contraseña de todos los usuarios de prueba: 123456
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           VehiculoRepository vehiculoRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (rolRepository.count() == 0) {
            Rol admin = rolRepository.save(new Rol(Rol.ADMIN));
            Rol instructor = rolRepository.save(new Rol(Rol.INSTRUCTOR));
            Rol recepcionista = rolRepository.save(new Rol(Rol.RECEPCIONISTA));
            Rol estudiante = rolRepository.save(new Rol(Rol.ESTUDIANTE));

            String clave = passwordEncoder.encode("123456");

            usuarioRepository.save(new Usuario("Administrador General", "admin@driveacademy.com", clave, null, admin));
            usuarioRepository.save(new Usuario("Carlos Instructor", "instructor@driveacademy.com", clave, "C1", instructor));
            usuarioRepository.save(new Usuario("Laura Recepción", "recepcion@driveacademy.com", clave, null, recepcionista));
            usuarioRepository.save(new Usuario("Andrés Estudiante", "estudiante@driveacademy.com", clave, "B1", estudiante));

            System.out.println(">>> DriveAcademy Pro: roles y 4 usuarios de prueba creados (clave 123456).");
        }

        if (vehiculoRepository.count() == 0) {
            vehiculoRepository.save(new Vehiculo("DAP-101", "Renault", "Sandero", "Mecánico", Vehiculo.DISPONIBLE));
            vehiculoRepository.save(new Vehiculo("DAP-202", "Chevrolet", "Onix", "Automático", Vehiculo.DISPONIBLE));
            vehiculoRepository.save(new Vehiculo("DAP-303", "Kia", "Picanto", "Mecánico", Vehiculo.DISPONIBLE));

            System.out.println(">>> DriveAcademy Pro: 3 vehículos iniciales creados.");
        }
    }
}
