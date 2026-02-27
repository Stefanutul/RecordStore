package com.example.RecordStore.controller;


import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.dtos.PersonDto;
import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Person;
import com.example.RecordStore.service.AppUserService;
import com.example.RecordStore.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/api/persons")
public class PracticeController {

    public final AppUserService userService;

    public final PersonService personService;

    public PracticeController(AppUserService userService , PersonService personService){
        this.userService = userService;
        this.personService = personService;
    }


    @GetMapping()
    public ResponseEntity<List<Person>> getAllPersons(){
        return ResponseEntity.ok(personService.getPersons());
    }


    @GetMapping(value = "/{id}")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id){
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    @PostMapping()
    public ResponseEntity<Person> createPerson(@Valid@RequestBody PersonDto pers) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.createPerson(pers));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id , @Valid@RequestBody PersonDto personDto) {

        Person personUpdated = personService.updatePerson(id , personDto);

        return ResponseEntity.ok(personUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Person> deletePerson(@PathVariable Long id ) {

        personService.deletePerson(id);

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/majors")
    public ResponseEntity<List<Person>> getMajors() {
        return ResponseEntity.ok(personService.findMajors());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Person>> addMultiplePeople(@Valid @RequestBody List<PersonDto> people) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.addMultiplePersons(people));
    }

    @GetMapping("/gnames")
    public ResponseEntity<List<String>> getGmailUsersNames() {
       return ResponseEntity.ok(personService.getGmailNames());
    }


}
