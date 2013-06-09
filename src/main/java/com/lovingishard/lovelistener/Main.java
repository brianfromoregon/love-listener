package com.lovingishard.lovelistener;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lang.Loggers;
import org.slf4j.Logger;


public class Main {
    static final Logger log = Loggers.contextLogger();

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {

        Config config = ConfigFactory.load();
        TwitterStreamer streamer = new TwitterStreamer(config);
        MongoBeamStore store = new MongoBeamStore(config);

        PoorManPubsub.Stream<Beam> beams = streamer.getBeams().mergeWith(sampleBeams());

        beams.map(String::valueOf).forEach(log::info);
        beams.forEach(store::write);

        store.connect();
        streamer.start();
    }

    public void shutdown() {

    }

    private static PoorManPubsub.CanBeUpdated<Beam> sampleBeams() {
        final PoorManPubsub.CanBeUpdated<Beam> beams = new PoorManPubsub.CanBeUpdated<>();

        new Thread(new Runnable() {
            int x = 0;

            @Override
            public void run() {
                while (true) {
                    beams.update(new Beam.Impl(x++, x++, 0, System.currentTimeMillis(), String.valueOf(x++)));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new AssertionError();
                    }
                }
            }
        }).start();

        return beams;
    }
}
