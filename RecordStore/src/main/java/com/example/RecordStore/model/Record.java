package com.example.RecordStore.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String label;
    private String genre;
    private int publishingYear;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Track> tracks = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    // Constructors
    public Record() {}
    public Record(String title, String artist, String label , String genre  , int publishingYear,  BigDecimal price) {
        this.title = title;
        this.artist = artist;
        this.label = label;
        this.genre=genre;
        this.publishingYear = publishingYear;
        this.price = price;
    }

    public void addTrack(Track t) {
        tracks.add(t);
        t.setRecord(this);
    }
    public void removeTrack(Track t) {
        tracks.remove(t);
        t.setRecord(null);
    }


    // Getters & Setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getLabel() {
        return label;
    }
    public BigDecimal getPrice() { return price; }
    public String getGenre() {
        return genre;
    }
    public AppUser getOwner() {
        return owner;
    }
    public int getPublishingYear() {
        return publishingYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setOwner(AppUser owner) {
        this.owner = owner;
    }
    public void setPublishingYear(int publishingYear) {
        this.publishingYear = publishingYear;
    }
}

