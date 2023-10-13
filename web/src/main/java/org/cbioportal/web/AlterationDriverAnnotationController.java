package org.cbioportal.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.model.CustomDriverAnnotationReport;
import org.cbioportal.service.AlterationDriverAnnotationService;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Custom driver annotations", description = " ")
public class AlterationDriverAnnotationController {

    @Autowired
    private AlterationDriverAnnotationService alterationDriverAnnotationService;

    @PreAuthorize("hasPermission(#molecularProfileIds, 'Collection<MolecularProfileId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @PostMapping(value = "/custom-driver-annotation-report/fetch",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Return availability of custom driver annotations for molecular profiles")
    public ResponseEntity<CustomDriverAnnotationReport> fetchAlterationDriverAnnotationReport(
        @RequestBody(required = true) List<String> molecularProfileIds) {

        CustomDriverAnnotationReport customDriverAnnotationReport = alterationDriverAnnotationService.getCustomDriverAnnotationProps(molecularProfileIds);

        return new ResponseEntity<>(customDriverAnnotationReport, HttpStatus.OK);
    }
}

