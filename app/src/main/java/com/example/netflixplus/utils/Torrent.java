package com.example.netflixplus.utils;

import org.libtorrent4j.SessionManager;
import org.libtorrent4j.TorrentHandle;

public class Torrent {
    final String movieName;
    final TorrentHandle handle;
    private final SessionManager session;

    Torrent(String movieName, TorrentHandle th, SessionManager session){

        this.movieName = movieName;
        this.handle = th;
        this.session = session;
    }
    void pause(){
        handle.pause();
    }
    void remove(){
        session.remove(handle);
    }
}