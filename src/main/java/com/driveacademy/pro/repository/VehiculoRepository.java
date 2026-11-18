package com.driveacademy.pro.repository;

import com.driveacademy.pro.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    boolean existsByPlacaIgnoreCase(String placa);

    List<Vehiculo> findByEstadoOrderByMarcaAsc(String estado);

    List<Vehiculo> findAllByOrderByIdDesc();

    long countByEstado(String estado);
}
