package com.driveacademy.pro.repository;

import com.driveacademy.pro.model.PagoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoMatriculaRepository extends JpaRepository<PagoMatricula, Long> {

    List<PagoMatricula> findAllByOrderByFechaPagoDesc();

    List<PagoMatricula> findByEstudianteIdOrderByFechaPagoDesc(Long estudianteId);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM PagoMatricula p")
    Double sumarTotalRecaudado();

    @Query("SELECT COALESCE(SUM(p.horasIncluidas), 0) FROM PagoMatricula p WHERE p.estudiante.id = :estudianteId")
    Integer sumarHorasPorEstudiante(@Param("estudianteId") Long estudianteId);
}
