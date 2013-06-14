package com.lovingishard.lovelistener;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lang.Loggers;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;


public class Main {
    static final Logger log = Loggers.contextLogger();

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Config config = ConfigFactory.load();
//        TwitterStreamer streamer = new TwitterStreamer(config);
//        MongoBeamStore mongo = new MongoBeamStore(config);
        FusionTableWriter fusion = new FusionTableWriter(config);

//        PoorManPubsub.Stream<Beam> beams = streamer.getBeams().mergeWith(sampleBeams());
        PoorManPubsub.Stream<Beam> beams = sampleBeams();

        beams.map(String::valueOf).forEach(log::info);
//        beams.forEach(mongo::write);
        beams.forEach(fusion::write);

        fusion.connect();
//        mongo.connect();
//        streamer.start();
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
                    beams.update(new Beam.Random());
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
