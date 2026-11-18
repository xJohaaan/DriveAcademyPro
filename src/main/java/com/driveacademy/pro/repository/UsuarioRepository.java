package com.driveacademy.pro.repository;

import com.driveacademy.pro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRolNombre(String nombreRol);

    long countByRolNombreAndActivo(String nombreRol, Boolean activo);

    long countByActivo(Boolean activo);

    List<Usuario> findByRolNombreOrderByNombreCompletoAsc(String nombreRol);

    List<Usuario> findTop5ByRolNombreOrderByIdDesc(String nombreRol);

    List<Usuario> findAllByOrderByIdDesc();
}
