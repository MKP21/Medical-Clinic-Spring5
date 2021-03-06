package clinic.controllers;

import clinic.mappers.*;
import clinic.model.SurgeryTreatment;
import clinic.services.PatientService;
import clinic.services.ProviderService;
import clinic.services.TreatmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/treatments")
public class TreatmentController{
    private final TreatmentService treatmentService;
    private final PatientService patientService;
    private final ProviderService providerService;

    public TreatmentController(TreatmentService treatmentService, PatientService patientService, ProviderService providerService) {
        this.treatmentService = treatmentService;
        this.patientService = patientService;
        this.providerService = providerService;
    }

    @GetMapping({"","/"})
    public ResponseEntity<Set<TreatmentDTO>> getAllTreatments(){
        return new ResponseEntity<>(treatmentService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TreatmentDTO> getTreatmentById(@PathVariable Long id){
        return new ResponseEntity<>(treatmentService.findById(id), HttpStatus.OK);
    }

    //TODO Convert parameter to Map<String,Object>, read field and transform map into appropriate DTO
    @PostMapping
    public ResponseEntity<TreatmentDTO> newTreatment(@RequestBody String payload){
        TreatmentDTO t = null;
        try{
            t = payLoadtoDto(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(treatmentService.save(t),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TreatmentDTO> editTreatment(@RequestBody TreatmentDTO treatmentDTO,@PathVariable Long id){
        TreatmentDTO old = treatmentService.findById(id);
        if(old == null){
            treatmentDTO.setId(id);
            PatientDTO p = treatmentDTO.getPatient();
            p.getTreatments().add(treatmentDTO.getId());
            treatmentDTO.setPatient(patientService.save(p));
            ProviderDTO providerDTO = treatmentDTO.getProvider();
            providerDTO.getTreatments().add(id);
            treatmentDTO.setProvider(providerService.save(providerDTO));
            TreatmentDTO sav = treatmentService.save(treatmentDTO);
            return new ResponseEntity<>(sav,
                    HttpStatus.CREATED);
        }else{
            old.setDiagnosis(treatmentDTO.getDiagnosis());
            old.setPatient(treatmentDTO.getPatient());
            old.setProvider(treatmentDTO.getProvider());
            old.setTreatmentType(treatmentDTO.getTreatmentType());
            return new ResponseEntity<>(treatmentService.save(old), HttpStatus.OK);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTreatment(@PathVariable Long id){
        treatmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public TreatmentDTO payLoadtoDto(String payload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TreatmentDTO t = null;
        if(payload.contains("RADIOLOGYTREATMENT")){
            t = objectMapper.readValue(payload, RadiologyTreatmentDTO.class);
        }else if(payload.contains("SURGERYTREATMENT")){
            t = objectMapper.readValue(payload, SurgeryTreatmentDTO.class);
        }else{
            t = objectMapper.readValue(payload, DrugTreatmentDTO.class);
        }
        return t;
    }
}
