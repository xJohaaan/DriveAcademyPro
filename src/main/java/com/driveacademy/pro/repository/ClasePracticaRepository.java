package com.driveacademy.pro.repository;

import com.driveacademy.pro.model.ClasePractica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClasePracticaRepository extends JpaRepository<ClasePractica, Long> {

    boolean existsByVehiculoIdAndFechaHoraAndEstado(Long vehiculoId, LocalDateTime fechaHora, String estado);

    boolean existsByInstructorIdAndFechaHoraAndEstado(Long instructorId, LocalDateTime fechaHora, String estado);

    List<ClasePractica> findByEstudianteIdOrderByFechaHoraDesc(Long estudianteId);

    List<ClasePractica> findByInstructorIdOrderByFechaHoraDesc(Long instructorId);

    List<ClasePractica> findAllByOrderByFechaHoraDesc();

    long countByEstado(String estado);
}
