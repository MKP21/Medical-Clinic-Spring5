package clinic.repositories;
import clinic.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentRepository  extends JpaRepository<Treatment,Long> {
}
