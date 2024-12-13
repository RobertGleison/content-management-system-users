package com.example.netflixplus.utils;

import org.libtorrent4j.AlertListener;
import org.libtorrent4j.InfoHash;
import org.libtorrent4j.SessionManager;
import org.libtorrent4j.Sha1Hash;
import org.libtorrent4j.TorrentHandle;
import org.libtorrent4j.TorrentInfo;
import org.libtorrent4j.alerts.AddTorrentAlert;
import org.libtorrent4j.alerts.Alert;
import org.libtorrent4j.alerts.AlertType;
import org.libtorrent4j.alerts.BlockFinishedAlert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TorrentManager {

    private final File outputDirectory;
    CountDownLatch signal;
    SessionManager s;
    List<Torrent> runningTorrents = new ArrayList<Torrent>() ;

    TorrentManager(File outputDirectory){
        this.outputDirectory = outputDirectory;
        s = new SessionManager();

        signal = new CountDownLatch(1);
        s.addListener(new AlertListener() {
            @Override
            public int[] types() {
                return null;
            }

            @Override
            public void alert(Alert<?> alert) {
                AlertType type = alert.type();

                switch (type) {
                    case ADD_TORRENT:
                        System.out.println("Torrent added");
                        ((AddTorrentAlert) alert).handle().resume();
                        break;
                    case BLOCK_FINISHED:
                        BlockFinishedAlert a = (BlockFinishedAlert) alert;
                        int p = (int) (a.handle().status().progress() * 100);
                        System.out.println("Progress: " + p + " for torrent name: " + a.torrentName());
                        System.out.println(s.stats().totalDownload());
                        break;
                    case TORRENT_FINISHED:
                        System.out.println("Torrent finished");
                        signal.countDown();
                        break;
                }
            }
        });
        s.start();
    }
    public void downloadTorrent(byte[] torrent, String movieName){
        TorrentInfo torrentInfo = new TorrentInfo(torrent);
        s.download(torrentInfo,outputDirectory);
        TorrentHandle handle = s.find(torrentInfo.infoHash());
        new Torrent(movieName, handle, s);
        try {
            signal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
