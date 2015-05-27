package com.groovesquid.model;

import java.util.Calendar;
import java.util.List;

public class Album {
    private String id;
    private String name;
    private List<Artist> artists;
    private String imageUrl;
    private Calendar releaseDate;
    private String trackCount;

    public Album(String id, String name, List<Artist> artists, Calendar releaseDate, String trackCount) {
        this.id = id;
        this.name = name;
        this.artists = artists;
        this.releaseDate = releaseDate;
        this.trackCount = trackCount;
    }

    public String getArtistNames() {
        StringBuilder builder = new StringBuilder();
        for(Artist artist : artists) {
            builder.append(artist.getName());
            builder.append(", ");
        }
        String artistNames = builder.toString();
        artistNames = artistNames.substring(0, artistNames.length() - 2);
        return artistNames;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Artist> getArtists() {
        return artists;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public String toString() {
        return getName();
    }

    public Calendar getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseYear() {
        return releaseDate != null ? String.valueOf(releaseDate.get(Calendar.YEAR)) : "";
    }
    
    public String getTrackCount() {
        return trackCount;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Album other = (Album) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return !((this.artists == null) ? (other.artists != null) : !this.artists.containsAll(other.artists) || !other.artists.containsAll(this.artists));
    }
}
