package com.industriaplus.backend.maintenancerequest;

import com.industriaplus.backend.technician.TechnicianWorkload;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    @EntityGraph(attributePaths = {"equipment", "sector", "technician"})
    @Query("""
        select request
        from MaintenanceRequest request
        where (:status is null or request.status = :status)
          and (:urgency is null or request.urgency = :urgency)
        """)
    List<MaintenanceRequest> findFiltered(
        @Param("status") MaintenanceRequestStatus status,
        @Param("urgency") Urgency urgency
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"equipment", "sector", "technician"})
    @Query("select request from MaintenanceRequest request where request.id = :id")
    Optional<MaintenanceRequest> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select count(request)
        from MaintenanceRequest request
        where request.technician.id = :technicianId
          and request.urgency = :urgency
          and request.status <> :closedStatus
        """)
    long countActiveByTechnicianAndUrgency(
        @Param("technicianId") Long technicianId,
        @Param("urgency") Urgency urgency,
        @Param("closedStatus") MaintenanceRequestStatus closedStatus
    );

    @Query("""
        select new com.industriaplus.backend.technician.TechnicianWorkload(
            request.technician.id,
            count(request)
        )
        from MaintenanceRequest request
        where request.technician is not null
          and request.urgency = :urgency
          and request.status <> :closedStatus
        group by request.technician.id
        """)
    List<TechnicianWorkload> findTechnicianWorkloads(
        @Param("urgency") Urgency urgency,
        @Param("closedStatus") MaintenanceRequestStatus closedStatus
    );
}
