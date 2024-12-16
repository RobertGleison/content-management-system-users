package com.example.netflixplus.utils;

import com.example.netflixplus.retrofitAPI.RetrofitNetworkConfig;

import org.libtorrent4j.AlertListener;
import org.libtorrent4j.SessionManager;
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
import java.util.concurrent.TimeUnit;

public class TorrentManager {

    private static TorrentManager instance;
    private final File outputDirectory;
    CountDownLatch signal;
    SessionManager s;
    List<Torrent> runningTorrents = new ArrayList<Torrent>() ;

    public static TorrentManager getInstance(File outputDirectory) {
        if (instance == null) {
            System.out.println("Creating Torrent Instance with ouput Directory: " + outputDirectory);
            instance = new TorrentManager(outputDirectory);
        }
        return instance;
    }
    private TorrentManager(File outputDirectory){
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
    public File getOutputDirectory(){
        return outputDirectory;
    }
    public File downloadTorrent(byte[] torrent, String movieName, String token){
        TorrentInfo torrentInfo = new TorrentInfo(torrent);
        String filename = torrentInfo.files().fileName(0);
        String httpSeed = String.format("%s/%s/%s/%s", RetrofitNetworkConfig.BASE_URL, "movies", movieName, filename);
        System.out.println("httpSeed = " + httpSeed);
        torrentInfo.addHttpSeed(httpSeed, "Bearer " + token);
        File movieDir = new File(outputDirectory, movieName);
        s.download(torrentInfo,movieDir);
        System.out.println("Starting Download");
        TorrentHandle handle = s.find(torrentInfo.infoHash());
        new Torrent(movieName, handle, s);
        for (int i = 0; i < 100; i++) {
            try {
                if (!(signal.await(30, TimeUnit.SECONDS))){
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            handle.forceReannounce();
        }
        try {
            signal.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        File result = new File(movieDir, filename);
        System.out.println("Finished downloading movie to: " + result.getAbsolutePath());
        return result;
    }

}
