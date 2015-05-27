package com.groovesquid.service;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.groovesquid.model.Album;
import com.groovesquid.model.Artist;
import com.groovesquid.model.Song;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchService extends HttpService {

    public Song getSongDetails(Song song) {
        return song;
    }

    public List<Song> getSongsByQuery(String query) {
        String response = null;
        try {
            response = get("http://search.musicbrainz.org/ws/2/recording/?query=" + URLEncoder.encode("\"" + query + "\"", "UTF-8") + "&fmt=json&dismax=true&limit=100");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonValue recordings = JsonObject.readFrom(response).get("recording-list").asObject().get("recording");

        List<Song> songs = new ArrayList<Song>();

        if (recordings == null || recordings.asArray().isEmpty()) {
            return songs;
        }

        recordingsLoop:
        for (JsonValue recording : recordings.asArray()) {
            List<Artist> artists = new ArrayList<Artist>();
            for (JsonValue artistsJson : recording.asObject().get("artist-credit").asObject().get("name-credit").asArray()) {
                artists.add(new Artist(artistsJson.asObject().get("artist").asObject().get("id").asString(), artistsJson.asObject().get("artist").asObject().get("name").asString()));
            }

            Album album;
            Calendar date = null;

            if (recording.asObject().get("release-list") != null) {
                JsonObject release = recording.asObject().get("release-list").asObject().get("release").asArray().get(0).asObject();
                date = Calendar.getInstance();
                if (release.get("date") != null) {
                    try {
                        if (release.get("date").asString().length() == 4) {
                            date.setTime(new SimpleDateFormat("Y").parse(release.get("date").asString()));
                        } else if (release.get("date").asString().length() == 7) {
                            date.setTime(new SimpleDateFormat("Y-m").parse(release.get("date").asString()));
                        } else if (release.get("date").asString().length() == 10) {
                            date.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(release.get("date").asString()));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                album = new Album(release.get("id").asString(), release.get("title").asString(), artists, date, null);
            } else {
                album = new Album("", "", artists, null, null);
            }

            Iterator<Song> i = songs.iterator();
            while (i.hasNext()) {
                Song song = i.next();
                if (song.getName().equalsIgnoreCase(recording.asObject().get("title").asString()) && song.getArtists().get(0).getId().equals(artists.get(0).getId())) {
                    if (date != null && song.getAlbum() != null && song.getAlbum().getReleaseDate() != null && date.getTime().before(song.getAlbum().getReleaseDate().getTime())) {
                        i.remove();
                    } else {
                        continue recordingsLoop;
                    }
                }
            }

            Song song = new Song(recording.asObject().get("id").asString(), recording.asObject().get("title").asString(), artists, album, recording.asObject().get("length") != null ? recording.asObject().get("length").asLong() : 0);
            songs.add(song);
        }

        return songs;
    }

    public List<Song> getSongsByAlbum(Album album) {
        String response = null;
        try {
            response = get("http://search.musicbrainz.org/ws/2/recording/?query=" + URLEncoder.encode("reid:" + album.getId(), "UTF-8") + "&fmt=json&limit=100");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonValue recordings = JsonObject.readFrom(response).get("recording-list").asObject().get("recording");

        List<Song> songs = new ArrayList<Song>();

        if (recordings == null || recordings.asArray().isEmpty()) {
            return songs;
        }

        for (JsonValue recording : recordings.asArray()) {
            List<Artist> artists = new ArrayList<Artist>();
            for (JsonValue artistsJson : recording.asObject().get("artist-credit").asObject().get("name-credit").asArray()) {
                artists.add(new Artist(artistsJson.asObject().get("artist").asObject().get("id").asString(), artistsJson.asObject().get("artist").asObject().get("name").asString()));
            }

            Song song = new Song(recording.asObject().get("id").asString(), recording.asObject().get("title").asString(), artists, album, recording.asObject().get("length") != null ? recording.asObject().get("length").asLong() : 0);
            songs.add(song);
        }

        return songs;
    }

    public List<Album> getAlbumsByQuery(String query) {
        String response = null;
        try {
            response = get("http://search.musicbrainz.org/ws/2/release/?query=" + URLEncoder.encode("\"" + query + "\"", "UTF-8") + "&fmt=json&dismax=true&limit=100");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JsonValue releases = JsonObject.readFrom(response).get("release-list").asObject().get("release");

        List<Album> albums = new ArrayList<Album>();

        if (releases == null || releases.asArray().isEmpty()) {
            return albums;
        }

        for (JsonValue release : releases.asArray()) {
            List<Artist> artists = new ArrayList<Artist>();
            for (JsonValue artistsJson : release.asObject().get("artist-credit").asObject().get("name-credit").asArray()) {
                artists.add(new Artist(artistsJson.asObject().get("artist").asObject().get("id").asString(), artistsJson.asObject().get("artist").asObject().get("name").asString()));
            }
            Calendar date = Calendar.getInstance();
            if (release.asObject().get("date") != null) {
                try {
                    if (release.asObject().get("date").asString().length() == 4) {
                        date.setTime(new SimpleDateFormat("Y").parse(release.asObject().get("date").asString()));
                    } else if (release.asObject().get("date").asString().length() == 7) {
                        date.setTime(new SimpleDateFormat("Y-m").parse(release.asObject().get("date").asString()));
                    } else if (release.asObject().get("date").asString().length() == 10) {
                        date.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(release.asObject().get("date").asString()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            
            albums.add(new Album(release.asObject().get("id").asString(), release.asObject().get("title").asString(), artists, date, Integer.toString(release.asObject().get("medium-list").asObject().get("track-count").asInt())));
        }

        return albums;
    }

    public List<Artist> getArtistsByQuery(String query) {
        List<Artist> artistsList = new ArrayList<Artist>();

        String response = null;
        try {
            response = get("http://search.musicbrainz.org/ws/2/artist/?query=" + URLEncoder.encode("\"" + query + "\"", "UTF-8") + "&fmt=json&dismax=true&limit=100");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        JsonValue artists = JsonObject.readFrom(response).get("artist-list").asObject().get("artist");
        
        if (artists == null || artists.asArray().isEmpty()) {
            return artistsList;
        }
        
        for (JsonValue artist : artists.asArray()) {
                artistsList.add(new Artist(artist.asObject().get("id").asString(), artist.asObject().asObject().get("name").asString()));
        }

        return artistsList;
    }

    public List<Song> getTopItunesSongs(String country) {
        List<Song> songs = new ArrayList<Song>();
        String response = get("https://itunes.apple.com/" + country.toLowerCase() + "/rss/topsongs/limit=100/explicit=true/json", Arrays.asList(browserHeaders));
        JsonValue entries = JsonObject.readFrom(response).get("feed").asObject().get("entry");
        if (entries != null && entries.isArray()) {
            for (JsonValue entry : entries.asArray()) {
                List<Artist> artists = new ArrayList<Artist>();
                artists.add(new Artist(null, entry.asObject().get("im:artist").asObject().get("label").asString()));
                String title = entry.asObject().get("im:name").asObject().get("label").asString();
                if (title.contains("feat.")) {
                    String[] beforeSplit = title.split("\\(feat.");
                    if (beforeSplit.length > 1) {
                        String[] afterSplit = beforeSplit[1].split("\\)", 2);
                        if (afterSplit[0].contains(",")) {
                            String[] artistSplit = afterSplit[0].split(",");
                            for (String split : artistSplit) {
                                artists.add(new Artist(null, split.trim()));
                            }
                        } else {
                            artists.add(new Artist(null, afterSplit[0].trim()));
                        }
                        title = beforeSplit[0].trim();
                        if (!afterSplit[1].trim().isEmpty()) {
                            title += afterSplit[1];
                        }
                    }
                }
                Album album = new Album(null, entry.asObject().get("im:collection").asObject().get("im:name").asObject().get("label").asString(), artists, null, null);
                songs.add(new Song(null, title, artists, album, 0));
            }
        }
        return songs;
    }

    public List<Song> getTopBillboardSongs() {
        List<Song> songs = new ArrayList<Song>();
        String response = get("http://www.billboard.com/charts/hot-100", Arrays.asList(browserHeaders));
        String[] rows = StringUtils.substringsBetween(response, "<div class=\"row-primary\">", "</article>");
        for (String row : rows) {
            String title = StringEscapeUtils.unescapeHtml4(StringUtils.substringBetween(row, "<h2>", "</h2>").trim());
            String[] artistsSplit = StringEscapeUtils.unescapeHtml4(StringUtils.substringBetween(row, "<h3>", "</h3>").replaceAll("\\<[^>]*>", "").trim()).split("Featuring");
            List<Artist> artists = new ArrayList<Artist>();
            for (String artist : artistsSplit) {
                artists.add(new Artist(null, artist.trim()));
            }
            songs.add(new Song(null, title, artists));
        }
        return songs;
    }

    public List<Song> getTopBeatportSongs() {
        List<Song> songs = new ArrayList<Song>();
        String response = get("https://pro.beatport.com/top-100", Arrays.asList(browserHeaders));
        String playables = StringUtils.substringBetween(response, "window.Playables = ", ";");
        JsonValue tracks = JsonObject.readFrom(playables).get("tracks");
        if (tracks != null && tracks.isArray()) {
            for (JsonValue track : tracks.asArray()) {
                List<Artist> artists = new ArrayList<Artist>();
                for (JsonValue artist : track.asObject().get("artists").asArray()) {
                    artists.add(new Artist(null, artist.asObject().get("name").asString()));
                }
                Album album = new Album(null, track.asObject().get("label").asObject().get("name").asString(), artists, null, null);
                songs.add(new Song(null, track.asObject().get("title").asString(), artists, album, track.asObject().get("duration").asObject().get("milliseconds").asLong()));
            }
        }
        return songs;
    }

    public List<Song> getTopHypemSongs() {
        List<Song> songs = new ArrayList<Song>();
        String response = get("https://api.hypem.com/v2/popular?mode=now&count=50");
        JsonArray items = JsonArray.readFrom(response);
        if (items != null && items.isArray()) {
            for (JsonValue item : items.asArray()) {
                List<Artist> artists = new ArrayList<Artist>();
                artists.add(new Artist(null, item.asObject().get("artist").asString()));
                songs.add(new Song(null, item.asObject().get("title").asString(), artists));
            }
        }
        return songs;
    }

    public List<Song> getSongsByArtist(Artist target) {
        /*
    	String response = null;
        try {
            response = get("http://search.musicbrainz.org/ws/2/recording/?query=" + URLEncoder.encode("arid:" + target.getId(), "UTF-8") + "&fmt=json&limit=100");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        */
        return null;
    	
    }
}
