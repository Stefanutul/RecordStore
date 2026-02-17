package com.example.RecordStore.model;


import jakarta.persistence.*;



@Entity
@Table(name="orderItem")
public class OrderItem {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private SoldListing record;






}
