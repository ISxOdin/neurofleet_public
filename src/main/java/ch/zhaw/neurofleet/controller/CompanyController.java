package ch.zhaw.neurofleet.controller;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.neurofleet.model.Company;
import ch.zhaw.neurofleet.model.CompanyCreateDTO;
import ch.zhaw.neurofleet.repository.CompanyRepository;
import ch.zhaw.neurofleet.service.CompanyService;
import ch.zhaw.neurofleet.service.UserService;

@RestController
@RequestMapping("/api")
public class CompanyController {
    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CompanyService companyService;

    @Autowired
    UserService userService;

    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(
            @RequestBody CompanyCreateDTO cDTO) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Company cDAO = companyService.createCompany(
                    cDTO.getName(),
                    cDTO.getEmail(),
                    cDTO.getAddress());
            Company company = companyRepository.save(cDAO);
            return new ResponseEntity<>(company, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/companies")
    public ResponseEntity<Page<Company>> getCompanies(
            @RequestParam(required = false, defaultValue = "1") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "5") Integer pageSize) {
        Page<Company> allCompanies = companyRepository.findAll(PageRequest.of(pageNumber - 1, pageSize));
        return new ResponseEntity<>(allCompanies, HttpStatus.OK);
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable String id) {
        Optional<Company> c = companyRepository.findById(id);
        if (c.isPresent()) {
            return new ResponseEntity<>(c.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(
            @PathVariable String id,
            @RequestBody CompanyCreateDTO dto) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            Company updated = companyService.updateCompany(id, dto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/companies/{id}")
    public ResponseEntity<String> deleteCompanyById(@PathVariable String id) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.OK).body("DELETED");
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/companies/{companyId}/users/{userId}")
    public ResponseEntity<String> addUser(
            @PathVariable String companyId,
            @PathVariable String userId) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        companyService.addUserToCompany(companyId, userId);
        return ResponseEntity.status(HttpStatus.OK).body("ASSIGNED");
    }

    @DeleteMapping("/companies/{companyId}/users/{userId}")
    public ResponseEntity<Void> removeUser(
            @PathVariable String companyId,
            @PathVariable String userId) {
        if (!userService.userHasAnyRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        companyService.removeUserFromCompany(companyId, userId);
        return ResponseEntity.ok().build();
    }

}
